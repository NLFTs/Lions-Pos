package com.dak.spravel.seeder;

import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;


@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataSeeder {

    private final PartnerRepository  partnerRepository;
    private final BranchesRepository branchesRepository;
    private final CategoryProductRepository categoryProductRepository;
    private final ProductRepository  productRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final UserRepository     userRepository;
    private final RoleRepository     roleRepository;

    @Transactional
    public void run() {
        if (partnerRepository.existsBySlug("pt-test-dummy")) {
            log.info("[DummyData] Data dummy sudah ada, skip seeding.");
            return;
        }

        Partners partner = new Partners();
        partner.setName("PT Test Dummy");
        partner.setSlug("pt-test-dummy");
        partner.setPlan(Partners.Plan.BASIC);
        partner.setIsActive(true);
        partner.setCreatedAt(LocalDateTime.now());
        partnerRepository.save(partner);

        Branches branch = new Branches();
        branch.setPartners(partner);
        branch.setName("Cabang Test Dummy");
        branch.setAddress("Jl. Test Dummy No. 1");
        branch.setIsActive(true);
        branch.setCreatedAt(LocalDateTime.now());
        branchesRepository.save(branch);

        CategoryProduct category = new CategoryProduct();
        category.setPartner(partner);
        category.setName("Kategori Test Dummy");
        category.setDescription("Kategori untuk produk dummy testing");
        category.setSortOrder(1);
        category.setCreatedAt(LocalDateTime.now());
        categoryProductRepository.save(category);

        Product product = new Product();
        product.setPartner(partner);
        product.setCategory(category);
        product.setName("Produk Dummy Kasir");
        product.setSku("DUMMY-001");
        product.setBasePrice(new BigDecimal("15000"));
        product.setTrackStock(true);
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());
        productRepository.save(product);

        StockBalance stock = new StockBalance();
        stock.setProduct(product);
        stock.setLocationType("BRANCH");
        stock.setLocationId(branch.getId());
        stock.setQty(100L);
        stock.setCreatedAt(LocalDateTime.now());
        stockBalanceRepository.save(stock);

        Role karyawanRole = roleRepository.findBySlug("karyawan-cabang")
                .orElseThrow(() -> new RuntimeException("Role 'karyawan-cabang' belum di-seed. Jalankan PermissionSeeder terlebih dahulu."));

        Role ownerRole = roleRepository.findBySlug("owner")
                .orElseThrow(() -> new RuntimeException("Role 'owner' belum di-seed. Jalankan PermissionSeeder terlebih dahulu."));

        User kasir = new User();
        kasir.setUsername("kasir_test");
        kasir.setFullname("Kasir Test Dummy");
        kasir.setEmail("kasir_test@testdummy.local");
        kasir.setPassword(new BCryptPasswordEncoder().encode("test1234"));
        kasir.setPartner(partner);
        kasir.setBranch(branch);
        java.util.Set<Role> roles = new HashSet<>();
        roles.add(karyawanRole);
        kasir.setRoles(roles);
        kasir.setIsActive(true);
        kasir.setCreatedAt(LocalDateTime.now());
        userRepository.save(kasir);

        User adminPartner = new User();
        adminPartner.setUsername("admin_test");
        adminPartner.setFullname("Admin Partner Test Dummy");
        adminPartner.setEmail("admin_test@testdummy.local");
        adminPartner.setPassword(new BCryptPasswordEncoder().encode("test1234"));
        adminPartner.setPartner(partner);
        adminPartner.setBranch(null);
        java.util.Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(ownerRole);
        adminPartner.setRoles(adminRoles);
        adminPartner.setIsActive(true);
        adminPartner.setCreatedAt(LocalDateTime.now());
        userRepository.save(adminPartner);

        log.info("[DummyData] ✅ Data dummy berhasil di-seed:");
        log.info("  - Partner: {} (slug={})", partner.getName(), partner.getSlug());
        log.info("  - Branch: {} (id={})", branch.getName(), branch.getId());
        log.info("  - Category: {} (id={})", category.getName(), category.getId());
        log.info("  - Product: {} (sku={}, basePrice={}, id={})", product.getName(), product.getSku(), product.getBasePrice(), product.getId());
        log.info("  - Stock: BRANCH id={} qty=100", branch.getId());
        log.info("  - Kasir: {} (password=test1234, role={})", kasir.getUsername(), karyawanRole.getSlug());
        log.info("  - Admin Partner: {} (password=test1234, role={})", adminPartner.getUsername(), ownerRole.getSlug());
    }
}
