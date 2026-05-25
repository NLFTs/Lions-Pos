package com.dak.spravel.seeder;

import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.BranchWarehouses;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.model.inventory.StockOpname;
import com.dak.spravel.model.inventory.StockOpnameItem;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.model.procurement.PurchaseOrder;
import com.dak.spravel.model.procurement.PurchaseOrderItems;
import com.dak.spravel.model.procurement.PurchaseReceipt;
import com.dak.spravel.model.procurement.PurchaseReceiptItem;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.BranchWarehousesRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.inventory.StockMutationRepository;
import com.dak.spravel.repository.inventory.StockOpnameItemRepository;
import com.dak.spravel.repository.inventory.StockOpnameRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderItemsRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderRepository;
import com.dak.spravel.repository.procurement.PurchaseReceiptItemRepository;
import com.dak.spravel.repository.procurement.PurchaseReceiptRepository;
import com.dak.spravel.repository.procurement.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartnerSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PartnerRepository partnerRepository;
    private final BranchesRepository branchesRepository;
    private final WarehousesRepository warehousesRepository;
    private final BranchWarehousesRepository branchWarehousesRepository;
    private final CategoryProductRepository categoryProductRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemsRepository purchaseOrderItemsRepository;
    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final PurchaseReceiptItemRepository purchaseReceiptItemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMutationRepository stockMutationRepository;
    private final StockOpnameRepository stockOpnameRepository;
    private final StockOpnameItemRepository stockOpnameItemRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void run() {
        if (partnerRepository.existsBySlug("pt-nlfts")) {
            log.info("[PartnerSeeder] PT NLFTs sudah ada, skip.");
            return;
        }

        log.info("[PartnerSeeder] Mulai seeding PT NLFTs...");

        // ── 1. Partner ────────────────────────────────────────────────────────
        Partners partner = new Partners();
        partner.setName("PT NLFTs");
        partner.setSlug("pt-nlfts");
        partner.setPlan(Partners.Plan.PRO);
        partner.setIsActive(true);
        partner = partnerRepository.save(partner);

        // ── 2. Owner Partner: nairha ──────────────────────────────────────────
        User adminPartner = createUserIfNotExists(
                "nairha", "Nairha Admin NLFTs", "nairha@nlfts.co.id",
                "Bismillah00", partner, null, "owner");

        // ── 3. Gudang Pusat ───────────────────────────────────────────────────
        Warehouses gudang = new Warehouses();
        gudang.setPartners(partner);
        gudang.setName("Gudang Pusat NLFTs");
        gudang.setAddress("Jl. Industri Raya No. 10, Bandung");
        gudang.setIsActive(true);
        gudang.setCreatedBy(adminPartner);
        gudang = warehousesRepository.save(gudang);

        // ── 4. Cabang Badung Pusat ────────────────────────────────────────────
        Branches cabang = new Branches();
        cabang.setPartners(partner);
        cabang.setName("Cabang Badung Pusat");
        cabang.setAddress("Jl. Raya Kuta No. 88, Badung, Bali");
        cabang.setIsActive(true);
        cabang.setCreatedBy(adminPartner);
        cabang = branchesRepository.save(cabang);

        // ── 5. Branch ↔ Warehouse mapping ────────────────────────────────────
        if (!branchWarehousesRepository.existsByBranchesAndWarehouses(cabang, gudang)) {
            BranchWarehouses bw = new BranchWarehouses();
            bw.setBranches(cabang);
            bw.setWarehouses(gudang);
            bw.setCreatedAt(LocalDateTime.now());
            bw.setCreatedBy(adminPartner);
            branchWarehousesRepository.save(bw);
        }

        // ── 5b. Kasir / Employee ──────────────────────────────────────────────

        // ── 6. Kategori: Elektronik ───────────────────────────────────────────
        CategoryProduct kategoriElektronik = createCategoryIfNotExists("Elektronik", partner, adminPartner);

        // ── 7. 3 Produk ───────────────────────────────────────────────────────
        Product laptop   = createProductIfNotExists("Laptop",        "SKU-NLFTS-LPT-001", new BigDecimal("8500000"), kategoriElektronik, partner, adminPartner);
        Product komputer = createProductIfNotExists("Komputer",      "SKU-NLFTS-KMP-001", new BigDecimal("6500000"), kategoriElektronik, partner, adminPartner);
        Product tablet   = createProductIfNotExists("Tablet Huawei", "SKU-NLFTS-TBL-001", new BigDecimal("3200000"), kategoriElektronik, partner, adminPartner);
        log.info("[PartnerSeeder] 3 Produk dibuat: Laptop, Komputer, Tablet Huawei");

        // ── 8. Supplier ───────────────────────────────────────────────────────
        Supplier supplier = createSupplierIfNotExists(partner, adminPartner);

        // ── 9. Purchase Order → Inisialisasi awal ────────────────────────────
        PurchaseOrder po = createPurchaseOrder(partner, supplier, gudang.getId(), generatePoNumber(), adminPartner);
        BigDecimal qty50 = new BigDecimal("50");
        PurchaseOrderItems poItemLaptop   = createPoItem(po, laptop,   qty50, new BigDecimal("7000000"));
        PurchaseOrderItems poItemKomputer = createPoItem(po, komputer, qty50, new BigDecimal("5500000"));
        PurchaseOrderItems poItemTablet   = createPoItem(po, tablet,   qty50, new BigDecimal("2800000"));
        
        po.setTotal(poItemLaptop.getSubtotal().add(poItemKomputer.getSubtotal()).add(poItemTablet.getSubtotal()));
        log.info("[PartnerSeeder] PO dibuat: {}", po.getPoNumber());

        // ── 10. Purchase Receipt → stok masuk ke Gudang ───────────────────────
        PurchaseReceipt receipt = createPurchaseReceipt(po, generateReceiptNumber(), adminPartner);
        createReceiptItem(receipt, poItemLaptop,   laptop,   qty50, new BigDecimal("7000000"));
        createReceiptItem(receipt, poItemKomputer, komputer, qty50, new BigDecimal("5500000"));
        createReceiptItem(receipt, poItemTablet,   tablet,   qty50, new BigDecimal("2800000"));

        poItemLaptop.setQtyReceived(qty50);
        poItemKomputer.setQtyReceived(qty50);
        poItemTablet.setQtyReceived(qty50);
        purchaseOrderItemsRepository.save(poItemLaptop);
        purchaseOrderItemsRepository.save(poItemKomputer);
        purchaseOrderItemsRepository.save(poItemTablet);

        po.setStatus(PurchaseOrder.Status.RECEIVED);
        po.setUpdatedAt(LocalDateTime.now());
        po.setUpdatedBy(adminPartner);
        purchaseOrderRepository.save(po);
        log.info("[PartnerSeeder] Receipt dibuat, PO → RECEIVED");

        // ── 11. Stock Balance GUDANG: +50 per produk ──────────────────────────
        addStockBalance(laptop,   "WAREHOUSE", gudang.getId(), 50L, adminPartner);
        addStockBalance(komputer, "WAREHOUSE", gudang.getId(), 50L, adminPartner);
        addStockBalance(tablet,   "WAREHOUSE", gudang.getId(), 50L, adminPartner);

        // ── 12. Stock Mutation PURCHASE_IN ke Gudang ──────────────────────────
        createMutation(laptop,   partner, receipt.getId(), 50L, null, null, "WAREHOUSE", gudang.getId(), StockMutation.Type.PURCHASE_IN,  StockMutation.ReferenceType.PURCHASE_RECEIPT, adminPartner, "Stok masuk dari PO ke gudang");
        createMutation(komputer, partner, receipt.getId(), 50L, null, null, "WAREHOUSE", gudang.getId(), StockMutation.Type.PURCHASE_IN,  StockMutation.ReferenceType.PURCHASE_RECEIPT, adminPartner, "Stok masuk dari PO ke gudang");
        createMutation(tablet,   partner, receipt.getId(), 50L, null, null, "WAREHOUSE", gudang.getId(), StockMutation.Type.PURCHASE_IN,  StockMutation.ReferenceType.PURCHASE_RECEIPT, adminPartner, "Stok masuk dari PO ke gudang");
        log.info("[PartnerSeeder] Gudang +50 per produk. Total gudang: 50 masing-masing.");

        // ── 13. Transfer Gudang → Cabang: 10 unit per produk ─────────────────
        addStockBalance(laptop,   "WAREHOUSE", gudang.getId(), -10L, adminPartner);
        addStockBalance(komputer, "WAREHOUSE", gudang.getId(), -10L, adminPartner);
        addStockBalance(tablet,   "WAREHOUSE", gudang.getId(), -10L, adminPartner);

        addStockBalance(laptop,   "BRANCH", cabang.getId(), 10L, adminPartner);
        addStockBalance(komputer, "BRANCH", cabang.getId(), 10L, adminPartner);
        addStockBalance(tablet,   "BRANCH", cabang.getId(), 10L, adminPartner);

        createMutation(laptop,   partner, null, 10L, "WAREHOUSE", gudang.getId(), "BRANCH", cabang.getId(), StockMutation.Type.TRANSFER, StockMutation.ReferenceType.TRANSFER_REQUEST, adminPartner, "Transfer awal gudang → Cabang Badung Pusat");
        createMutation(komputer, partner, null, 10L, "WAREHOUSE", gudang.getId(), "BRANCH", cabang.getId(), StockMutation.Type.TRANSFER, StockMutation.ReferenceType.TRANSFER_REQUEST, adminPartner, "Transfer awal gudang → Cabang Badung Pusat");
        createMutation(tablet,   partner, null, 10L, "WAREHOUSE", gudang.getId(), "BRANCH", cabang.getId(), StockMutation.Type.TRANSFER, StockMutation.ReferenceType.TRANSFER_REQUEST, adminPartner, "Transfer awal gudang → Cabang Badung Pusat");
        log.info("[PartnerSeeder] Transfer selesai. Gudang: 40 | Cabang: 10 per produk.");

        // ── 14. Stock Opname Cabang (APPROVED) ────────────────────────────────
        // 💡 Diubah: Aktor pembuat Opname dialihkan penuh ke adminPartner (nairha)
        seedStockOpname(partner, cabang, adminPartner, adminPartner, laptop, komputer, tablet);
        log.info("[PartnerSeeder] Stock Opname Cabang dibuat (APPROVED).");

        log.info("[PartnerSeeder] ✅ Seeding selesai!");
    }

    private void seedStockOpname(Partners partner, Branches cabang, User creator,
                                 User adminPartner, Product laptop, Product komputer, Product tablet) {
        StockOpname opname = new StockOpname();
        opname.setPartner(partner);
        opname.setLocation("BRANCH");
        opname.setLocationId(cabang.getId());
        opname.setDate(LocalDateTime.now().minusDays(1));
        opname.setStatus(StockOpname.Status.APPROVED);
        opname.setNotes("Opname rutin bulanan — Cabang Badung Pusat");
        opname.setReviewedBy(adminPartner);
        opname.setReviewedAt(LocalDateTime.now().minusHours(3));
        opname.setApprovedBy(adminPartner);
        opname.setApprovedAt(LocalDateTime.now().minusHours(1));
        opname.setCreatedBy(creator);
        opname.setUpdatedBy(adminPartner);
        opname = stockOpnameRepository.save(opname);

        createOpnameItem(opname, laptop,   10L, 9L,  creator, "1 unit rusak saat pengiriman");
        createOpnameItem(opname, komputer, 10L, 10L, creator, "Sesuai ✓");
        createOpnameItem(opname, tablet,   10L, 11L, creator, "Surplus — kemungkinan salah input transfer");

        adjustStockBalance(laptop, "BRANCH", cabang.getId(), -1L, adminPartner);
        adjustStockBalance(tablet, "BRANCH", cabang.getId(), +1L, adminPartner);

        createMutation(laptop, partner, opname.getId(), 1L, null, null, "BRANCH", cabang.getId(), StockMutation.Type.ADJUSTMENT, StockMutation.ReferenceType.STOCK_OPNAME, adminPartner, "Koreksi opname: -1 Laptop (rusak)");
        createMutation(tablet, partner, opname.getId(), 1L, null, null, "BRANCH", cabang.getId(), StockMutation.Type.ADJUSTMENT, StockMutation.ReferenceType.STOCK_OPNAME, adminPartner, "Koreksi opname: +1 Tablet (surplus)");
    }

    private void createOpnameItem(StockOpname opname, Product product, long qtySystem, long qtyPhysical, User countedBy, String notes) {
        StockOpnameItem item = new StockOpnameItem();
        item.setStockOpname(opname);
        item.setProduct(product);
        item.setQtySystem(qtySystem);
        item.setQtyPhysical(qtyPhysical);
        item.setQtyDifference(qtyPhysical - qtySystem);
        item.setNotes(notes);
        item.setCountedBy(countedBy);
        item.setCountedAt(LocalDateTime.now().minusDays(1));
        stockOpnameItemRepository.save(item);
    }

    private void adjustStockBalance(Product product, String locationType, Long locationId, long adjustment, User updatedBy) {
        stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(product.getId(), locationType, locationId)
                .ifPresent(sb -> {
                    sb.setQty(sb.getQty() + adjustment);
                    sb.setUpdatedBy(updatedBy);
                    stockBalanceRepository.save(sb);
                });
    }

    private User createUserIfNotExists(String username, String fullname, String email,
            String rawPassword, Partners partner, Branches branch, String roleSlug) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            log.info("[PartnerSeeder] User '{}' sudah ada, skip.", username);
            return existing.get();
        }
        
        // Cari role global (partner_id = null) dengan slug yang sesuai
        Role role = roleRepository.findAll().stream()
                .filter(r -> r.getSlug().equals(roleSlug) && r.getPartner() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("[PartnerSeeder] Role '" + roleSlug + "' global tidak ditemukan. Pastikan PermissionSeeder sudah dijalankan."));

        User user = new User();
        user.setUsername(username);
        user.setFullname(fullname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPartner(partner);
        user.setBranch(branch);
        
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    private CategoryProduct createCategoryIfNotExists(String name, Partners partner, User createdBy) {
        if (categoryProductRepository.existsByNameAndPartnerId(name, partner.getId())) {
            return categoryProductRepository.findAllByPartner(partner, org.springframework.data.domain.Sort.by("id"))
                    .stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
        }
        CategoryProduct cat = new CategoryProduct();
        cat.setPartner(partner);
        cat.setName(name);
        cat.setDescription("Kategori produk elektronik");
        cat.setSortOrder(1);
        cat.setCreatedBy(createdBy);
        return categoryProductRepository.save(cat);
    }

    private Product createProductIfNotExists(String name, String sku, BigDecimal basePrice, CategoryProduct category, Partners partner, User createdBy) {
        if (productRepository.existsBySkuAndPartnerId(sku, partner.getId())) {
            return productRepository.findAllByPartner(partner).stream()
                    .filter(p -> p.getSku().equals(sku)).findFirst().orElseThrow();
        }
        Product product = new Product();
        product.setPartner(partner);
        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setBasePrice(basePrice);
        product.setTrackStock(true);
        product.setIsActive(true);
        product.setCreatedBy(createdBy);
        return productRepository.save(product);
    }

    private Supplier createSupplierIfNotExists(Partners partner, User createdBy) {
        if (supplierRepository.existsByNameAndPartnerIdAndDeletedAtIsNull("PT Supplier Bandung Elektronika", partner.getId())) {
            return supplierRepository.findByPartnerIdAndDeletedAtIsNull(partner.getId()).stream()
                    .filter(s -> s.getName().equals("PT Supplier Bandung Elektronika")).findFirst().orElseThrow();
        }
        Supplier supplier = new Supplier();
        supplier.setPartner(partner);
        supplier.setName("PT Supplier Bandung Elektronika");
        supplier.setPhone("022-5551234");
        supplier.setEmail("supplier@bandungelektronika.co.id");
        supplier.setAddress("Jl. Elektronika Raya No. 45, Bandung, Jawa Barat");
        supplier.setNotes("Supplier utama elektronik untuk PT NLFTs");
        supplier.setCreatedBy(createdBy);
        return supplierRepository.save(supplier);
    }

    private PurchaseOrder createPurchaseOrder(Partners partner, Supplier supplier, Long warehouseId, String poNumber, User createdBy) {
        PurchaseOrder po = new PurchaseOrder();
        po.setPartner(partner);
        po.setSupplier(supplier);
        po.setPoNumber(poNumber);
        po.setLocationType("WAREHOUSE");
        po.setLocationId(warehouseId);
        po.setStatus(PurchaseOrder.Status.ORDERED);
        po.setOrderDate(Date.valueOf(LocalDate.now()));
        po.setExpectedDate(Date.valueOf(LocalDate.now().plusDays(7)));
        po.setNotes("PO awal stok elektronik PT NLFTs — 50 unit per produk");
        po.setTotal(BigDecimal.ZERO);
        po.setCreatedBy(createdBy);
        return purchaseOrderRepository.save(po);
    }

    private PurchaseOrderItems createPoItem(PurchaseOrder po, Product product, BigDecimal qty, BigDecimal unitCost) {
        PurchaseOrderItems item = new PurchaseOrderItems();
        item.setPurchaseOrder(po);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setQtyOrdered(qty);
        item.setQtyReceived(BigDecimal.ZERO);
        item.setUnitCost(unitCost);
        item.setSubtotal(qty.multiply(unitCost));
        return purchaseOrderItemsRepository.save(item);
    }

    private PurchaseReceipt createPurchaseReceipt(PurchaseOrder po, String receiptNumber, User createdBy) {
        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setPurchaseOrder(po);
        receipt.setReceiptNumber(receiptNumber);
        receipt.setReceivedDate(LocalDateTime.now());
        receipt.setNotes("Penerimaan barang lengkap — semua 50 unit diterima");
        receipt.setCreatedBy(createdBy);
        return purchaseReceiptRepository.save(receipt);
    }

    private void createReceiptItem(PurchaseReceipt receipt, PurchaseOrderItems poItem, Product product, BigDecimal qtyReceived, BigDecimal unitCost) {
        PurchaseReceiptItem item = new PurchaseReceiptItem();
        item.setPurchaseReceipt(receipt);
        item.setPurchaseOrderItem(poItem);
        item.setProduct(product);
        item.setQtyReceived(qtyReceived);
        item.setUnitCost(unitCost);
        item.setNotes("Diterima lengkap");
        purchaseReceiptItemRepository.save(item);
    }

    private void addStockBalance(Product product, String locationType, Long locationId, Long qty, User createdBy) {
        Optional<StockBalance> existing = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(product.getId(), locationType, locationId);
        if (existing.isPresent()) {
            StockBalance sb = existing.get();
            sb.setQty(sb.getQty() + qty);
            sb.setUpdatedBy(createdBy);
            stockBalanceRepository.save(sb);
        } else {
            StockBalance sb = new StockBalance();
            sb.setProduct(product);
            sb.setLocationType(locationType);
            sb.setLocationId(locationId);
            sb.setQty(qty);
            sb.setCreatedBy(createdBy);
            stockBalanceRepository.save(sb);
        }
    }

    private void createMutation(Product product, Partners partner, Long referenceId, Long qty, String fromLocationType, Long fromLocationId, String toLocationType, Long toLocationId, StockMutation.Type type, StockMutation.ReferenceType referenceType, User createdBy, String notes) {
        StockMutation mutation = new StockMutation();
        mutation.setProduct(product);
        mutation.setPartner(partner);
        mutation.setType(type);
        mutation.setFromLocationType(fromLocationType != null ? StockMutation.Location.valueOf(fromLocationType) : null);
        mutation.setFromLocationId(fromLocationId);
        mutation.setToLocationType(StockMutation.Location.valueOf(toLocationType));
        mutation.setToLocationId(toLocationId);
        mutation.setQty(qty);
        mutation.setReferenceType(referenceType);
        mutation.setReferenceId(referenceId);
        mutation.setNotes(notes);
        mutation.setCreatedBy(createdBy);
        stockMutationRepository.save(mutation);
    }

    private String generatePoNumber() {
        String date = LocalDate.now().toString().replace("-", "");
        String prefix = "PO-" + date + "-";
        long count = purchaseOrderRepository.countByPoNumberStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    private String generateReceiptNumber() {
        String date = LocalDate.now().toString().replace("-", "");
        return "GR-" + date + "-" + String.format("%04d", (int) (Math.random() * 9000) + 1000);
    }
}