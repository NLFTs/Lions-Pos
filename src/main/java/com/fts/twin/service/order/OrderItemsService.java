package com.fts.twin.service.order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fts.twin.dto.request.order.OrderItemsRequest;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.catalog.Product;
import com.fts.twin.model.common.Partners;
import com.fts.twin.model.order.OrderItems;
import com.fts.twin.model.order.Orders;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.catalog.ProductRepository;
import com.fts.twin.repository.order.OrderItemsRepository;
import com.fts.twin.repository.order.OrdersRepository;
import com.fts.twin.handler.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemsService {

    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;

    //  PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS)

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

    // ─── MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private OrderItems getValidatedOrderItem(Long id, User currentUser) {
        OrderItems item = orderItemsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", id));

        // Super Admin global bebas bypass pengecekan tenant ID
        if (currentUser.getPartner() == null) {
            return item;
        }

        if (item.getOrder() == null || item.getOrder().getPartner() == null || 
                !item.getOrder().getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses Order Item partner lain.");
        }

        return item;
    }

    // ─── MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<OrderItems> findAllOrderItems() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return orderItemsRepository.findAll();
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)

    public List<OrderItems> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.show"); // Masuk ke rumpun melihat data transaksi

        // Handling Super Admin: Tarik semua item transaksi global
        if (currentUser.getPartner() == null) {
            return orderItemsRepository.findAll();
        }

        // Handling Tenant: Filter item yang hanya milik perusahaan context dia sendiri
        return orderItemsRepository.findAll().stream()
                .filter(item -> item.getOrder() != null 
                        && item.getOrder().getPartner() != null
                        && item.getOrder().getPartner().getId().equals(currentUser.getPartner().getId()))
                .toList();
    }

    public OrderItems findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.show");
        return getValidatedOrderItem(id, currentUser);
    }

    public OrderItems findByProductName(String productName) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.show");

        OrderItems item = orderItemsRepository.findByProductName(productName)
                .orElseThrow(() -> new RuntimeException("Order item dengan nama '" + productName + "' tidak ditemukan"));

        // Multi-Tenant Adaptif Guard (Bebaskan jika Super Admin)
        if (currentUser.getPartner() != null) {
            if (item.getOrder() == null || item.getOrder().getPartner() == null || 
                    !item.getOrder().getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses Order Item partner lain.");
            }
        }

        return item;
    }

    // ==========================================
    // CREATE / ADD ITEM (Berbasis Permission)
    // ==========================================
    @Transactional
    public OrderItems create(OrderItemsRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.update"); // Menambah item ke dalam order dihitung sebagai aksi update transaksi

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak boleh membuat item order langsung tanpa scope tenant.");
        }

        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orders", request.getOrderId()));

        // Validasi Cross-Tenant Data Injection Prevention
        if (order.getPartner() == null || !order.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Target Order ini milik partner lain.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        OrderItems item = new OrderItems();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getBasePrice());
        item.setQty(request.getQty());
        item.setSubtotal(product.getBasePrice().multiply(BigDecimal.valueOf(request.getQty())));

        return orderItemsRepository.save(item);
    }

    // ==========================================
    // DELETE ITEM (Berbasis Permission)
    // ==========================================
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "order.update"); // Menghapus baris item belanjaan dihitung sebagai update order

        OrderItems item = getValidatedOrderItem(id, currentUser);
        orderItemsRepository.delete(item);
    }
}