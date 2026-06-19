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
import com.dak.spravel.repository.auth.PermissionRepository;
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
import java.util.Set;

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
    private final PermissionRepository permissionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void run() {
        // Ambil atau buat partner PT NLFTs
        Partners partner;
        if (partnerRepository.existsBySlug("pt-nlfts")) {
            log.info("[PartnerSeeder] PT NLFTs sudah ada, melanjutkan seeding data tambahan...");
            partner = partnerRepository.findBySlug("pt-nlfts")
                .orElseThrow(() -> new RuntimeException("Partner pt-nlfts tidak ditemukan"));
        } else {
            log.info("[PartnerSeeder] Mulai seeding PT NLFTs...");

            // ── 1. Partner ────────────────────────────────────────────────────────
            partner = new Partners();
            partner.setName("PT NLFTs");
            partner.setSlug("pt-nlfts");
            partner.setPlan(Partners.Plan.PRO);
            partner.setIsActive(true);
            partner = partnerRepository.save(partner);
        }

        // ── 2. Owner Partner: nairha ──────────────────────────────────────────
        User adminPartner = createUserIfNotExists(
                "nairha", "Nairha Admin NLFTs", "nairha@nlfts.co.id",
                "Bismillah00", partner, null, "owner");

        // Variabel final untuk digunakan di lambda
        final Partners partnerRef = partner;
        final User adminRef = adminPartner;

        // ── 3. Gudang Pusat ───────────────────────────────────────────────────
        Warehouses gudang = warehousesRepository
            .findByPartnersIdAndDeletedAtIsNull(partnerRef.getId())
            .stream()
            .filter(w -> w.getName().equals("Gudang Pusat NLFTs"))
            .findFirst()
            .orElseGet(() -> {
                Warehouses g = new Warehouses();
                g.setPartners(partnerRef);
                g.setName("Gudang Pusat NLFTs");
                g.setAddress("Jl. Industri Raya No. 10, Bandung");
                g.setIsActive(true);
                g.setCreatedBy(adminRef);
                return warehousesRepository.save(g);
            });

        // ── 4. Cabang Badung Pusat ────────────────────────────────────────────
        Branches cabang = branchesRepository.findByPartners(partnerRef)
            .stream()
            .filter(b -> b.getName().equals("Cabang Badung Pusat"))
            .findFirst()
            .orElseGet(() -> {
                Branches c = new Branches();
                c.setPartners(partnerRef);
                c.setName("Cabang Badung Pusat");
                c.setAddress("Jl. Raya Kuta No. 88, Badung, Bali");
                c.setIsActive(true);
                c.setCreatedBy(adminRef);
                return branchesRepository.save(c);
            });

            // ── 4d. User Cabang: davingm ──────────────────────────────────────────
        // 1. Ambil template pengelola-cabang dari seeder template
        var templateCabang = PartnerRoleTemplateSeeder.TEMPLATES.stream()
            .filter(t -> t.slug().equals("pengelola-cabang"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Template pengelola-cabang tidak ditemukan"));

        // 2. Buat role spesifik untuk partner ini berdasarkan template tersebut
        Role roleCabangAsli = createPartnerRoleIfNotExists(
            templateCabang.slug(),
            templateCabang.name(),
            partner,
            adminPartner, // Ditandai dibuat oleh admin partner (nairha)
            templateCabang.perms() // Mengambil String[] permissions dari record template
        );
        
        // 3. Daftarkan usernya menggunakan role asli yang baru dibuat
        createBranchUserIfNotExists(
            "davingm", "Davin GM Cabang NLFTs Djogja", "davingm@nlfts.co.id",
            "12345678", partner, cabang, roleCabangAsli);

        // ── 4e. Cabang NLFTs Djogja ───────────────────────────────────────────
        final Branches savedCabangDjogja = branchesRepository.findByPartners(partnerRef).stream()
            .filter(b -> b.getName().equals("Cabang NLFTs Djogja"))
            .findFirst()
            .orElseGet(() -> {
                Branches cabangDjogja = new Branches();
                cabangDjogja.setPartners(partnerRef);
                cabangDjogja.setName("Cabang NLFTs Djogja");
                cabangDjogja.setAddress("Jl. Malioboro No. 1, Yogyakarta");
                cabangDjogja.setIsActive(true);
                cabangDjogja.setCreatedBy(adminRef);
                return branchesRepository.save(cabangDjogja);
            });

        // Update user davingm ke cabang Djogja
        userRepository.findByUsername("davingm").ifPresent(u -> {
            u.setBranch(savedCabangDjogja);
            userRepository.save(u);
        });

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

        // ── 9-14. Purchase Order, Receipt, Stock — hanya jika belum ada ──────
        final Partners partnerFinal = partner;
        boolean hasPO = purchaseOrderRepository.findAll().stream()
            .anyMatch(po -> po.getPartner() != null && po.getPartner().getId().equals(partnerFinal.getId()));

        if (!hasPO) {
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
        seedStockOpname(partner, cabang, adminPartner, adminPartner, laptop, komputer, tablet);
        log.info("[PartnerSeeder] Stock Opname Cabang dibuat (APPROVED).");
        } else {
            log.info("[PartnerSeeder] PO/Stock sudah ada, skip seeding PO & stock.");
        }

        // ── 15. Stock awal Cabang NLFTs Djogja (davingm) ─────────────────────
        // Ambil cabangDjogja dari DB (sudah disimpan di step 4e)
        branchesRepository.findByPartners(partner).stream()
            .filter(b -> b.getName().equals("Cabang NLFTs Djogja"))
            .findFirst()
            .ifPresent(cabangDjogja2 -> {
                boolean hasStock = stockBalanceRepository
                    .findByLocationTypeAndLocationId("BRANCH", cabangDjogja2.getId())
                    .stream().anyMatch(sb -> sb.getProduct() != null &&
                        sb.getProduct().getPartner() != null &&
                        sb.getProduct().getPartner().getId().equals(partnerFinal.getId()));

                if (!hasStock) {
                    // Ambil produk dari DB
                    productRepository.findAllByPartner(partnerFinal).forEach(prod -> {
                        addStockBalance(prod, "BRANCH", cabangDjogja2.getId(), 8L, adminPartner);
                        createMutation(prod, partnerFinal, null, 8L,
                            "WAREHOUSE", gudang.getId(),
                            "BRANCH", cabangDjogja2.getId(),
                            StockMutation.Type.TRANSFER, StockMutation.ReferenceType.TRANSFER_REQUEST,
                            adminPartner, "Stok awal Cabang NLFTs Djogja");
                        // Kurangi dari gudang
                        addStockBalance(prod, "WAREHOUSE", gudang.getId(), -8L, adminPartner);
                    });
                    log.info("[PartnerSeeder] Stok awal Cabang NLFTs Djogja: 8 per produk.");
                }

                // ── 16. PO untuk Cabang NLFTs Djogja — status ORDERED (siap diterima davingm) ──
                boolean hasPOForDjogja = purchaseOrderRepository.findAll().stream()
                    .anyMatch(po -> po.getPartner() != null
                        && po.getPartner().getId().equals(partnerFinal.getId())
                        && "BRANCH".equalsIgnoreCase(po.getLocationType())
                        && po.getLocationId().equals(cabangDjogja2.getId())
                        && po.getStatus() == PurchaseOrder.Status.ORDERED);

                if (!hasPOForDjogja) {
                    // Ambil produk laptop untuk PO ini
                    productRepository.findAllByPartner(partnerFinal).stream()
                        .filter(p -> p.getName().equals("Laptop"))
                        .findFirst()
                        .ifPresent(laptop2 -> {
                            PurchaseOrder poDjogja = new PurchaseOrder();
                            poDjogja.setPartner(partnerFinal);
                            poDjogja.setSupplier(supplierRepository
                                .findByPartnerIdAndDeletedAtIsNull(partnerFinal.getId())
                                .stream().findFirst().orElse(null));
                            poDjogja.setPoNumber(generatePoNumber());
                            poDjogja.setLocationType("BRANCH");
                            poDjogja.setLocationId(cabangDjogja2.getId());
                            poDjogja.setStatus(PurchaseOrder.Status.ORDERED);
                            poDjogja.setOrderDate(Date.valueOf(LocalDate.now()));
                            poDjogja.setExpectedDate(Date.valueOf(LocalDate.now().plusDays(1)));
                            poDjogja.setNotes("PO uji coba — siap diterima oleh davingm di Cabang NLFTs Djogja");
                            poDjogja.setTotal(BigDecimal.ZERO);
                            poDjogja.setCreatedBy(adminPartner);
                            PurchaseOrder savedPO = purchaseOrderRepository.save(poDjogja);

                            BigDecimal qty = new BigDecimal("10");
                            BigDecimal harga = new BigDecimal("5000000");
                            PurchaseOrderItems poItem = new PurchaseOrderItems();
                            poItem.setPurchaseOrder(savedPO);
                            poItem.setProduct(laptop2);
                            poItem.setProductName(laptop2.getName());
                            poItem.setQtyOrdered(qty);
                            poItem.setQtyReceived(BigDecimal.ZERO);
                            poItem.setUnitCost(harga);
                            poItem.setSubtotal(qty.multiply(harga));
                            purchaseOrderItemsRepository.save(poItem);

                            savedPO.setTotal(qty.multiply(harga));
                            purchaseOrderRepository.save(savedPO);

                            log.info("[PartnerSeeder] PO untuk Cabang NLFTs Djogja dibuat: {} — status ORDERED, siap diterima davingm.", savedPO.getPoNumber());
                        });
                }
            });

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
        Role role = roleRepository.findBySlug(roleSlug)
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

    /**
     * Buat user cabang dengan role per-partner (bukan role global).
     */
    private User createBranchUserIfNotExists(String username, String fullname, String email,
            String rawPassword, Partners partner, Branches branch, Role role) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            log.info("[PartnerSeeder] User '{}' sudah ada, skip.", username);
            return existing.get();
        }

        User user = new User();
        user.setUsername(username);
        user.setFullname(fullname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPartner(partner);
        user.setBranch(branch);
        user.setRoles(new HashSet<>());
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    /**
     * Buat user gudang dengan role per-partner (bukan role global).
     */

    /**
     * Buat role per-partner dengan permission slugs yang diberikan.
     */
     private Role createPartnerRoleIfNotExists(String slug, String name, Partners partner,
            User createdBy, String[] permissionSlugs) {
        
        // 1. CEK DULU: Kalau role dengan slug ini sudah ada di DB, langsung kembalikan yang sudah ada
        Optional<Role> existingRole = roleRepository.findBySlug(slug); // atau findBySlugAndPartnerId jika role-nya unique per partner
        if (existingRole.isPresent()) {
            log.info("[PartnerSeeder] Role '{}' sudah ada di database, skip creation.", slug);
            return existingRole.get();
        }
        
        // 2. Kalau belum ada, baru buat baru (Kode bawaan kamu)
        log.info("[PartnerSeeder] Role '{}' belum ada, mulai membuat baru...", slug);
        Role role = new Role();
        role.setSlug(slug);
        role.setName(name);
        role.setType(Role.Type.EXTERNAL);
        role.setCreatedBy(createdBy);
        role.setCreatedAt(java.time.LocalDateTime.now());

        // Assign permissions berdasarkan slug
        Set<com.dak.spravel.model.auth.Permission> perms = new HashSet<>();
        for (String permSlug : permissionSlugs) {
            permissionRepository.findBySlug(permSlug).ifPresentOrElse(
                perms::add,
                () -> log.warn("[PartnerSeeder] Permission slug '{}' tidak ditemukan, skip.", permSlug)
            );
        }
        role.setPermissions(perms);

        Role saved = roleRepository.save(role);
        log.info("[PartnerSeeder] Role '{}' berhasil dibuat dengan {} permissions.", slug, perms.size());
        return saved;
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