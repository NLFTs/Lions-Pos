package com.dak.spravel.service.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.request.order.PaymentsRequest;
import com.dak.spravel.dto.response.order.PaymentResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import com.dak.spravel.repository.order.PaymentsRepository;
import com.dak.spravel.service.inventory.StockBalanceService;
import com.dak.spravel.service.inventory.StockMutationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final StockBalanceService stockBalanceService;
    private final StockMutationService stockMutationService;

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // KUNCI DINAMIS: Check permission dinamis dari database tanpa kaku nge-lock nama role
    private void checkPermission(User user, String permissionSlug) {
        // Raja Super Admin (partner null) bypass seluruh jenis gate permission
        if (user.getPartner() == null) {
            return;
        }

        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private Payments getValidatedPayment(Long id, User currentUser) {
        Payments payment = paymentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        
        // Super Admin global bebas bypass pengecekan tenant ID
        if (currentUser.getPartner() == null) {
            return payment;
        }

        if (payment.getOrder() == null) {
            throw new RuntimeException("Data Payment tidak memiliki asosiasi Order.");
        }

        boolean valid = payment.getOrder().getPartner() != null &&
                payment.getOrder().getPartner().getId().equals(currentUser.getPartner().getId());
        
        if (!valid) {
            throw new RuntimeException("Akses Ditolak: Data Payment ini milik partner lain.");
        }
        return payment;
    }

    // ─── MAPPER SECTION ────────────────────────────────────────────────────

    private PaymentResponse mapToResponse(Payments p) {
        if (p == null) return null;
        
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .orderNumber(p.getOrder() != null ? p.getOrder().getOrderNumber() : null)
                .method(p.getMethod() != null ? p.getMethod().name() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .amount(p.getAmount())
                .cashTendered(p.getCashTendered())
                .changeDue(p.getChangeDue())
                .bankName(p.getBankName())
                .referenceNo(p.getReferenceNo())
                .proofUrl(p.getProofUrl())
                .createdAt(p.getCreatedAt())
                .build();
    }

    // ─── MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<PaymentResponse> findAllPayments() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        
        return paymentsRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)

    public List<PaymentResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "payment.index"); // Saring via permission index

        // Handling Super Admin: Lihat data global
        if (currentUser.getPartner() == null) {
            return paymentsRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        return paymentsRepository.findAll().stream()
                .filter(p -> p.getOrder() != null
                        && p.getOrder().getPartner() != null
                        && p.getOrder().getPartner().getId().equals(currentUser.getPartner().getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public Page<PaymentResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "payment.index");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Handling Super Admin Global
        if (currentUser.getPartner() == null) {
            return paymentsRepository.findAll(pageable).map(this::mapToResponse);
        }

        return paymentsRepository.findAll(pageable)
                .map(p -> {
                    if (p.getOrder() == null) return null;
                    boolean allowed = p.getOrder().getPartner() != null
                            && p.getOrder().getPartner().getId().equals(currentUser.getPartner().getId());
                    return allowed ? mapToResponse(p) : null;
                });
    }

    // ==========================================
    // PROCESS PAYMENT (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public PaymentResponse pay(PaymentsRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "payment.store"); // Berhak memicu pembuatan transaksi invoice baru

        Orders orders = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orders", request.getOrderId()));

        if (currentUser.getPartner() != null) {
            if (orders.getPartner() == null || !orders.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Order milik partner lain.");
            }
        }

        Payments payments = new Payments();
        payments.setOrder(orders);
        payments.setMethod(Payments.Method.valueOf(request.getMethod().toUpperCase()));
        payments.setAmount(request.getAmount());
        payments.setCreatedAt(LocalDateTime.now());

        if (request.getMethod().equalsIgnoreCase("cash")) {
            BigDecimal cashTendered = request.getCashTendered();
            if (cashTendered == null) {
                throw new RuntimeException("Jumlah uang tunai (Cash Tendered) wajib diisi.");
            }
            if (cashTendered.compareTo(orders.getTotal()) < 0) {
                throw new RuntimeException("Gagal: Jumlah uang tunai tidak mencukupi.");
            }
            payments.setCashTendered(cashTendered);
            payments.setChangeDue(cashTendered.subtract(orders.getTotal()));
            payments.setStatus(Payments.Status.VERIFIED);
            orders.setStatus(Orders.PaymentStatus.PAID);

        } else if (request.getMethod().equalsIgnoreCase("transfer")) {
            if (request.getBankName() == null || request.getReferenceNo() == null) {
                throw new RuntimeException("Nama Bank & Nomor Referensi wajib dilampirkan.");
            }
            payments.setBankName(request.getBankName());
            payments.setReferenceNo(request.getReferenceNo());
            payments.setProofUrl(request.getProofUrl());
            payments.setCashTendered(BigDecimal.ZERO);
            payments.setChangeDue(BigDecimal.ZERO);
            payments.setStatus(Payments.Status.PENDING);
            
            // Dokumen order terkunci DRAFT hingga admin memverifikasi keabsahan dana di mutasi rekening bank
            orders.setStatus(Orders.PaymentStatus.DRAFT);

        } else {
            throw new RuntimeException("Metode pembayaran tidak valid.");
        }

        ordersRepository.save(orders);
        return mapToResponse(paymentsRepository.save(payments));
    }

    // ==========================================
    // DELETE (Berbasis Permission)
    // ==========================================
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "payment.delete");

        Payments payments = getValidatedPayment(id, currentUser);
        paymentsRepository.delete(payments);
    }

    
    @Transactional
    public PaymentResponse verifyPayment(Long id) {
        User currentUser = getAuthenticatedUser();

        //  hanya user yang punya partner  yang bisa konfirmasi
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin tidak bisa mengkonfirmasi pembayaran. Gunakan akun partner.");
        }

        //  hanya owner/admin-partners yang bisa konfirmasi
        boolean isOwner = currentUser.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equalsIgnoreCase("owner") ||
                        role.getSlug().equalsIgnoreCase("admin-partners"));
        if (!isOwner) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner atau Admin Partner yang bisa mengkonfirmasi pembayaran transfer");
        }

        Payments payment = getValidatedPayment(id, currentUser);

        if (payment.getMethod() != Payments.Method.TRANSFER) {
            throw new RuntimeException("Gagal: Hanya pembayaran dengan metode TRANSFER yang membutuhkan konfirmasi manual.");
        }
        if (payment.getStatus() == Payments.Status.VERIFIED) {
            throw new RuntimeException("Pemberitahuan: Pembayaran ini sudah dikonfirmasi sebelumnya.");
        }
        if (payment.getStatus() == Payments.Status.FAILED) {
            throw new RuntimeException("Gagal: Pembayaran sudah ditandai FAILED, tidak bisa diubah kembali.");
        }

        payment.setStatus(Payments.Status.VERIFIED);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setUpdatedBy(currentUser);

        // Update status invoice order induk menjadi PAID setelah valid
        Orders order = payment.getOrder();
        if (order != null) {
            order.setStatus(Orders.PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            order.setUpdatedBy(currentUser);

            // POTONG STOK SEARA REAL-TIME SAAT SETELAH APPROVAL TRANSFER TERVERIFIKASI
            if (order.getBranch() != null && order.getPartner() != null) {
                for (com.dak.spravel.model.order.OrderItems item : order.getItems()) {
                    stockBalanceService.adjustStock(
                            item.getProduct().getId(),
                            "BRANCH",
                            order.getBranch().getId(),
                            -item.getQty()
                    );
                    
                    // Catat ke dalam baris histori mutasi inventori Spravel
                    stockMutationService.recordMutation(
                            item.getProduct(),
                            order.getPartner(),
                            "SALE_OUT",
                            "BRANCH", order.getBranch().getId(),
                            null, null,
                            item.getQty(),
                            "ORDER", order.getId(),
                            "Order #" + order.getOrderNumber() + " (Transfer Payment Verified By " + currentUser.getUsername() + ")",
                            currentUser
                    );
                }
            }

            ordersRepository.save(order);
        }

        return mapToResponse(paymentsRepository.save(payment));
    }
}