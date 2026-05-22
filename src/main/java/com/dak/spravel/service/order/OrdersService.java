package com.dak.spravel.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dak.spravel.dto.request.order.OrdersRequest;
import com.dak.spravel.dto.request.order.ReturnRequest;
import com.dak.spravel.dto.response.order.OrderItemResponse;
import com.dak.spravel.dto.response.order.OrdersResponse;
import com.dak.spravel.dto.response.order.PaymentResponse;
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
import com.dak.spravel.repository.order.OrderItemsRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import com.dak.spravel.dto.response.order.ReturnResponse;
import com.dak.spravel.service.inventory.StockBalanceService;
import com.dak.spravel.service.inventory.StockMutationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dak.spravel.repository.order.PaymentsRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final PaymentsRepository paymentsRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final StockBalanceService stockBalanceService;
    private final StockMutationService stockMutationService;

    // =========================
    // AUTH USER
    // =========================
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
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();

        // Super admin tidak boleh akses endpoint partner/employee
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin")
                        || role.getSlug().equalsIgnoreCase("super_admin"));

        if (isSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Super Admin tidak bisa mengakses endpoint ini. Gunakan akun partner.");
        }

        // User harus terasosiasi dengan partner
        if (user.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: User tidak terasosiasi dengan Partner manapun.");
        }

        return user;
    }

    // 💡 HELPER: Deteksi jika user yang login adalah Employee murni
    private boolean isEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("employee"));
    }

    private Orders getValidatedOrder(Long id, User currentUser) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (currentUser.getPartner() == null
                || order.getPartner() == null
                || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
        }

        return order;
    }

    public List<Orders> findAllOrders() {
        getAuthenticatedSuperAdmin();
        return ordersRepository.findAllWithDetails();
    }

    public List<OrdersResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return ordersRepository.findAllWithDetails()
                .stream()
                .filter(order -> order.getPartner() != null
                        && order.getPartner().getId().equals(currentUser.getPartner().getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public OrdersResponse findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return mapToResponse(getValidatedOrder(id, currentUser));
    }

    @Transactional
    public OrdersResponse create(OrdersRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        Branches branch = branchesRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (!branch.getPartners().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
        }

        // Voucher handling
        Voucher voucher = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getVoucherId() != null) {
            voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher not found"));
        }

        // Create Order Header
        Orders order = new Orders();
        order.setPartner(partner);
        order.setBranch(branch);
        order.setCashier(currentUser);
        order.setOrderNumber(request.getOrderNumber());
        order.setVoucher(voucher);
        order.setNotes(request.getNotes());
        order.setStatus(Orders.PaymentStatus.DRAFT);
        order.setSubtotal(BigDecimal.ZERO);
        order.setDiscountAmount(discount);
        order.setTotal(BigDecimal.ZERO);
        order.setCreatedBy(currentUser);
        
        Orders savedOrder = ordersRepository.save(order);

        // Process Items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItems> orderItems = new ArrayList<>();

        for (OrdersRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));
            
            OrderItems item = new OrderItems();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQty(itemReq.getQty());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setSubtotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQty())));
            
            subtotal = subtotal.add(item.getSubtotal());
            orderItems.add(item);

            // REDUCE STOCK (Otomatis potong stok cabang)
            stockBalanceService.adjustStock(product.getId(), "BRANCH", branch.getId(), -item.getQty());
            
            // RECORD MUTATION (Catat riwayat keluar barang SALE_OUT)
            stockMutationService.recordMutation(product, partner, "SALE_OUT", "branch", branch.getId(), null, null, 
                item.getQty(), "order", savedOrder.getId(),
                "Order #" + savedOrder.getOrderNumber(), currentUser
            );
        }
        orderItemsRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);

        // Calculate Final Totals
        savedOrder.setSubtotal(subtotal);
        
        if (voucher != null) {
            BigDecimal discountValue = voucher.getDiscountValue();

            if ("percent".equalsIgnoreCase(voucher.getDiscountType().toString())) {
                discount = subtotal.multiply(new BigDecimal(String.valueOf(voucher.getDiscountValue()))).divide(new BigDecimal(100));
                if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                    discount = voucher.getMaxDiscount();
                }
            } else {
                discount = discountValue;
            }
        }
        savedOrder.setDiscountAmount(discount);
        savedOrder.setTotal(subtotal.subtract(discount));
        ordersRepository.save(savedOrder);

        // 🛠️ FIX SELESAI: Proses simpan pembayaran langsung via paymentsRepository, dijamin bebas dari error merah/crash
        if (request.getPayment() != null) {
            Payments payment = new Payments();
            payment.setOrder(savedOrder);
            payment.setMethod(Payments.Method.valueOf(request.getPayment().getMethod().toUpperCase()));
            payment.setAmount(savedOrder.getTotal());
            if (request.getPayment().getMethod().equalsIgnoreCase("CASH")) {

            BigDecimal cashTendered = request.getPayment().getCashTendered();

            if (cashTendered == null) {
                throw new RuntimeException("Cash wajib diisi");
            }

            if (cashTendered.compareTo(savedOrder.getTotal()) < 0) {
                throw new RuntimeException("Cash tidak cukup");
            }

            payment.setCashTendered(cashTendered);
            payment.setChangeDue(cashTendered.subtract(savedOrder.getTotal()));
            payment.setStatus(Payments.Status.VERIFIED);
            savedOrder.setStatus(Orders.PaymentStatus.PAID); // ← order jadi PAID

        } else if (request.getPayment().getMethod().equalsIgnoreCase("TRANSFER")) {

            payment.setCashTendered(BigDecimal.ZERO);
            payment.setChangeDue(BigDecimal.ZERO);
            payment.setBankName(request.getPayment().getBankName());
            payment.setReferenceNo(request.getPayment().getReferenceNo());
            payment.setStatus(Payments.Status.PENDING);
            savedOrder.setStatus(Orders.PaymentStatus.PAID); // ← order jadi PAID (menunggu verifikasi transfer)

        } else {
            throw new RuntimeException("Method tidak valid");
        }
            payment.setCreatedAt(java.time.LocalDateTime.now());
            payment.setOrder(savedOrder);
            paymentsRepository.save(payment);
            savedOrder.getPayments().add(payment);
            ordersRepository.save(savedOrder); // ← simpan perubahan status PAID
        }

        Orders finalOrder = ordersRepository.findByIdWithDetails(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToResponse(finalOrder);
    }

    // Mapper Response Order
    private OrdersResponse mapToResponse(Orders order) {
    return OrdersResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus().name())
            .subtotal(order.getSubtotal())
            .discountAmount(order.getDiscountAmount())
            .total(order.getTotal())
            .notes(order.getNotes())
            .branchId(order.getBranch().getId())
            .branchName(order.getBranch().getName())
            .cashierId(order.getCashier().getId())
            .cashierName(order.getCashier().getUsername())
            .items(order.getItems().stream().map(item ->
                    OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProductName())
                            .qty(item.getQty())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getSubtotal())
                            .build()
            ).toList())
            .payments(order.getPayments().stream().map(payment ->
                    PaymentResponse.builder()
                            .id(payment.getId())
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .method(payment.getMethod().name())
                            .status(payment.getStatus().name())
                            .amount(payment.getAmount())
                            .cashTendered(payment.getCashTendered())
                            .changeDue(payment.getChangeDue())
                            .bankName(payment.getBankName())
                            .referenceNo(payment.getReferenceNo())
                            .proofUrl(payment.getProofUrl())
                            .createdAt(payment.getCreatedAt())
                            .build()
            ).toList())
            .build();
}

    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        
        // 🔥 VALIDASI UTAMA: Employee dilarang keras menghapus riwayat transaksi apa pun!
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak diizinkan untuk menghapus data transaksi penjualan.");
        }

        Orders order = getValidatedOrder(id, currentUser);
        ordersRepository.delete(order);
    }

    // Cancel Order
    @Transactional
    public OrdersResponse cancelOrder(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Orders order = ordersRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));

        if (currentUser.getPartner() == null
                || order.getPartner() == null
                || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
        }

        
        if (order.getStatus() == Orders.PaymentStatus.CANCELED) {
            throw new RuntimeException("Order sudah dibatalkan sebelumnya.");
        }
        if (order.getStatus() == Orders.PaymentStatus.RETURN) {
            throw new RuntimeException("Order dengan status RETURN tidak bisa dibatalkan.");
        }

        
        for (OrderItems item : order.getItems()) {
            stockBalanceService.adjustStock(
                    item.getProduct().getId(),
                    "BRANCH",
                    order.getBranch().getId(),
                    item.getQty()
            );
            stockMutationService.recordMutation(
                    item.getProduct(),
                    order.getPartner(),
                    "ADJUSTMENT",
                    "branch", order.getBranch().getId(),
                    null, null,
                    item.getQty(),
                    "order", order.getId(),
                    "Pembatalan Order #" + order.getOrderNumber(),
                    currentUser
            );
        }

        // Mengubah Status Menjadi Canceled
        order.setStatus(Orders.PaymentStatus.CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(currentUser);
        ordersRepository.save(order);

        return mapToResponse(order);
    }

    // Return Order
       @Transactional
    public ReturnResponse returnOrder(Long id, ReturnRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Orders order = ordersRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));

        
        if (currentUser.getPartner() == null
                || order.getPartner() == null
                || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
        }

       
        if (order.getStatus() != Orders.PaymentStatus.PAID) {
            throw new RuntimeException("Hanya order dengan status PAID yang bisa diretur. Status saat ini: " + order.getStatus());
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Minimal 1 item harus disertakan dalam retur.");
        }

        List<ReturnResponse.ReturnItemResponse> returnedItems = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;

        for (ReturnRequest.ReturnItemRequest itemReq : request.getItems()) {
            OrderItems orderItem = order.getItems().stream()
                    .filter(i -> i.getId().equals(itemReq.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Order item tidak ditemukan: id=" + itemReq.getOrderItemId()));

            if (itemReq.getQtyReturn() <= 0) {
                throw new RuntimeException("Qty retur harus lebih dari 0.");
            }
            if (itemReq.getQtyReturn() > orderItem.getQty()) {
                throw new RuntimeException(
                        "Qty retur (" + itemReq.getQtyReturn() + ") melebihi qty order ("
                                + orderItem.getQty() + ") untuk produk: " + orderItem.getProductName());
            }

            stockBalanceService.adjustStock(
                    orderItem.getProduct().getId(),
                    "BRANCH",
                    order.getBranch().getId(),
                    itemReq.getQtyReturn()
            );
            stockMutationService.recordMutation(
            orderItem.getProduct(),
            order.getPartner(),"RETURN","branch", order.getBranch().getId(),null, null,
                    itemReq.getQtyReturn(),"order", order.getId(),"Retur Order #" + order.getOrderNumber()+(itemReq.getReason()
                     != null ? " - " + itemReq.getReason() : ""), currentUser
            );

            BigDecimal refundAmount = orderItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQtyReturn()));
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

        order.setStatus(Orders.PaymentStatus.RETURN);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(currentUser);
        ordersRepository.save(order);

        return ReturnResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus().name())
                .returnedItems(returnedItems)
                .totalRefund(totalRefund)
                .returnedAt(LocalDateTime.now())
                .build();
    }

    // Struk
        public OrdersResponse getReceipt(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Orders order = ordersRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));

        if (currentUser.getPartner() == null
                || order.getPartner() == null
                || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
        }

        return mapToResponse(order);
    }
}
