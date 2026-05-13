package com.dak.spravel.service.procurement;

import com.dak.spravel.dto.request.procurement.PurchaseOrderItemDTO;
import com.dak.spravel.dto.request.procurement.PurchaseOrderRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.procurement.PurchaseOrder;
import com.dak.spravel.model.procurement.PurchaseOrderItems;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderItemsRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderRepository;
import com.dak.spravel.repository.procurement.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemsRepository purchaseOrderItemsRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
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
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // =========================
    // SUPER ADMIN / ADMIN
    // =========================
    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("super_admin")
                                || role.getSlug().equals("admin"));
    }

    // =========================
    // ADMIN PARTNER / EMPLOYEE
    // =========================
    private boolean isAdminPartnerAndEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("employee")
                                || role.getSlug().equals("admin-partners"));
    }

    // =========================
    // VALIDASI PURCHASE ORDER (cross-partner)
    // =========================
    private PurchaseOrder getValidatedPurchaseOrder(Long id, User currentUser) {

        // VALIDASI 1: Admin tidak boleh akses
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Purchase Order.");
        }

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        // VALIDASI 2: Cross-Partner Check
        if (currentUser.getPartner() == null
                || po.getPartner() == null
                || !po.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException(
                    "Akses Ditolak: Purchase Order bukan milik partner Anda.");
        }

        return po;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<PurchaseOrder> findAllPurchaseOrders() {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee dan Admin Partner tidak diperbolehkan melihat semua Purchase Order.");
        }

        return purchaseOrderRepository.findAll();
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<PurchaseOrder> findAll() {
        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Purchase Order.");
        }

        return purchaseOrderRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId());
    }

    // =========================
    // PAGINATION KHUSUS PARTNER
    // =========================
    public Page<PurchaseOrder> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Purchase Order.");
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        return purchaseOrderRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // =========================
    // GET BY ID
    // =========================
    public PurchaseOrder findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedPurchaseOrder(id, currentUser);
    }

    // =========================
    // GET ITEMS
    // =========================
    public List<PurchaseOrderItems> findItemsByOrderId(Long orderId) {
        User currentUser = getAuthenticatedUser();
        getValidatedPurchaseOrder(orderId, currentUser);
        return purchaseOrderItemsRepository.findByPurchaseOrderId(orderId);
    }

    // =========================
    // CREATE
    // =========================
    @Transactional
    public PurchaseOrder create(PurchaseOrderRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        // VALIDASI 1: Admin tidak boleh create
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan membuat Purchase Order.");
        }

        // VALIDASI 2: User harus punya partner
        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        // VALIDASI 3: Supplier harus milik partner yang sama
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));

        if (supplier.getPartner() == null
                || !supplier.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException(
                    "Akses Ditolak: Supplier bukan milik partner Anda.");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPartner(partner);
        po.setSupplier(supplier);
        po.setPoNumber(generatePoNumber());
        po.setLocationType(request.getLocationType());
        po.setLocationId(request.getLocationId());
        po.setOrderDate(request.getOrderDate());
        po.setExpectedDate(request.getExpectedDate());
        po.setNotes(request.getNotes());
        po.setStatus(PurchaseOrder.Status.DRAFT);

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        List<PurchaseOrderItems> items = new ArrayList<>();
        for (PurchaseOrderItemDTO itemDTO : request.getItems()) {

            // VALIDASI 4: Product harus milik partner yang sama
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

            if (product.getPartner() == null
                    || !product.getPartner().getId().equals(partner.getId())) {
                throw new RuntimeException(
                        "Akses Ditolak: Product bukan milik partner Anda.");
            }

            PurchaseOrderItems item = new PurchaseOrderItems();
            item.setPurchaseOrder(saved);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQtyOrdered(itemDTO.getQtyOrdered());
            item.setUnitCost(itemDTO.getUnitCost());
            item.setSubtotal(itemDTO.getQtyOrdered().multiply(itemDTO.getUnitCost()));
            items.add(item);
        }

        purchaseOrderItemsRepository.saveAll(items);

        saved.setTotal(items.stream()
                .map(PurchaseOrderItems::getSubtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        purchaseOrderRepository.save(saved);

        return saved;
    }

    // =========================
    // UPDATE STATUS
    // =========================
    public PurchaseOrder updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedUser();

        PurchaseOrder po = getValidatedPurchaseOrder(id, currentUser);
        po.setStatus(PurchaseOrder.Status.valueOf(status.toUpperCase()));
        return purchaseOrderRepository.save(po);
    }

    // =========================
    // SOFT DELETE
    // =========================
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();

        PurchaseOrder po = getValidatedPurchaseOrder(id, currentUser);
        po.setDeletedAt(LocalDateTime.now());
        purchaseOrderRepository.save(po);
    }

    // =========================
    // GENERATE PO NUMBER
    // =========================
    private String generatePoNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PO-" + date + "-";
        long count = purchaseOrderRepository.countByPoNumberStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }
}