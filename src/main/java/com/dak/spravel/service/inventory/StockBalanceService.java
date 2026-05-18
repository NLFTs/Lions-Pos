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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        role.getSlug().equalsIgnoreCase("employee"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    // =========================
    // VALIDASI STOCK BALANCE
    // =========================
    private StockBalance getValidatedStockBalance(Long id, User currentUser) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock Balance tidak ditemukan"));

        if (currentUser.getPartner() == null
                || stock.getProduct() == null
                || stock.getProduct().getPartner() == null
                || !stock.getProduct().getPartner().getId()
                .equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock Balance bukan milik partner Anda.");
        }

        return stock;
    }

    // =========================
    // HELPER: Resolve nama lokasi
    // =========================
    private String resolveLocationName(String locationType, Long locationId) {
        if ("BRANCH".equalsIgnoreCase(locationType)) {
            return branchesRepository.findById(locationId)
                    .map(Branches::getName)
                    .orElse("Branch #" + locationId);
        } else if ("WAREHOUSE".equalsIgnoreCase(locationType)) {
            return warehousesRepository.findById(locationId)
                    .map(Warehouses::getName)
                    .orElse("Warehouse #" + locationId);
        }
        return "Lokasi #" + locationId;
    }

    // =========================
    // ADJUST STOCK
    // Dipakai oleh OrdersService saat terjadi transaksi
    // qty bisa positif (tambah) atau negatif (kurang)
    // =========================
    @Transactional
    public void adjustStock(Long productId, String locationType, Long locationId, BigDecimal qty) {
        StockBalance stock = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(
                        productId,
                        locationType.toUpperCase(),
                        locationId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock tidak ditemukan untuk product id=" + productId
                                + " di " + locationType + " id=" + locationId));

        BigDecimal newQty = stock.getQty().add(qty);

        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Stock tidak mencukupi untuk product id=" + productId
                            + ". Stock saat ini: " + stock.getQty());
        }

        stock.setQty(newQty);
        stock.setUpdatedAt(LocalDateTime.now());
        stockBalanceRepository.save(stock);
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<StockBalance> findAllStockBalances() {
        getAuthenticatedSuperAdmin();
        return stockBalanceRepository.findAll();
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<StockBalance> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId());
    }

    // =========================
    // PAGINATION KHUSUS PARTNER
    // =========================
    public Page<StockBalance> findAll(int page, int size) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

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

    // =========================
    // GET BY LOKASI (branch/warehouse tertentu)
    // =========================
    public List<StockBalance> findByLocation(String locationType, Long locationId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        return stockBalanceRepository
                .findByLocationTypeAndLocationId(locationType, locationId)
                .stream()
                .filter(s -> s.getProduct() != null
                        && s.getProduct().getPartner() != null
                        && s.getProduct().getPartner().getId()
                        .equals(currentUser.getPartner().getId()))
                .toList();
    }

    // =========================
    // SUMMARY: Semua stock per product
    // dikelompokkan per lokasi (branch + warehouse)
    // beserta total qty keseluruhan
    // =========================
    public List<StockLocationSummaryResponse> findStockSummary() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        List<StockBalance> allStocks = stockBalanceRepository
                .findByProductPartnerId(currentUser.getPartner().getId());

        Map<Long, List<StockBalance>> groupedByProduct = allStocks.stream()
                .collect(Collectors.groupingBy(s -> s.getProduct().getId()));

        List<StockLocationSummaryResponse> result = new ArrayList<>();

        for (Map.Entry<Long, List<StockBalance>> entry : groupedByProduct.entrySet()) {
            List<StockBalance> stocks = entry.getValue();
            Product product = stocks.get(0).getProduct();

            BigDecimal totalQty = stocks.stream()
                    .map(StockBalance::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<StockLocationSummaryResponse.StockPerLocation> perLokasi = stocks.stream()
                    .map(s -> new StockLocationSummaryResponse.StockPerLocation(
                            s.getLocationType(),
                            s.getLocationId(),
                            resolveLocationName(s.getLocationType(), s.getLocationId()),
                            s.getQty()))
                    .toList();

            result.add(new StockLocationSummaryResponse(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    totalQty,
                    perLokasi));
        }

        return result;
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

        for (StockBalanceItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product tidak ditemukan: id=" + itemRequest.getProductId()));

            if (product.getPartner() == null
                    || !product.getPartner().getId().equals(partner.getId())) {
                throw new RuntimeException(
                        "Akses Ditolak: Product '" + product.getName() + "' bukan milik partner Anda.");
            }

            StockBalance stock = stockBalanceRepository
                    .findByProductIdAndLocationTypeAndLocationId(
                            product.getId(),
                            request.getLocationType(),
                            request.getLocationId())
                    .orElse(null);

            if (stock != null) {
                stock.setQty(itemRequest.getQty());
                stock.setUpdatedBy(currentUser);
                stock.setUpdatedAt(LocalDateTime.now());
            } else {
                stock = new StockBalance();
                stock.setProduct(product);
                stock.setLocationType(request.getLocationType().toUpperCase());
                stock.setLocationId(request.getLocationId());
                stock.setQty(itemRequest.getQty());
                stock.setCreatedBy(currentUser);
            }

            results.add(stockBalanceRepository.save(stock));
        }

        return results;
    }
}