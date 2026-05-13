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

    // AUTH USER
    private User getAuthenticatedUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {

            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() ->
                        new RuntimeException("User tidak ditemukan"));
    }


    // SUPER ADMIN / ADMIN
    private boolean isAdmin(User user) {

        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("super_admin")
                                || role.getSlug().equals("admin"));
    }

    // ADMIN PARTNER / EMPLOYEE

    private boolean isAdminPartnerAndEmployee(User user) {

        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("employee")
                                || role.getSlug().equals("admin-partners"));
    }


    // VALIDASI OWNERSHIP

    private OrderItems getValidatedOrderItem(Long id, User currentUser) {

        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Order Item.");
        }

        OrderItems item = orderItemsRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order item not found"));

        // VALIDASI CROSS PARTNER
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


    // KHUSUS SUPER ADMIN

    public List<OrderItems> findAllOrderItems() {

        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {

            throw new RuntimeException(
                    "Akses Ditolak: Admin Partner dan Employee tidak diperbolehkan melihat semua Order Item.");
        }

        return orderItemsRepository.findAll();
    }


    // KHUSUS PARTNER
    public List<OrderItems> findAll() {

        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {

            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Order Item.");
        }

        return orderItemsRepository.findAll()
                .stream()
                .filter(item ->
                        item.getOrder() != null
                                && item.getOrder().getPartner() != null
                                && item.getOrder().getPartner().getId()
                                .equals(currentUser.getPartner().getId()))
                .toList();
    }


    // FIND BY ID

    public OrderItems findById(Long id) {

        User currentUser = getAuthenticatedUser();

        return getValidatedOrderItem(id, currentUser);
    }


    // FIND BY PRODUCT NAME

    public OrderItems findByProductName(String productName) {
        User currentUser = getAuthenticatedUser();

        // Tambah cek isAdmin
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Order Item.");
        }

        OrderItems item = orderItemsRepository.findByProductName(productName)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        // Tambah null check sebelum akses partner
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

        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {

            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan membuat Order Item.");
        }

        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // VALIDASI CROSS PARTNER
        if (currentUser.getPartner() == null
                || order.getPartner() == null
                || !order.getPartner().getId()
                .equals(currentUser.getPartner().getId())) {

            throw new RuntimeException(
                    "Akses Ditolak: Order milik partner lain.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        OrderItems item = new OrderItems();

        item.setOrder(order);
        item.setProduct(product);

        item.setProductName(product.getName());
        item.setUnitPrice(product.getBasePrice());

        item.setQty(request.getQty());
        item.setSubtotal(
                product.getBasePrice().multiply(request.getQty()));

        return orderItemsRepository.save(item);
    }


    // DELETE

    public void delete(Long id) {

        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {

            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan menghapus Order Item.");
        }

        OrderItems item = getValidatedOrderItem(id, currentUser);

        orderItemsRepository.delete(item);
    }
}