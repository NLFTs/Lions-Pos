package com.dak.spravel.service.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.request.order.OrdersRequest;
import com.dak.spravel.dto.request.order.ReturnRequest;
import com.dak.spravel.dto.response.order.OrderItemResponse;
import com.dak.spravel.dto.response.order.OrdersResponse;
import com.dak.spravel.dto.response.order.PaymentResponse;
import com.dak.spravel.dto.response.order.ReturnResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.order.OrderItems;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.catalog.VoucherRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import com.dak.spravel.repository.order.OrderItemsRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import com.dak.spravel.repository.order.PaymentsRepository;
import com.dak.spravel.service.inventory.StockBalanceService;
import com.dak.spravel.service.inventory.StockMutationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final PaymentsRepository paymentsRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final BranchesRepository branchesRepository;
    private final WarehousesRepository warehousesRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
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

    // 🔥 KUNCI DINAMIS: Cek permission langsung dari database tanpa hardcode nama role kaku
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Raja Super Admin (partner null) bypass seluruh jenis gate permission
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

    // 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────
    private Orders getValidatedOrder(Long id, User currentUser) {
        Orders order = ordersRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan dengan ID: " + id));

        // 👑 Super Admin global bebas bypass pengecekan tenant ID
        if (currentUser.getPartner() == null) {
            return order;
        }

        if (order.getPartner() == null || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
        }

        return order;
    }

    // ─── 🚀 MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN — Melihat seluruh order lintas perusahaan di Spravel
    public List<Orders> findAllOrders() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return ordersRepository.findAllWithDetails();
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)
    public List<OrdersResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.index"); // 💡 Ambil hak akses index

        // 👑 Handling Super Admin: Lihat data global
        if (currentUser.getPartner() == null) {
            return ordersRepository.findAllWithDetails().stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        // 🏢 Handling Tenant & Branch Isolation Guard
        return ordersRepository.findAllWithDetails().stream()
                .filter(order -> order.getPartner() != null && order.getPartner().getId().equals(currentUser.getPartner().getId()))
                .filter(order -> {
                    // Jika user dikunci di cabang tertentu, dia cuma boleh liat transaksi di cabangnya sendiri
                    if (currentUser.getBranch() != null) {
                        return order.getBranch() != null && order.getBranch().getId().equals(currentUser.getBranch().getId());
                    }
                    return true;
                })
                .map(this::mapToResponse)
                .toList();
    }

    public OrdersResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.show");

        Orders order = getValidatedOrder(id, currentUser);

        // Branch Isolation Check
        if (currentUser.getBranch() != null && (order.getBranch() == null || !order.getBranch().getId().equals(currentUser.getBranch().getId()))) {
            throw new RuntimeException("Akses Ditolak: Transaksi ini berada di luar cabang penugasan Anda.");
        }

        return mapToResponse(order);
    }

    // ==========================================
    // CREATE / CHECKOUT KASIR (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public OrdersResponse create(OrdersRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.store"); // 💡 Siapapun boleh checkout asal punya permission kasir/sales

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat transaksi langsung.");
        }

        Branches branch = branchesRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));

        if (branch.getPartners() == null || !branch.getPartners().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Cabang/Branch tujuan bukan milik partner Anda.");
        }

        // Branch Isolation Guard: Mencegah kasir cabang A nembak transaksi pake branch cabang B
        if (currentUser.getBranch() != null && !currentUser.getBranch().getId().equals(branch.getId())) {
            throw new RuntimeException("Akses Ditolak: Anda hanya diperbolehkan membuat transaksi di cabang penugasan Anda sendiri.");
        }

        // Voucher handling Context
        Voucher voucher = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getVoucherId() != null) {
            voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Voucher", request.getVoucherId()));

            // Validasi voucher aktif
            if (Boolean.FALSE.equals(voucher.getIs_active())) {
                throw new RuntimeException("Voucher sudah tidak aktif dan tidak bisa digunakan.");
            }

            // Validasi kuota — quota 0 atau null berarti unlimited
            int currentUsed = voucher.getUsed_count() != null ? voucher.getUsed_count() : 0;
            if (voucher.getQuota() != null && voucher.getQuota() > 0 && currentUsed >= voucher.getQuota()) {
                throw new RuntimeException("Kuota voucher sudah habis.");
            }
        }

        // Create Order Header Record
        Orders order = new Orders();
        order.setPartner(partner);
        order.setBranch(branch);
        order.setCashier(currentUser);
        order.setOrderNumber(request.getOrderNumber());
        order.setVoucher(voucher);
        order.setNotes(request.getNotes());
        order.setBuyerName(request.getBuyerName());
        order.setStatus(Orders.PaymentStatus.DRAFT);
        order.setSubtotal(BigDecimal.ZERO);
        order.setDiscountAmount(discount);
        order.setTotal(BigDecimal.ZERO);
        order.setCreatedBy(currentUser);
        order.setCreatedAt(LocalDateTime.now());
        
        Orders savedOrder = ordersRepository.save(order);

        // Process Sub Items Transaction
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItems> orderItems = new ArrayList<>();

        for (OrdersRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));
            
            OrderItems item = new OrderItems();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQty(itemReq.getQty());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setSubtotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQty())));
            
            subtotal = subtotal.add(item.getSubtotal());
            orderItems.add(item);
        }
        orderItemsRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);

        // Calculate Final Grand Totals
        savedOrder.setSubtotal(subtotal);
        
        if (voucher != null) {
            BigDecimal discountValue = voucher.getDiscountValue();

            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType().toString())) {
                discount = subtotal.multiply(new BigDecimal(String.valueOf(voucher.getDiscountValue()))).divide(new BigDecimal(100));
                if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                    discount = voucher.getMaxDiscount();
                }
            } else {
                discount = discountValue;
            }
        }

        //  Diskon Manual
        if (request.getManualDiscountValue() != null && request.getManualDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
            String discType = request.getManualDiscountType() != null
                    ? request.getManualDiscountType().toUpperCase()
                    : "FLAT";

            BigDecimal manualDisc;
            if ("PERCENT".equals(discType)) {
                manualDisc = subtotal
                        .multiply(request.getManualDiscountValue())
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                manualDisc = manualDisc.min(subtotal.subtract(discount));
            } else {
                manualDisc = request.getManualDiscountValue().min(subtotal.subtract(discount));
            }
            discount = discount.add(manualDisc);
            savedOrder.setManualDiscountType(discType);
            savedOrder.setManualDiscountValue(request.getManualDiscountValue());
            savedOrder.setManualDiscountNote(request.getManualDiscountNote());
        }

        savedOrder.setDiscountAmount(discount);
        savedOrder.setTotal(subtotal.subtract(discount).max(BigDecimal.ZERO));

        // Increment used_count voucher jika dipakai
        if (voucher != null) {
            int currentUsed = voucher.getUsed_count() != null ? voucher.getUsed_count() : 0;
            voucher.setUsed_count(currentUsed + 1);

            // Auto-nonaktifkan jika kuota sudah penuh setelah increment
            if (voucher.getQuota() != null && voucher.getQuota() > 0 && voucher.getUsed_count() >= voucher.getQuota()) {
                voucher.setIs_active(false);
            }

            voucherRepository.save(voucher);
        }

        ordersRepository.save(savedOrder);

        // Payment Processing Engine Gateway
        if (request.getPayment() != null) {
            Payments payment = new Payments();
            payment.setOrder(savedOrder);
            payment.setMethod(Payments.Method.valueOf(request.getPayment().getMethod().toUpperCase()));
            payment.setAmount(savedOrder.getTotal());

            if (request.getPayment().getMethod().equalsIgnoreCase("CASH")) {
                BigDecimal cashTendered = request.getPayment().getCashTendered();

                if (cashTendered == null) {
                    throw new RuntimeException("Jumlah uang tunai (Cash Tendered) wajib diisi.");
                }

                if (cashTendered.compareTo(savedOrder.getTotal()) < 0) {
                    throw new RuntimeException("Gagal: Jumlah uang tunai yang dibayarkan tidak mencukupi.");
                }

                payment.setCashTendered(cashTendered);
                payment.setChangeDue(cashTendered.subtract(savedOrder.getTotal()));
                payment.setStatus(Payments.Status.VERIFIED);
                savedOrder.setStatus(Orders.PaymentStatus.PAID);

                // 🔥 POTONG STOK REAL-TIME HANYA JIKA METODE TUNAI LUNAS (CASH PAID)
                for (OrderItems item : orderItems) {
                    stockBalanceService.adjustStock(item.getProduct().getId(), "BRANCH", branch.getId(), -item.getQty());
                    stockMutationService.recordMutation(
                            item.getProduct(), partner, "SALE_OUT",
                            "BRANCH", branch.getId(), null, null,
                            item.getQty(), "ORDER", savedOrder.getId(),
                            "Penjualan Kasir Order #" + savedOrder.getOrderNumber(), currentUser);
                }

            } else if (request.getPayment().getMethod().equalsIgnoreCase("TRANSFER")) {
                payment.setCashTendered(BigDecimal.ZERO);
                payment.setChangeDue(BigDecimal.ZERO);
                payment.setBankName(request.getPayment().getBankName());
                payment.setReferenceNo(request.getPayment().getReferenceNo());
                payment.setStatus(Payments.Status.PENDING);
                
                // Invoice transfer tetap DRAFT sampai divalidasi manual di back-office. Stok berkurang pas verifikasi.
                savedOrder.setStatus(Orders.PaymentStatus.DRAFT);

            } else {
                throw new RuntimeException("Metode pembayaran tidak valid.");
            }

            payment.setCreatedAt(LocalDateTime.now());
            payment.setOrder(savedOrder);
            paymentsRepository.save(payment);
            
            if (savedOrder.getPayments() == null) {
                savedOrder.setPayments(new HashSet<>());
            }
            savedOrder.getPayments().add(payment);
            ordersRepository.save(savedOrder);
        }

        Orders finalOrder = ordersRepository.findByIdWithDetails(savedOrder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Orders", savedOrder.getId()));

        return mapToResponse(finalOrder);
    }

    // ==========================================
    // DELETE (🔒 Berbasis Permission)
    // ==========================================
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.delete");

        Orders order = getValidatedOrder(id, currentUser);
        ordersRepository.delete(order);
    }

    // ==========================================
    // CANCEL ORDER (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public OrdersResponse cancelOrder(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.update"); // Pembatalan dihitung hak kelola update data keuangan

        Orders order = ordersRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orders", id));

        if (currentUser.getPartner() != null) {
            if (order.getPartner() == null || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
            }
        }

        if (order.getStatus() == Orders.PaymentStatus.CANCELED) {
            throw new RuntimeException("Order sudah dibatalkan sebelumnya.");
        }
        if (order.getStatus() == Orders.PaymentStatus.RETURN) {
            throw new RuntimeException("Order dengan status RETURN tidak bisa dibatalkan.");
        }

        // 🔥 LOGIC REVERSAL STOK: Hanya pulangkan kuantitas barang jika status pesanan adalah PAID
        if (order.getStatus() == Orders.PaymentStatus.PAID) {
            for (OrderItems item : order.getItems()) {
                stockBalanceService.adjustStock(item.getProduct().getId(), "BRANCH", order.getBranch().getId(), item.getQty());
                
                // Masukkan rekaman mutasi balik stok (Cancellation Reversal)
                stockMutationService.recordMutation(
                        item.getProduct(), order.getPartner(), "RETURN",
                        null, null, "BRANCH", order.getBranch().getId(),
                        item.getQty(), "ORDER", order.getId(),
                        "Pembatalan Transaksi Penjualan Order #" + order.getOrderNumber(), currentUser
                );
            }
        }

        order.setStatus(Orders.PaymentStatus.CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(currentUser);
        ordersRepository.save(order);

        return mapToResponse(order);
    }

    // ==========================================
    // RETURN ORDER / RETUR (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public ReturnResponse returnOrder(Long id, ReturnRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.update");

        Orders order = ordersRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orders", id));

        if (currentUser.getPartner() != null) {
            if (order.getPartner() == null || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
            }
        }

        if (order.getStatus() != Orders.PaymentStatus.PAID) {
            throw new RuntimeException("Hanya order dengan status PAID yang bisa diretur. Status saat ini: " + order.getStatus());
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Minimal 1 item harus disertakan dalam retur.");
        }

        List<ReturnResponse.ReturnItemResponse> returnedItems = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;

        // ── Tentukan lokasi tujuan return ──────────────────────────────────────
        String targetLocationType;
        Long targetLocationId;
        boolean isDefective = Boolean.TRUE.equals(request.getIsDefective());

        if (isDefective) {
            targetLocationType = "QUARANTINE";
            targetLocationId = order.getPartner().getId();
        } else {
            String reqLocationType = request.getReturnLocationType();
            Long reqLocationId = request.getReturnLocationId();

            if (reqLocationType != null && !reqLocationType.isBlank() && reqLocationId != null) {
                String upperType = reqLocationType.toUpperCase();
                if (!upperType.equals("BRANCH") && !upperType.equals("WAREHOUSE")) {
                    throw new RuntimeException("returnLocationType tidak valid. Gunakan 'BRANCH' atau 'WAREHOUSE'.");
                }
                if (upperType.equals("WAREHOUSE")) {
                    warehousesRepository.findById(reqLocationId)
                            .orElseThrow(() -> new RuntimeException("Gudang tidak ditemukan: id=" + reqLocationId));
                } else {
                    branchesRepository.findById(reqLocationId)
                            .orElseThrow(() -> new RuntimeException("Cabang tidak ditemukan: id=" + reqLocationId));
                }
                targetLocationType = upperType;
                targetLocationId = reqLocationId;
            } else {
                targetLocationType = "BRANCH";
                targetLocationId = order.getBranch().getId();
            }
        }

        String targetLocationLabel = isDefective ? "Karantina"
                : (targetLocationType.equals("WAREHOUSE") ? "Gudang" : "Cabang");

        for (ReturnRequest.ReturnItemRequest itemReq : request.getItems()) {
            OrderItems orderItem = order.getItems().stream()
                    .filter(i -> i.getId().equals(itemReq.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Order item tidak ditemukan: id=" + itemReq.getOrderItemId()));

            if (itemReq.getQtyReturn() <= 0) {
                throw new RuntimeException("Qty retur harus lebih dari 0.");
            }
            if (itemReq.getQtyReturn() > orderItem.getQty()) {
                throw new RuntimeException("Qty retur (" + itemReq.getQtyReturn() + ") melebihi qty order ("
                        + orderItem.getQty() + ") untuk produk: " + orderItem.getProductName());
            }
            if (isDefective) {
                stockBalanceService.addToQuarantine(
                        orderItem.getProduct().getId(),
                        order.getPartner().getId(),
                        itemReq.getQtyReturn(),
                        order.getId(),
                        "Retur Barang Rusak/Expired Order #" + order.getOrderNumber()
                                + (request.getDefectiveNote() != null ? " - " + request.getDefectiveNote() : "")
                                + (itemReq.getReason() != null ? " | Alasan: " + itemReq.getReason() : ""),
                        currentUser
                );
            } else {
                stockBalanceService.adjustStock(orderItem.getProduct().getId(), targetLocationType, targetLocationId, itemReq.getQtyReturn());
                stockMutationService.recordMutation(
                        orderItem.getProduct(), order.getPartner(), "RETURN",
                        null, null, targetLocationType, targetLocationId,
                        itemReq.getQtyReturn(), "ORDER", order.getId(),
                        "Retur Barang Penjualan Order #" + order.getOrderNumber()
                                + " ke " + targetLocationLabel + " (id=" + targetLocationId + ")"
                                + (itemReq.getReason() != null ? " - Alasan: " + itemReq.getReason() : ""),
                        currentUser
                );
            }

            // Simpan returnQty dan returnReason ke OrderItems untuk riwayat
            orderItem.setReturnQty(itemReq.getQtyReturn());
            orderItem.setReturnReason(itemReq.getReason());
            orderItemsRepository.save(orderItem);

            BigDecimal refundAmount = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQtyReturn()));
            totalRefund = totalRefund.add(refundAmount);

            returnedItems.add(ReturnResponse.ReturnItemResponse.builder()
                    .orderItemId(orderItem.getId())
                    .productName(orderItem.getProductName())
                    .qtyReturn(itemReq.getQtyReturn())
                    .unitPrice(orderItem.getUnitPrice())
                    .refundAmount(refundAmount)
                    .reason(itemReq.getReason())
                    .build());
        }

        LocalDateTime returnedAt = LocalDateTime.now();
        order.setStatus(Orders.PaymentStatus.RETURN);
        order.setReturnedAt(returnedAt);
        order.setUpdatedAt(returnedAt);
        order.setUpdatedBy(currentUser);
        ordersRepository.save(order);

        return ReturnResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus().name())
                .returnedItems(returnedItems)
                .totalRefund(totalRefund)
                .returnedAt(returnedAt)
                .returnLocationType(targetLocationType)
                .returnLocationId(targetLocationId)
                .build();
    }

    public OrdersResponse getReceipt(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.show");
        
        Orders order = getValidatedOrder(id, currentUser);
        return mapToResponse(order);
    }

    // ─── 🔄 PRIVATE MAPPERS SECTION ───────────────────────────────────────────

    private OrdersResponse mapToResponse(Orders order) {
        if (order == null) return null;

        List<OrderItemResponse> itemResponses = order.getItems() != null ? order.getItems().stream().map(item ->
                OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .qty(item.getQty())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .returnQty(item.getReturnQty())
                        .returnReason(item.getReturnReason())
                        .build()
        ).toList() : new ArrayList<>();

        List<PaymentResponse> paymentResponses = order.getPayments() != null ? order.getPayments().stream().map(payment ->
                PaymentResponse.builder()
                        .id(payment.getId())
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .method(payment.getMethod() != null ? payment.getMethod().name() : null)
                        .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                        .amount(payment.getAmount())
                        .cashTendered(payment.getCashTendered())
                        .changeDue(payment.getChangeDue())
                        .bankName(payment.getBankName())
                        .referenceNo(payment.getReferenceNo())
                        .proofUrl(payment.getProofUrl())
                        .createdAt(payment.getCreatedAt())
                        .build()
        ).toList() : new ArrayList<>();

        // Build ReturnInfo jika order berstatus RETURN dan ada item yang diretur
        OrdersResponse.ReturnInfo returnInfo = null;
        if (order.getStatus() == Orders.PaymentStatus.RETURN && order.getItems() != null) {
            List<OrdersResponse.ReturnInfo.ReturnItemDetail> returnedItemDetails = order.getItems().stream()
                    .filter(item -> item.getReturnQty() != null && item.getReturnQty() > 0)
                    .map(item -> {
                        BigDecimal refund = item.getUnitPrice() != null
                                ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getReturnQty()))
                                : BigDecimal.ZERO;
                        return OrdersResponse.ReturnInfo.ReturnItemDetail.builder()
                                .productName(item.getProductName())
                                .qtyReturn(item.getReturnQty())
                                .reason(item.getReturnReason())
                                .refundAmount(refund)
                                .build();
                    })
                    .toList();

            BigDecimal totalRefund = returnedItemDetails.stream()
                    .map(OrdersResponse.ReturnInfo.ReturnItemDetail::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (!returnedItemDetails.isEmpty()) {
                returnInfo = OrdersResponse.ReturnInfo.builder()
                        .returnedAt(order.getReturnedAt())
                        .totalRefund(totalRefund)
                        .items(returnedItemDetails)
                        .build();
            }
        }

        return OrdersResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .total(order.getTotal())
                .notes(order.getNotes())
                .buyerName(order.getBuyerName())
                .manualDiscountType(order.getManualDiscountType())
                .manualDiscountValue(order.getManualDiscountValue())
                .manualDiscountNote(order.getManualDiscountNote())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : null)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : null)
                .cashierId(order.getCashier() != null ? order.getCashier().getId() : null)
                .cashierName(order.getCashier() != null ? order.getCashier().getUsername() : null)
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .payments(paymentResponses)
                .returnInfo(returnInfo)
                .build();
    }
}