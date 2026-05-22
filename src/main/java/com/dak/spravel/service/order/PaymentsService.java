package com.dak.spravel.service.order;

import com.dak.spravel.dto.request.order.PaymentsRequest;
import com.dak.spravel.dto.response.order.PaymentResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import com.dak.spravel.repository.order.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;

    // ─── Auth Helpers ────────────────────────────────────────────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("super_admin")
                        || role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) {
            throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        }
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin")
                        || role.getSlug().equalsIgnoreCase("super_admin"));

        if (isSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Super Admin tidak bisa mengakses endpoint ini. Gunakan akun partner.");
        }

        if (user.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: User tidak terasosiasi dengan Partner manapun.");
        }

        return user;
    }

    private Payments getValidatedPayment(Long id, User currentUser) {
        Payments payment = paymentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan partner.");
        }
        if (payment.getOrder() == null) {
            throw new RuntimeException("Payment tidak memiliki order.");
        }
        boolean valid = payment.getOrder().getPartner() != null &&
                payment.getOrder().getPartner().getId().equals(currentUser.getPartner().getId());
        if (!valid) {
            throw new RuntimeException("Akses Ditolak: Payment milik partner lain.");
        }
        return payment;
    }

    // Mapper Payments

    private PaymentResponse mapToResponse(Payments p) {
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

    //  Read 

    public List<PaymentResponse> findAllPayments() {
        getAuthenticatedSuperAdmin();
        return paymentsRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<PaymentResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan partner.");
        }
        return paymentsRepository.findAll().stream()
                .filter(p -> p.getOrder() != null
                        && p.getOrder().getPartner() != null
                        && p.getOrder().getPartner().getId().equals(currentUser.getPartner().getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public Page<PaymentResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan partner.");
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paymentsRepository.findAll(pageRequest)
                .map(p -> {
                    if (p.getOrder() == null) return null;
                    boolean allowed = p.getOrder().getPartner() != null
                            && p.getOrder().getPartner().getId().equals(currentUser.getPartner().getId());
                    return allowed ? mapToResponse(p) : null;
                });
    }


    @Transactional
    public PaymentResponse pay(PaymentsRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Orders orders = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (currentUser.getPartner() == null
                || orders.getPartner() == null
                || !orders.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order milik partner lain.");
        }

        Payments payments = new Payments();
        payments.setOrder(orders);
        payments.setMethod(Payments.Method.valueOf(request.getMethod().toUpperCase()));
        payments.setAmount(request.getAmount());
        payments.setCreatedAt(LocalDateTime.now());

        if (request.getMethod().equalsIgnoreCase("cash")) {
            BigDecimal cashTendered = request.getCashTendered();
            if (cashTendered == null) {
                throw new RuntimeException("Cash wajib diisi");
            }
            if (cashTendered.compareTo(orders.getTotal()) < 0) {
                throw new RuntimeException("Cash tidak cukup");
            }
            payments.setCashTendered(cashTendered);
            payments.setChangeDue(cashTendered.subtract(orders.getTotal()));
            payments.setStatus(Payments.Status.VERIFIED);
            orders.setStatus(Orders.PaymentStatus.PAID);

        } else if (request.getMethod().equalsIgnoreCase("transfer")) {
            if (request.getBankName() == null || request.getReferenceNo() == null) {
                throw new RuntimeException("Bank & reference wajib diisi");
            }
            payments.setBankName(request.getBankName());
            payments.setReferenceNo(request.getReferenceNo());
            payments.setProofUrl(request.getProofUrl());
            payments.setCashTendered(BigDecimal.ZERO);
            payments.setChangeDue(BigDecimal.ZERO);
            payments.setStatus(Payments.Status.PENDING);
            orders.setStatus(Orders.PaymentStatus.PAID);

        } else {
            throw new RuntimeException("Method tidak valid");
        }

        ordersRepository.save(orders);
        return mapToResponse(paymentsRepository.save(payments));
    }

    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Payments payments = getValidatedPayment(id, currentUser);
        paymentsRepository.delete(payments);
    }

    @Transactional
    public PaymentResponse verifyPayment(Long id) {
        User currentUser = getAuthenticatedUser();
        
        boolean isAdminPartner = currentUser.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners"));
        if (!isAdminPartner) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner (owner/manager) yang bisa mengkonfirmasi pembayaran transfer.");
        }

        Payments payment = getValidatedPayment(id, currentUser);

        if (payment.getMethod() != Payments.Method.TRANSFER) {
            throw new RuntimeException("Hanya pembayaran TRANSFER yang perlu dikonfirmasi.");
        }
        if (payment.getStatus() == Payments.Status.VERIFIED) {
            throw new RuntimeException("Pembayaran sudah dikonfirmasi sebelumnya.");
        }
        if (payment.getStatus() == Payments.Status.FAILED) {
            throw new RuntimeException("Pembayaran sudah ditandai FAILED, tidak bisa dikonfirmasi.");
        }

        payment.setStatus(Payments.Status.VERIFIED);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setUpdatedBy(currentUser);

        return mapToResponse(paymentsRepository.save(payment));
    }
}
