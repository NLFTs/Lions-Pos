package com.dak.spravel.service.order;

import com.dak.spravel.dto.request.order.OrderItemsRequest;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.order.OrderItems;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.order.OrderItemsRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemsService {

    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;

    // =========================
    // AUTH USER
    // =========================
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    // =========================
    // KHUSUS ADMIN PARTNER / EMPLOYEE
    // =========================
    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee-partners"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    // =========================
    // VALIDASI OWNERSHIP
    // =========================
    private OrderItems getValidatedOrderItem(Long id, User currentUser) {
        OrderItems item = orderItemsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        if (currentUser.getPartner() == null
                || item.getOrder() == null
                || item.getOrder().getPartner() == null
                || !item.getOrder().getPartner().getId()
                .equals(currentUser.getPartner().getId())) {
            throw new RuntimeException(
                    "Akses Ditolak: Anda tidak bisa mengakses Order Item partner lain.");
        }

        return item;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<OrderItems> findAllOrderItems() {
        getAuthenticatedSuperAdmin();
        return orderItemsRepository.findAll();
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<OrderItems> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        return orderItemsRepository.findAll()
                .stream()
                .filter(item ->
                        item.getOrder() != null
                                && item.getOrder().getPartner() != null
                                && item.getOrder().getPartner().getId()
                                .equals(currentUser.getPartner().getId()))
                .toList();
    }

    // =========================
    // FIND BY ID
    // =========================
    public OrderItems findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return getValidatedOrderItem(id, currentUser);
    }

    // =========================
    // FIND BY PRODUCT NAME
    // =========================
    public OrderItems findByProductName(String productName) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        OrderItems item = orderItemsRepository.findByProductName(productName)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        if (currentUser.getPartner() == null
                || item.getOrder() == null
                || item.getOrder().getPartner() == null
                || !item.getOrder().getPartner().getId()
                .equals(currentUser.getPartner().getId())) {
            throw new RuntimeException(
                    "Akses Ditolak: Anda tidak bisa mengakses Order Item partner lain.");
        }

        return item;
    }

    // =========================
    // CREATE
    // =========================
    public OrderItems create(OrderItemsRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // VALIDASI CROSS PARTNER
        if (order.getPartner() == null
                || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order milik partner lain.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        OrderItems item = new OrderItems();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getBasePrice());
        item.setQty(request.getQty());
        item.setSubtotal(product.getBasePrice().multiply(request.getQty()));

        return orderItemsRepository.save(item);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        OrderItems item = getValidatedOrderItem(id, currentUser);
        orderItemsRepository.delete(item);
    }
}