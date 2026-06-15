package com.dak.spravel.service.procurement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemsRepository purchaseOrderItemsRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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
        // Super Admin (partner null) bypass seluruh jenis gate permission
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

    // Izinkan akses jika punya salah satu dari beberapa permission
    private void checkAnyPermission(User user, String... permissionSlugs) {
        if (user.getPartner() == null) return; // super admin bypass

        boolean hasAny = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> {
                    for (String slug : permissionSlugs) {
                        if (perm.getSlug().equalsIgnoreCase(slug)) return true;
                    }
                    return false;
                });

        if (!hasAny) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses yang diperlukan.");
        }
    }

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private PurchaseOrder getValidatedPurchaseOrder(Long id, User currentUser) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        // Super Admin global bebas bypass pengecekan tenant ID
        if (currentUser.getPartner() == null) {
            return po;
        }

        if (po.getPartner() == null || !po.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Purchase Order bukan milik partner Anda.");
        }

        return po;
    }

    // ─── MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL
    public List<PurchaseOrder> findAllPurchaseOrders() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return purchaseOrderRepository.findAll(Sort.by("createdAt").descending());
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)
    public List<PurchaseOrder> findAll() {
        User currentUser = getAuthenticatedUser();
        // Izinkan akses jika punya purchase_order.index ATAU purchase_receipt.store
        checkAnyPermission(currentUser, "purchase_order.index", "purchase_receipt.store");

        // Super Admin: lihat semua
        if (currentUser.getPartner() == null) {
            return purchaseOrderRepository.findAll(Sort.by("id").descending());
        }

        List<PurchaseOrder> partnerPOs = purchaseOrderRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId(), Sort.by("id").descending());

        // Branch isolation: user cabang hanya lihat PO untuk cabangnya
        if (currentUser.getBranch() != null) {
            final Long branchId = currentUser.getBranch().getId();
            return partnerPOs.stream()
                    .filter(po -> "BRANCH".equalsIgnoreCase(po.getLocationType())
                            && branchId.equals(po.getLocationId()))
                    .toList();
        }

        // Warehouse isolation: user gudang hanya lihat PO untuk gudangnya
        if (currentUser.getWarehouse() != null) {
            final Long warehouseId = currentUser.getWarehouse().getId();
            return partnerPOs.stream()
                    .filter(po -> "WAREHOUSE".equalsIgnoreCase(po.getLocationType())
                            && warehouseId.equals(po.getLocationId()))
                    .toList();
        }

        // Owner/admin partner: lihat semua PO milik partner
        return partnerPOs;
    }

    // PAGINATION TENANT
    public Page<PurchaseOrder> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkAnyPermission(currentUser, "purchase_order.index", "purchase_receipt.store");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Super Admin
        if (currentUser.getPartner() == null) {
            return purchaseOrderRepository.findAll(pageable);
        }

        // Untuk branch/warehouse user, ambil semua dulu lalu filter (karena filter lokasi tidak ada di repo)
        if (currentUser.getBranch() != null || currentUser.getWarehouse() != null) {
            List<PurchaseOrder> filtered = findAll(); // reuse logic di atas
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<PurchaseOrder> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);
            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
        }

        return purchaseOrderRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId(), pageable);
    }

    // GET BY ID
    public PurchaseOrder findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkAnyPermission(currentUser, "purchase_order.show", "purchase_receipt.store");
        return getValidatedPurchaseOrder(id, currentUser);
    }

    // GET ITEMS BY ORDER ID
    public List<PurchaseOrderItems> findItemsByOrderId(Long orderId) {
        User currentUser = getAuthenticatedUser();
        checkAnyPermission(currentUser, "purchase_order.show", "purchase_receipt.store");
        getValidatedPurchaseOrder(orderId, currentUser);
        return purchaseOrderItemsRepository.findByPurchaseOrderId(orderId);
    }

    // ==========================================
    // CREATE PURCHASE ORDER (Berbasis Permission)
    // ==========================================
    @Transactional
    public PurchaseOrder create(PurchaseOrderRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_order.store"); 

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat dokumen Purchase Order langsung.");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));

        if (supplier.getPartner() == null || !supplier.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Supplier bukan milik partner Anda.");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPartner(partner);
        po.setSupplier(supplier);
        po.setPoNumber(generatePoNumber());
        po.setLocationType(request.getLocationType().toUpperCase());
        po.setLocationId(request.getLocationId());
        po.setOrderDate(request.getOrderDate());
        po.setExpectedDate(request.getExpectedDate());
        po.setNotes(request.getNotes());
        po.setStatus(PurchaseOrder.Status.DRAFT);

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        List<PurchaseOrderItems> items = new ArrayList<>();
        for (PurchaseOrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

            if (product.getPartner() == null || !product.getPartner().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Product '" + product.getName() + "' bukan milik partner Anda.");
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
        
        return purchaseOrderRepository.save(saved);
    }

    // ==========================================
    // UPDATE STATUS (Berbasis Permission)
    // ==========================================
    @Transactional
    public PurchaseOrder updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_order.update"); 

        PurchaseOrder po = getValidatedPurchaseOrder(id, currentUser);
        po.setStatus(PurchaseOrder.Status.valueOf(status.toUpperCase()));
        return purchaseOrderRepository.save(po);
    }

    // ==========================================
    // SOFT DELETE (Berbasis Permission)
    // ==========================================
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_order.delete"); 

        PurchaseOrder po = getValidatedPurchaseOrder(id, currentUser);
        po.setDeletedAt(LocalDateTime.now());
        purchaseOrderRepository.save(po);
    }

    // ─── PRIVATE GENERATOR UTILS ───────────────────────────────────────────

    private String generatePoNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PO-" + date + "-";
        long count = purchaseOrderRepository.countByPoNumberStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }
}