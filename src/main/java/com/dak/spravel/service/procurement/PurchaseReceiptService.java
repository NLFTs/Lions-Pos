package com.dak.spravel.service.procurement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dak.spravel.dto.request.procurement.PurchaseReceiptItemDTO;
import com.dak.spravel.dto.request.procurement.PurchaseReceiptRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.model.procurement.PurchaseOrder;
import com.dak.spravel.model.procurement.PurchaseOrderItems;
import com.dak.spravel.model.procurement.PurchaseReceipt;
import com.dak.spravel.model.procurement.PurchaseReceiptItem;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.inventory.StockMutationRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderItemsRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderRepository;
import com.dak.spravel.repository.procurement.PurchaseReceiptItemRepository;
import com.dak.spravel.repository.procurement.PurchaseReceiptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseReceiptService {

    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final PurchaseReceiptItemRepository purchaseReceiptItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemsRepository purchaseOrderItemsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    // === STOCK INTEGRATION ===
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMutationRepository stockMutationRepository;

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // 🔥 KUNCI DINAMIS: Cek permission dinamis langsung dari database tanpa hardcode nama role kaku
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

    // ─── 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private PurchaseOrder getValidatedPurchaseOrder(Long id, User currentUser) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        if (currentUser.getPartner() == null) {
            return po;
        }

        if (po.getPartner() == null || !po.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Purchase Order bukan milik partner Anda.");
        }

        return po;
    }

    private PurchaseReceipt getValidatedPurchaseReceipt(Long id, User currentUser) {
        PurchaseReceipt receipt = purchaseReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseReceipt", id));

        if (currentUser.getPartner() == null) {
            return receipt;
        }

        if (receipt.getPurchaseOrder() == null || receipt.getPurchaseOrder().getPartner() == null ||
                !receipt.getPurchaseOrder().getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Purchase Receipt bukan milik partner Anda.");
        }

        return receipt;
    }

    // ─── 🚀 MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL
    public List<PurchaseReceipt> findAllPurchaseReceipt() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return purchaseReceiptRepository.findAll();
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG BARU)
    public List<PurchaseReceipt> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_receipt.index"); // 💡 SINKRON: Menggunakan purchase_receipt.*

        if (currentUser.getPartner() == null) {
            return purchaseReceiptRepository.findAll();
        }

        // Branch/Warehouse Isolation Guard
        return purchaseReceiptRepository.findAll().stream()
                .filter(receipt -> receipt.getPurchaseOrder() != null
                        && receipt.getPurchaseOrder().getPartner() != null
                        && receipt.getPurchaseOrder().getPartner().getId().equals(currentUser.getPartner().getId()))
                .filter(receipt -> {
                    PurchaseOrder po = receipt.getPurchaseOrder();
                    if (po == null || po.getLocationType() == null) return false;

                    if (currentUser.getBranch() != null) {
                        // User cabang: hanya lihat receipt PO untuk cabangnya
                        return po.getLocationType().equalsIgnoreCase("BRANCH")
                                && po.getLocationId().equals(currentUser.getBranch().getId());
                    }
                    if (currentUser.getWarehouse() != null) {
                        // User gudang: hanya lihat receipt PO untuk gudangnya
                        return po.getLocationType().equalsIgnoreCase("WAREHOUSE")
                                && po.getLocationId().equals(currentUser.getWarehouse().getId());
                    }
                    // Owner/admin partner: lihat semua receipt milik partner
                    return true;
                })
                .toList();
    }

    // PAGINATION TENANT
    public Page<PurchaseReceipt> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_receipt.index"); // 💡 SINKRON: Menggunakan purchase_receipt.*

        Pageable pageable = PageRequest.of(page, size, Sort.by("receivedDate").descending());

        if (currentUser.getPartner() == null) {
            return purchaseReceiptRepository.findAll(pageable);
        }

        return purchaseReceiptRepository
                .findAllByPurchaseOrderPartnerId(currentUser.getPartner().getId(), pageable);
    }

    // GET BY ORDER ID
    public List<PurchaseReceipt> findByOrderId(Long purchaseOrderId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_receipt.show"); // 💡 SINKRON: Menggunakan purchase_receipt.*
        getValidatedPurchaseOrder(purchaseOrderId, currentUser);
        return purchaseReceiptRepository.findByPurchaseOrderId(purchaseOrderId);
    }

    // GET BY ID
    public PurchaseReceipt findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_receipt.show"); // 💡 SINKRON: Menggunakan purchase_receipt.*
        
        PurchaseReceipt receipt = getValidatedPurchaseReceipt(id, currentUser);

        // Strict location isolation
        if (currentUser.getPartner() != null) {
            PurchaseOrder po = receipt.getPurchaseOrder();
            if (po != null && po.getLocationType() != null) {
                if (currentUser.getBranch() != null) {
                    if (!po.getLocationType().equalsIgnoreCase("BRANCH")
                            || !po.getLocationId().equals(currentUser.getBranch().getId())) {
                        throw new RuntimeException("Akses Ditolak: Dokumen ini bukan milik cabang Anda.");
                    }
                } else if (currentUser.getWarehouse() != null) {
                    if (!po.getLocationType().equalsIgnoreCase("WAREHOUSE")
                            || !po.getLocationId().equals(currentUser.getWarehouse().getId())) {
                        throw new RuntimeException("Akses Ditolak: Dokumen ini bukan milik gudang Anda.");
                    }
                }
            }
        }

        return receipt;
    }

    // GET ITEMS
    public List<PurchaseReceiptItem> findItemsByReceiptId(Long receiptId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_receipt.show"); // 💡 SINKRON: Menggunakan purchase_receipt.*
        getValidatedPurchaseReceipt(receiptId, currentUser);
        return purchaseReceiptItemRepository.findByPurchaseReceiptId(receiptId);
    }

    // =============================================================
    // CREATE RECEIPT — Proses Penerimaan & Update Stok Real-time
    // =============================================================
    @Transactional
    public PurchaseReceipt create(PurchaseReceiptRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "purchase_receipt.store"); // 💡 SINKRON: Menggunakan purchase_receipt.*

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat dokumen penerimaan langsung.");
        }

        PurchaseOrder po = getValidatedPurchaseOrder(request.getPurchaseOrderId(), currentUser);

        // Branch/Warehouse Isolation Guard: strict — hanya bisa terima PO yang ditujukan ke lokasi sendiri
        if (currentUser.getBranch() != null) {
            // User cabang: hanya bisa terima PO untuk cabangnya sendiri
            if (po.getLocationType() == null
                    || !po.getLocationType().equalsIgnoreCase("BRANCH")
                    || !po.getLocationId().equals(currentUser.getBranch().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda hanya diizinkan menerima barang yang ditujukan ke cabang Anda.");
            }
        } else if (currentUser.getWarehouse() != null) {
            // User gudang: hanya bisa terima PO untuk gudangnya sendiri
            if (po.getLocationType() == null
                    || !po.getLocationType().equalsIgnoreCase("WAREHOUSE")
                    || !po.getLocationId().equals(currentUser.getWarehouse().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda hanya diizinkan menerima barang yang ditujukan ke gudang Anda.");
            }
        }

        if (po.getStatus() == PurchaseOrder.Status.DRAFT) {
            throw new RuntimeException("Purchase Order masih dalam status DRAFT. Kirim PO ke supplier terlebih dahulu.");
        }
        if (po.getStatus() == PurchaseOrder.Status.CANCELLED) {
            throw new RuntimeException("Purchase Order sudah dibatalkan. Tidak bisa menerima barang.");
        }

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
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderItem", itemDTO.getPurchaseOrderItemId()));

            if (!poItem.getPurchaseOrder().getId().equals(po.getId())) {
                throw new RuntimeException("Akses Ditolak: PurchaseOrderItem bukan bagian dari Purchase Order ini.");
            }

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

            if (product.getPartner() == null || !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Product '" + product.getName() + "' bukan milik partner Anda.");
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

            // =============================================================
            // UPDATE STOCK BALANCE REAL-TIME
            // =============================================================
            long qtyReceivedLong = itemDTO.getQtyReceived().longValue();
            if (qtyReceivedLong > 0) {
                String locationType = po.getLocationType() != null ? po.getLocationType().toUpperCase() : null;
                Long locationId = po.getLocationId();

                if (locationType == null || locationId == null) {
                    throw new RuntimeException("Purchase Order tidak memiliki lokasi tujuan yang valid.");
                }

                StockBalance stockBalance = stockBalanceRepository
                        .findByProductIdAndLocationTypeAndLocationId(product.getId(), locationType, locationId)
                        .orElse(null);

                if (stockBalance == null) {
                    stockBalance = new StockBalance();
                    stockBalance.setProduct(product);
                    stockBalance.setLocationType(locationType);
                    stockBalance.setLocationId(locationId);
                    stockBalance.setQty(0L);
                    stockBalance.setCreatedBy(currentUser);
                    stockBalance.setCreatedAt(LocalDateTime.now());
                }

                long currentQty = stockBalance.getQty() != null ? stockBalance.getQty() : 0L;
                stockBalance.setQty(currentQty + qtyReceivedLong);
                stockBalance.setUpdatedBy(currentUser);
                stockBalance.setUpdatedAt(LocalDateTime.now());
                stockBalanceRepository.save(stockBalance);

                // =============================================================
                // CATAT STOCK MUTATION AUDIT LOG
                // =============================================================
                StockMutation mutation = new StockMutation();
                mutation.setProduct(product);
                mutation.setPartner(po.getPartner());
                mutation.setType(StockMutation.Type.PURCHASE_IN);
                mutation.setFromLocationType(null);
                mutation.setFromLocationId(null);
                mutation.setToLocationType(StockMutation.Location.valueOf(locationType));
                mutation.setToLocationId(locationId);
                mutation.setQty(qtyReceivedLong);
                mutation.setReferenceType(StockMutation.ReferenceType.PURCHASE_RECEIPT);
                mutation.setReferenceId(saved.getId());
                mutation.setNotes("Penerimaan barang dari PO #" + po.getPoNumber() + " - Receipt #" + saved.getReceiptNumber());
                mutation.setCreatedBy(currentUser);
                mutation.setCreatedAt(LocalDateTime.now());
                stockMutationRepository.save(mutation);
            }
        }

        purchaseReceiptItemRepository.saveAll(items);
        updatePoStatus(po);

        return saved;
    }

    private void updatePoStatus(PurchaseOrder po) {
        List<PurchaseOrderItems> poItems = purchaseOrderItemsRepository.findByPurchaseOrderId(po.getId());

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

    // ─── 🔄 PRIVATE GENERATOR UTILS ───────────────────────────────────────────

    private String generateReceiptNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(9999) + 1);
        return "GR-" + date + "-" + random;
    }
}