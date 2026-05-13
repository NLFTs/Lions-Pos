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
    // VALIDASI PURCHASE ORDER
    // =========================
    private PurchaseOrder getValidatedPurchaseOrder(Long id, User currentUser) {

        // VALIDASI 1: Admin tidak boleh akses
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Purchase Receipt.");
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
    // VALIDASI PURCHASE RECEIPT
    // =========================
    private PurchaseReceipt getValidatedPurchaseReceipt(Long id, User currentUser) {

        // VALIDASI 1: Admin tidak boleh akses
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Purchase Receipt.");
        }

        PurchaseReceipt receipt = purchaseReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseReceipt", id));

        // VALIDASI 2: Cross-Partner Check
        if (currentUser.getPartner() == null
                || receipt.getPurchaseOrder() == null
                || receipt.getPurchaseOrder().getPartner() == null
                || !receipt.getPurchaseOrder().getPartner().getId()
                .equals(currentUser.getPartner().getId())) {
            throw new RuntimeException(
                    "Akses Ditolak: Purchase Receipt bukan milik partner Anda.");
        }

        return receipt;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<PurchaseReceipt> findAllPurchaseReceipt() {
        User currentUser = getAuthenticatedUser();

        // Hanya admin yang boleh akses endpoint ini
        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee dan Admin Partner tidak diperbolehkan melihat semua Purchase Receipt.");
        }

        return purchaseReceiptRepository.findAll();
    }

    // =========================
    // KHUSUS PARTNER
    // =========================
    public List<PurchaseReceipt> findAll() {
        User currentUser = getAuthenticatedUser();

        // Admin tidak boleh akses endpoint ini
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Purchase Receipt.");
        }

        // Filter hanya receipt milik partner user yang login
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
        User currentUser = getAuthenticatedUser();

        // Admin tidak boleh akses endpoint ini
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Purchase Receipt.");
        }

        // Partner tidak boleh null
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
        User currentUser = getAuthenticatedUser();

        // Validasi PO sekaligus cek cross-partner
        getValidatedPurchaseOrder(purchaseOrderId, currentUser);

        return purchaseReceiptRepository.findByPurchaseOrderId(purchaseOrderId);
    }

    // =========================
    // GET BY ID
    // =========================
    public PurchaseReceipt findById(Long id) {
        User currentUser = getAuthenticatedUser();

        return getValidatedPurchaseReceipt(id, currentUser);
    }

    // =========================
    // GET ITEMS
    // =========================
    public List<PurchaseReceiptItem> findItemsByReceiptId(Long receiptId) {
        User currentUser = getAuthenticatedUser();

        // Validasi receipt sekaligus cek cross-partner
        getValidatedPurchaseReceipt(receiptId, currentUser);

        return purchaseReceiptItemRepository.findByPurchaseReceiptId(receiptId);
    }

    // =========================
    // CREATE
    // =========================
    @Transactional
    public PurchaseReceipt create(PurchaseReceiptRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        // VALIDASI 1: Admin tidak boleh create
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan membuat Purchase Receipt.");
        }

        // VALIDASI 2: User harus punya partner
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        // VALIDASI 3: PO harus milik partner yang sama
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

            // VALIDASI 4: PO Item harus milik PO yang sama
            if (!poItem.getPurchaseOrder().getId().equals(po.getId())) {
                throw new RuntimeException(
                        "Akses Ditolak: PurchaseOrderItem bukan bagian dari Purchase Order ini.");
            }

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", itemDTO.getProductId()));

            // VALIDASI 5: Product harus milik partner yang sama
            if (product.getPartner() == null
                    || !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException(
                        "Akses Ditolak: Product bukan milik partner Anda.");
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