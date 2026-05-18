package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceInitRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceItemRequest;
import com.dak.spravel.dto.response.inventoryresponse.StockLocationSummaryResponse;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockBalanceService {

    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;
    private final BranchesRepository branchesRepository;
    private final WarehousesRepository warehousesRepository;
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
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses Di Tolak: Anda Bukan Super Admin");
        return user;
    }

    // =========================
    // KHUSUS ADMIN PARTNER / EMPLOYEE
    // =========================
    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private StockBalance getValidatedStockBalance(Long id, User currentUser) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));

        if (currentUser.getPartner() == null ||
                !stock.getProduct().getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock balance bukan milik partner Anda.");
        }

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Akses Di Tolak: Admin Tidak Di Perbolehkan Mengelola Stock Balances");
        }

        return stock;
    }

    // mapToResponse (Memastikan qty dipulangkan sebagai Long)
    public StockBalanceResponse mapToResponse(StockBalance stock) {
        if (stock == null) return null;

        // Product DTO
        StockBalanceResponse.ProductSimpleDto productDto = new StockBalanceResponse.ProductSimpleDto();
        productDto.setId(stock.getProduct().getId());
        productDto.setName(stock.getProduct().getName());
        productDto.setSku(stock.getProduct().getSku());

        // User DTO
        UserSimpleDto userDto = null;
        if (stock.getUpdatedBy() != null) {
            userDto = new UserSimpleDto();
            userDto.setId(stock.getUpdatedBy().getId());
            userDto.setUsername(stock.getUpdatedBy().getUsername());
        }

        return StockBalanceResponse.builder()
                .id(stock.getId())
                .product(productDto)
                .locationType(stock.getLocationType())
                .locationId(stock.getLocationId())
                .qty(stock.getQty()) 
                .updatedAt(stock.getUpdatedAt())
                .updatedBy(userDto)
                .build();
    }

    public List<StockBalanceResponse> findAllStockBalance() {
        getAuthenticatedSuperAdmin();
        return stockBalanceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockBalanceResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ALL PAGINATED
    public Page<StockBalanceResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        if (isAdmin(currentUser)) {
            return stockBalanceRepository.findAll(pageRequest).map(this::mapToResponse);
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        return stockBalanceRepository.findByProductPartnerId(
                currentUser.getPartner().getId(),
                PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // =========================
    // GET BY ID
    // =========================
    public StockBalance findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return getValidatedStockBalance(id, currentUser);
    }

    // GET BY PRODUCT
    public List<StockBalanceResponse> findByProductId(Long productId) {
        User currentUser = getAuthenticatedUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (currentUser.getPartner() == null ||
                !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        return stockBalanceRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔥 FIX 1: Ubah return type jadi DTO Response biar aman di Controller
    public List<StockBalanceResponse> findByLocation(String locationType, Long locationId) {
        getAuthenticatedUser();
        return stockBalanceRepository.findByLocationTypeAndLocationId(locationType, locationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // CREATE
    @Transactional
    public StockBalanceResponse create(StockBalanceRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        List<StockBalance> allStocks = stockBalanceRepository
                .findByProductPartnerId(currentUser.getPartner().getId());

        if (partner == null || !product.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                request.getProductId(), request.getLocationType(), request.getLocationId()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Stock balance sudah ada untuk produk dan lokasi ini.");
        });

        StockBalance stock = new StockBalance();
        stock.setProduct(product);
        stock.setLocationType(request.getLocationType());
        stock.setLocationId(request.getLocationId());
        stock.setQty(request.getQty()); // Pastikan di DTO Request lu type qty-nya juga Long ya!
        stock.setCreatedBy(currentUser);
        stock.setUpdatedBy(currentUser);

        return mapToResponse(stockBalanceRepository.save(stock));
    }

    // =========================
    // INISIASI STOCK AWAL
    // =========================
    @Transactional
    public List<StockBalance> initializeStock(StockBalanceInitRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner manapun.");
        }

        if ("BRANCH".equalsIgnoreCase(request.getLocationType())) {
            Branches branch = branchesRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan"));
            if (branch.getPartners() == null
                    || !branch.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
            }
        } else if ("WAREHOUSE".equalsIgnoreCase(request.getLocationType())) {
            Warehouses warehouse = warehousesRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan"));
            if (warehouse.getPartners() == null
                    || !warehouse.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda.");
            }
        } else {
            throw new RuntimeException("locationType tidak valid. Gunakan 'BRANCH' atau 'WAREHOUSE'.");
        }

        List<StockBalance> results = new ArrayList<>();

    @Transactional
    public void adjustStock(Long productId, String locationType, Long locationId, Long adjustment) {
        StockBalance stock = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                productId, locationType, locationId
        ).orElseGet(() -> {
            StockBalance newStock = new StockBalance();
            newStock.setProduct(productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId)));
            newStock.setLocationType(locationType);
            newStock.setLocationId(locationId);
            newStock.setQty(0L); // FIX: Isi nilai default awal dengan 0L biar gak kosongan
            return newStock;
        });

        // Pastikan kalkulasi murni menggunakan matematika primitive Long
        long currentQty = stock.getQty() != null ? stock.getQty() : 0L;
        long newQty = currentQty + adjustment;

        if (newQty < 0) {
            throw new RuntimeException("Stok tidak mencukupi untuk produk ID: " + productId);
        }
        
        stock.setQty(newQty);
        stockBalanceRepository.save(stock);
    }
}