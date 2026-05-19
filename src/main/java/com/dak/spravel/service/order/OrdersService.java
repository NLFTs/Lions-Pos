package com.dak.spravel.service.order;

import com.dak.spravel.dto.request.order.OrdersRequest;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.order.OrderItems;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.catalog.VoucherRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.order.OrderItemsRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import com.dak.spravel.repository.order.PaymentsRepository;
import com.dak.spravel.service.inventory.StockBalanceService;
import com.dak.spravel.service.inventory.StockMutationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final PaymentsRepository paymentsRepository;
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
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee-partners"));
        if (!isAuthorized) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
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

    public List<Orders> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return ordersRepository.findAllWithDetails()
                .stream()
                .filter(order ->
                        order.getPartner() != null
                                && order.getPartner().getId()
                                .equals(currentUser.getPartner().getId()))
                .toList();
    }

    public Orders findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return getValidatedOrder(id, currentUser);
    }

    @Transactional
    public Orders create(OrdersRequest request) {
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
            // Logic diskon bisa dihitung di sini atau dari frontend (tapi amannya di backend)
        }

        // Create Order Header
        Orders order = new Orders();
        order.setPartner(partner);
        order.setBranch(branch);
        order.setCashier(currentUser); // Default cashier
        order.setOrderNumber(request.getOrderNumber());
        order.setVoucher(voucher);
        order.setNotes(request.getNotes());
        order.setStatus(Orders.PaymentStatus.PAID); // Typically paid if from POS
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
            item.setQty(itemReq.getQty());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setSubtotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQty())));
            
            subtotal = subtotal.add(item.getSubtotal());
            orderItems.add(item);

            // REDUCE STOCK
            stockBalanceService.adjustStock(product.getId(), "BRANCH", branch.getId(), item.getQty());
            // adjustStock(product.getId(), "branch", branch.getId(), item.getQty().negate())
            
            // RECORD MUTATION
            stockMutationService.recordMutation( product, partner, "SALE_OUT", "branch", branch.getId(),null, null, 
                item.getQty(), "order", savedOrder.getId(),
                "Order #" + savedOrder.getOrderNumber(), currentUser
                
            );
        }
        orderItemsRepository.saveAll(orderItems);

        // Calculate Final Totals
        savedOrder.setSubtotal(subtotal);
        // Recalculate discount if voucher exists
        if (voucher != null) {
            BigDecimal discountValue = voucher.getDiscountValue();

            // 1. Cek tipe voucher (Gunakan .name() kalau discountType itu Enum)
            if ("percent".equalsIgnoreCase(voucher.getDiscountType().toString())) {
                discount = subtotal.multiply(new BigDecimal(String.valueOf(voucher.getDiscountValue()))).divide(new BigDecimal(100));
                if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                    discount = voucher.getMaxDiscount();
                }
            } else {
                // Jika tipenya 'FIXED' atau nominal langsung
                discount = discountValue;
            }
        }
        savedOrder.setDiscountAmount(discount);
        savedOrder.setTotal(subtotal.subtract(discount));
        ordersRepository.save(savedOrder);

        // Process Payment
        if (request.getPayment() != null) {
            Payments payment = new Payments();
            payment.setOrders((savedOrder));
            payment.setMethod(Payments.Method.valueOf(request.getPayment().getMethod().toUpperCase()));
            payment.setAmount(savedOrder.getTotal());
            payment.setCashTendered(request.getPayment().getCashTendered());
            payment.setChangeDue(request.getPayment().getChangeDue());
            payment.setBankName(request.getPayment().getBankName());
            payment.setReferenceNo(request.getPayment().getReferenceNo());
            payment.setStatus(Payments.Status.SUCCESS);
            payment.setCreatedAt(java.time.LocalDateTime.now());
            paymentsRepository.save(payment);
        }

        return savedOrder;
    }

    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Orders order = getValidatedOrder(id, currentUser);
        ordersRepository.delete(order);
    }
}
