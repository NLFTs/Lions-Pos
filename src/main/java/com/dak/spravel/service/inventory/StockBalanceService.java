package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceInitRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceItemRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.StockBalanceResponse;
import com.dak.spravel.dto.response.inventoryresponse.StockLocationSummaryResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.inventory.StockMutationRepository;
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
    private final StockMutationRepository stockMutationRepository;
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
        if (!isSuperAdmin) throw new RuntimeException("Akses Ditolak: Anda bukan Super Admin");
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
    // VALIDASI STOCK BALANCE
    // =========================
    private StockBalance getValidatedStockBalance(Long id, User currentUser) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));

        if (currentUser.getPartner() == null
                || stock.getProduct() == null
                || stock.getProduct().getPartner() == null
                || !stock.getProduct().getPartner().getId()
                .equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock balance bukan milik partner Anda.");
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
    // HELPER: Validasi lokasi milik partner
    // =========================
    private void validateLocation(String locationType, Long locationId, Partners partner) {
        if ("BRANCH".equalsIgnoreCase(locationType)) {
            Branches branch = branchesRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan"));
            if (branch.getPartners() == null
                    || !branch.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
            }
        } else if ("WAREHOUSE".equalsIgnoreCase(locationType)) {
            Warehouses warehouse = warehousesRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan"));
            if (warehouse.getPartners() == null
                    || !warehouse.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda.");
            }
        } else {
            throw new RuntimeException("locationType tidak valid. Gunakan 'BRANCH' atau 'WAREHOUSE'.");
        }
    }

    // =========================
    // MAP TO RESPONSE
    // =========================
    public StockBalanceResponse mapToResponse(StockBalance stock) {
        if (stock == null) return null;

        StockBalanceResponse.ProductSimpleDto productDto = new StockBalanceResponse.ProductSimpleDto();
        productDto.setId(stock.getProduct().getId());
        productDto.setName(stock.getProduct().getName());
        productDto.setSku(stock.getProduct().getSku());

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

    // =========================
    // RECORD MUTATION (otomatis saat stock in)
    // =========================
    private void recordMutation(StockBalance stock, Long qty, String type,
                                String refType, Long refId, String notes, User user) {
        StockMutation mutation = new StockMutation();
        mutation.setProduct(stock.getProduct());
        mutation.setPartner(stock.getProduct().getPartner());
        mutation.setType(StockMutation.Type.valueOf(type.toUpperCase()));
        mutation.setFromLocationType(null);
        mutation.setFromLocationId(null);
        mutation.setToLocationType(StockMutation.Location.valueOf(stock.getLocationType().toUpperCase()));
        mutation.setToLocationId(stock.getLocationId());
        mutation.setQty(qty);
        mutation.setReferenceType(StockMutation.ReferenceType.valueOf(refType.toUpperCase()));
        mutation.setReferenceId(refId);
        mutation.setNotes(notes);
        mutation.setCreatedBy(user);
        stockMutationRepository.save(mutation);
    }

    // =========================
    // FIND ALL — SUPER ADMIN
    // =========================
    public List<StockBalanceResponse> findAllStockBalance() {
        getAuthenticatedSuperAdmin();
        return stockBalanceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // FIND ALL — PARTNER / EMPLOYEE (semua stock flat)
    // =========================
    public List<StockBalanceResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // PAGINATION
    // =========================
    public Page<StockBalanceResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return stockBalanceRepository
                .findByProductPartnerId(currentUser.getPartner().getId(), pageRequest)
                .map(this::mapToResponse);
    }

    // =========================
    // SUMMARY: per product + total qty + breakdown per lokasi
    // =========================
    public List<StockLocationSummaryResponse> findStockSummary() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Long partnerId = currentUser.getPartner().getId();

        List<StockBalance> allBalances = stockBalanceRepository.findByProductPartnerId(partnerId);

        Map<Product, List<StockBalance>> groupedByProduct = allBalances.stream()
                .collect(Collectors.groupingBy(StockBalance::getProduct));

        return groupedByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<StockBalance> balances = entry.getValue();

                    BigDecimal totalQty = balances.stream()
                            .map(sb -> sb.getQty() != null ? BigDecimal.valueOf(sb.getQty()) : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<StockLocationSummaryResponse.StockPerLocation> perLokasi = balances.stream()
                            .map(sb -> new StockLocationSummaryResponse.StockPerLocation(
                                    sb.getLocationType(),
                                    sb.getLocationId(),
                                    resolveLocationName(sb.getLocationType(), sb.getLocationId()),
                                    sb.getQty() != null ? BigDecimal.valueOf(sb.getQty()) : BigDecimal.ZERO))
                            .toList();

                    return StockLocationSummaryResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sku(product.getSku())
                            .totalQty(totalQty)
                            .perLokasi(perLokasi)
                            .build();
                })
                .toList();
    }

    // =========================
    // GET BY ID
    // =========================
    public StockBalanceResponse findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return mapToResponse(getValidatedStockBalance(id, currentUser));
    }

    // =========================
    // GET BY LOKASI (branch/warehouse tertentu)
    // =========================
    public List<StockBalanceResponse> findByLocation(String locationType, Long locationId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        return stockBalanceRepository
                .findByLocationTypeAndLocationId(locationType.toUpperCase(), locationId)
                .stream()
                .filter(s -> s.getProduct() != null
                        && s.getProduct().getPartner() != null
                        && s.getProduct().getPartner().getId()
                        .equals(currentUser.getPartner().getId()))
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // GET BY BRANCH — filter hanya BRANCH
    // =========================
    public List<StockBalanceResponse> findByBranch(Long branchId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan"));
        if (branch.getPartners() == null
                || !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
        }

        return stockBalanceRepository
                .findByLocationTypeAndLocationId("BRANCH", branchId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // GET BY WAREHOUSE — filter hanya WAREHOUSE
    // =========================
    public List<StockBalanceResponse> findByWarehouse(Long warehouseId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Warehouses warehouse = warehousesRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan"));
        if (warehouse.getPartners() == null
                || !warehouse.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda.");
        }

        return stockBalanceRepository
                .findByLocationTypeAndLocationId("WAREHOUSE", warehouseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // CREATE — stock awal manual (1 produk 1 lokasi)
    // otomatis catat stock mutation
    // =========================
    @Transactional
    public StockBalanceResponse create(StockBalanceRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();

        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner manapun.");
        }

        Product product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new RuntimeException("Product tidak ditemukan"));

        if (product.getPartner() == null
                || !product.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        validateLocation(request.getLocationType(), request.getLocationId(), partner);

        stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                request.getProduct(),
                request.getLocationType().toUpperCase(),
                request.getLocationId()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Stock balance sudah ada untuk produk dan lokasi ini.");
        });

        StockBalance stock = new StockBalance();
        stock.setProduct(product);
        stock.setLocationType(request.getLocationType().toUpperCase());
        stock.setLocationId(request.getLocationId());
        stock.setQty(request.getQty());
        stock.setCreatedBy(currentUser);
        stock.setUpdatedBy(currentUser);

        StockBalance saved = stockBalanceRepository.save(stock);

        // Otomatis catat mutation sebagai ADJUSTMENT (stock awal)
        recordMutation(saved, request.getQty(), "ADJUSTMENT",
                "STOCK_OPNAME", saved.getId(),
                "Stock awal manual untuk " + product.getName(), currentUser);

        return mapToResponse(saved);
    }

    // =========================
    // INISIASI STOCK AWAL BATCH
    // otomatis catat stock mutation tiap produk
    // =========================
    @Transactional
    public List<StockBalanceResponse> initializeStock(StockBalanceInitRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner manapun.");
        }

        validateLocation(request.getLocationType(), request.getLocationId(), partner);

        List<StockBalanceResponse> results = new ArrayList<>();

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
                            request.getLocationType().toUpperCase(),
                            request.getLocationId())
                    .orElse(null);

            long oldQty = 0L;
            if (stock != null) {
                oldQty = stock.getQty() != null ? stock.getQty() : 0L;
                stock.setQty(itemRequest.getQty().longValue());
                stock.setUpdatedBy(currentUser);
                stock.setUpdatedAt(LocalDateTime.now());
            } else {
                stock = new StockBalance();
                stock.setProduct(product);
                stock.setLocationType(request.getLocationType().toUpperCase());
                stock.setLocationId(request.getLocationId());
                stock.setQty(itemRequest.getQty().longValue());
                stock.setCreatedBy(currentUser);
                stock.setUpdatedBy(currentUser);
            }

            StockBalance saved = stockBalanceRepository.save(stock);

            long qtyDiff = saved.getQty() - oldQty;
            if (qtyDiff != 0) {
                recordMutation(saved, Math.abs(qtyDiff), "ADJUSTMENT",
                        "STOCK_OPNAME", saved.getId(),
                        "Stock awal batch untuk " + product.getName(), currentUser);
            }

            results.add(mapToResponse(saved));
        }

        return results;
    }

    // =========================
    // ADJUST STOCK (dipakai OrdersService)
    // =========================
    @Transactional
    public void adjustStock(Long productId, String locationType, Long locationId, Long adjustment) {
        StockBalance stock = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(
                        productId, locationType.toUpperCase(), locationId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock tidak ditemukan untuk product id=" + productId
                                + " di " + locationType + " id=" + locationId));

        long currentQty = stock.getQty() != null ? stock.getQty() : 0L;
        long newQty = currentQty + adjustment;

        if (newQty < 0) {
            throw new RuntimeException(
                    "Stok tidak mencukupi untuk produk ID: " + productId
                            + ". Stock saat ini: " + currentQty);
        }

        stock.setQty(newQty);
        stock.setUpdatedAt(LocalDateTime.now());
        stockBalanceRepository.save(stock);
    }
}