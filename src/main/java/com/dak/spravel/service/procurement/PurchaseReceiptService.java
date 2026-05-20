package com.dak.spravel.service.procurement;

import com.dak.spravel.dto.request.procurement.PurchaseReceiptItemDTO;
import com.dak.spravel.dto.request.procurement.PurchaseReceiptRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.procurement.PurchaseOrder;
import com.dak.spravel.model.procurement.PurchaseOrderItems;
import com.dak.spravel.model.procurement.PurchaseReceipt;
import com.dak.spravel.model.procurement.PurchaseReceiptItem;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderItemsRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderRepository;
import com.dak.spravel.repository.procurement.PurchaseReceiptItemRepository;
import com.dak.spravel.repository.procurement.PurchaseReceiptRepository;
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
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PurchaseReceiptService {

    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final PurchaseReceiptItemRepository purchaseReceiptItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemsRepository purchaseOrderItemsRepository;
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
    // KHUSUS SUPER ADMIN
    // =========================
    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("super_admin"));
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
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    // =========================
    // VALIDASI PURCHASE ORDER
    // =========================
    private PurchaseOrder getValidatedPurchaseOrder(Long id, User currentUser) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        if (currentUser.getPartner() == null
                || po.getPartner() == null
                || !po.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Purchase Order bukan milik partner Anda.");
        }

        return po;
    }

    // =========================
    // VALIDASI PURCHASE RECEIPT
    // =========================
    private PurchaseReceipt getValidatedPurchaseReceipt(Long id, User currentUser) {
        PurchaseReceipt receipt = purchaseReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseReceipt", id));

        if (currentUser.getPartner() == null
                || receipt.getPurchaseOrder() == null
                || receipt.getPurchaseOrder().getPartner() == null
                || !receipt.getPurchaseOrder().getPartner().getId()
                .equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Purchase Receipt bukan milik partner Anda.");
        }

        return receipt;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<PurchaseReceipt> findAllPurchaseReceipt() {
        getAuthenticatedSuperAdmin();
        return purchaseReceiptRepository.findAll();
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<PurchaseReceipt> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        return purchaseReceiptRepository.findAll()
                .stream()
                .filter(receipt ->
                        receipt.getPurchaseOrder() != null
                                && receipt.getPurchaseOrder().getPartner() != null
                                && receipt.getPurchaseOrder().getPartner().getId()
                                .equals(currentUser.getPartner().getId()))
                .toList();
    }

    // =========================
    // PAGINATION KHUSUS PARTNER
    // =========================
    public Page<PurchaseReceipt> findAll(int page, int size) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("receivedDate").descending());
        return purchaseReceiptRepository
                .findAllByPurchaseOrderPartnerId(currentUser.getPartner().getId(), pageRequest);
    }

    // =========================
    // GET BY ORDER ID
    // =========================
    public List<PurchaseReceipt> findByOrderId(Long purchaseOrderId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        getValidatedPurchaseOrder(purchaseOrderId, currentUser);
        return purchaseReceiptRepository.findByPurchaseOrderId(purchaseOrderId);
    }

    // =========================
    // GET BY ID
    // =========================
    public PurchaseReceipt findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return getValidatedPurchaseReceipt(id, currentUser);
    }

    // =========================
    // GET ITEMS
    // =========================
    public List<PurchaseReceiptItem> findItemsByReceiptId(Long receiptId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        getValidatedPurchaseReceipt(receiptId, currentUser);
        return purchaseReceiptItemRepository.findByPurchaseReceiptId(receiptId);
    }

    // =========================
    // CREATE
    // =========================
    @Transactional
    public PurchaseReceipt create(PurchaseReceiptRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        PurchaseOrder po = getValidatedPurchaseOrder(request.getPurchaseOrderId(), currentUser);

        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setPurchaseOrder(po);
        receipt.setReceiptNumber(generateReceiptNumber());
        receipt.setReceivedDate(request.getReceivedDate());
        receipt.setNotes(request.getNotes());

        PurchaseReceipt saved = purchaseReceiptRepository.save(receipt);

        List<PurchaseReceiptItem> items = new ArrayList<>();

        for (PurchaseReceiptItemDTO itemDTO : request.getItems()) {

            PurchaseOrderItems poItem = purchaseOrderItemsRepository
                    .findById(itemDTO.getPurchaseOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PurchaseOrderItem", itemDTO.getPurchaseOrderItemId()));

            if (!poItem.getPurchaseOrder().getId().equals(po.getId())) {
                throw new RuntimeException(
                        "Akses Ditolak: PurchaseOrderItem bukan bagian dari Purchase Order ini.");
            }

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", itemDTO.getProductId()));

            if (product.getPartner() == null
                    || !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
            }

            PurchaseReceiptItem item = new PurchaseReceiptItem();
            item.setPurchaseReceipt(saved);
            item.setPurchaseOrderItem(poItem);
            item.setProduct(product);
            item.setQtyReceived(itemDTO.getQtyReceived());
            item.setUnitCost(poItem.getUnitCost());
            item.setNotes(itemDTO.getNotes());

            poItem.setQtyReceived(poItem.getQtyReceived().add(itemDTO.getQtyReceived()));
            purchaseOrderItemsRepository.save(poItem);

            items.add(item);
        }

        purchaseReceiptItemRepository.saveAll(items);
        updatePoStatus(po);

        return saved;
    }

    // =========================
    // UPDATE STATUS PO
    // =========================
    private void updatePoStatus(PurchaseOrder po) {
        List<PurchaseOrderItems> poItems =
                purchaseOrderItemsRepository.findByPurchaseOrderId(po.getId());

        boolean allReceived = poItems.stream()
                .allMatch(i -> i.getQtyReceived().compareTo(i.getQtyOrdered()) >= 0);

        boolean anyReceived = poItems.stream()
                .anyMatch(i -> i.getQtyReceived().compareTo(java.math.BigDecimal.ZERO) > 0);

        if (allReceived) {
            po.setStatus(PurchaseOrder.Status.RECEIVED);
        } else if (anyReceived) {
            po.setStatus(PurchaseOrder.Status.PARTIAL);
        }

        purchaseOrderRepository.save(po);
    }

    // =========================
    // GENERATE NUMBER
    // =========================
    private String generateReceiptNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(9999) + 1);
        return "GR-" + date + "-" + random;
    }
}