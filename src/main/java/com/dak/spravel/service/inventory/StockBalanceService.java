package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.BranchStockInRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceInitRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceItemRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.dto.request.inventory.StockTransferRequest;
import com.dak.spravel.dto.request.inventory.WarehouseStockInRequest;
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
import org.springframework.data.domain.Pageable;
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

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // 🔥 KUNCI UTAMA KITA: Cek permission dinamis, bebas dari hardcode nama role!
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Raja Super Admin (partner null) bypass semua jenis permission sistem
        if (user.getPartner() == null) {
            return;
        }

        // Flatten kumpulin semua permission dari role apa saja yang nempel di user ini
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
            throw new RuntimeException("Akses ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── 🛡️ MULTI-TENANT GUARD ────────────────────────────────────────

    private StockBalance getValidatedStockBalance(Long id, User currentUser) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));

        if (currentUser.getPartner() == null) {
            return stock;
        }

        if (stock.getProduct() == null || stock.getProduct().getPartner() == null || 
                !stock.getProduct().getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock balance bukan milik partner Anda.");
        }

        return stock;
    }

    private void validateLocation(String locationType, Long locationId, Partners partner) {
        if (partner == null) return; 

        if ("BRANCH".equalsIgnoreCase(locationType)) {
            Branches branch = branchesRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan"));
            if (branch.getPartners() == null || !branch.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
            }
        } else if ("WAREHOUSE".equalsIgnoreCase(locationType)) {
            Warehouses warehouse = warehousesRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan"));
            if (warehouse.getPartners() == null || !warehouse.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda.");
            }
        } else {
            throw new RuntimeException("locationType tidak valid. Gunakan 'BRANCH' or 'WAREHOUSE'.");
        }
    }

    private String resolveLocationName(String locationType, Long locationId) {
        if ("BRANCH".equalsIgnoreCase(locationType)) {
            return branchesRepository.findById(locationId).map(Branches::getName).orElse("Branch #" + locationId);
        } else if ("WAREHOUSE".equalsIgnoreCase(locationType)) {
            return warehousesRepository.findById(locationId).map(Warehouses::getName).orElse("Warehouse #" + locationId);
        }
        return "Lokasi #" + locationId;
    }

    // ─── 🔄 MAPPERS & MUTATION LOGS ───────────────────────────────────────────

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

    private void recordMutation(StockBalance stock, Long qty, String type, String refType, Long refId, String notes, User user) {
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

    // ─── 🚀 METHODS CORE (PERMISSON SIKAT HABIS ROLE VALIDATION) ────────────────

    public List<StockBalanceResponse> findAllStockBalance() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return stockBalanceRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public List<StockBalanceResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.index"); // 💡 Cukup cek permission slug

        if (currentUser.getPartner() == null) {
            return stockBalanceRepository.findAll().stream().map(this::mapToResponse).toList();
        }
        return stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId()).stream()
                .map(this::mapToResponse).toList();
    }

    public Page<StockBalanceResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.index"); // 💡 Cukup cek permission slug

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (currentUser.getPartner() == null) {
            return stockBalanceRepository.findAll(pageable).map(this::mapToResponse);
        }
        return stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId(), pageable)
                .map(this::mapToResponse);
    }

    public List<StockLocationSummaryResponse> findStockSummary() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.index");

        List<StockBalance> allBalances;
        if (currentUser.getPartner() == null) {
            allBalances = stockBalanceRepository.findAll();
        } else {
            allBalances = stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId());
        }

        Map<Product, List<StockBalance>> groupedByProduct = allBalances.stream()
                .filter(sb -> sb.getProduct() != null)
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

    public StockBalanceResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.show");
        return mapToResponse(getValidatedStockBalance(id, currentUser));
    }

    public List<StockBalanceResponse> findByLocation(String locationType, Long locationId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.index");

        List<StockBalance> queryResults = stockBalanceRepository.findByLocationTypeAndLocationId(locationType.toUpperCase(), locationId);
        if (currentUser.getPartner() == null) {
            return queryResults.stream().map(this::mapToResponse).toList();
        }

        return queryResults.stream()
                .filter(s -> s.getProduct() != null && s.getProduct().getPartner() != null && 
                             s.getProduct().getPartner().getId().equals(currentUser.getPartner().getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockBalanceResponse> findByBranch(Long branchId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.index");
        validateLocation("BRANCH", branchId, currentUser.getPartner());

        return stockBalanceRepository.findByLocationTypeAndLocationId("BRANCH", branchId).stream()
                .map(this::mapToResponse).toList();
    }

    public List<StockBalanceResponse> findByWarehouse(Long warehouseId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.index");
        validateLocation("WAREHOUSE", warehouseId, currentUser.getPartner());

        return stockBalanceRepository.findByLocationTypeAndLocationId("WAREHOUSE", warehouseId).stream()
                .map(this::mapToResponse).toList();
    }

    // ─── 🛠️ MUTATION ACTIONS (MAKIN SAKTI TANPA LOCK ROLE) ─────────────────────

    @Transactional
    public StockBalanceResponse create(StockBalanceRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.store"); // 💡 Siapapun boleh create asal punya permission ini!

        Partners partner = currentUser.getPartner();
        Product product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new RuntimeException("Product tidak ditemukan"));

        if (partner != null && (product.getPartner() == null || !product.getPartner().getId().equals(partner.getId()))) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        validateLocation(request.getLocationType(), request.getLocationId(), partner);

        stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                request.getProduct(), request.getLocationType().toUpperCase(), request.getLocationId()
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
        stock.setUpdatedAt(LocalDateTime.now());

        StockBalance saved = stockBalanceRepository.save(stock);
        recordMutation(saved, request.getQty(), "ADJUSTMENT", "STOCK_OPNAME", saved.getId(),
                "Stock awal manual untuk " + product.getName(), currentUser);

        return mapToResponse(saved);
    }

    @Transactional
    public List<StockBalanceResponse> initializeStock(StockBalanceInitRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_balance.store"); // 💡 Hilang sudah barier kaku employee!

        Partners partner = currentUser.getPartner();
        validateLocation(request.getLocationType(), request.getLocationId(), partner);
        List<StockBalanceResponse> results = new ArrayList<>();

        for (StockBalanceItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product tidak ditemukan: id=" + itemRequest.getProductId()));

            if (partner != null && (product.getPartner() == null || !product.getPartner().getId().equals(partner.getId()))) {
                throw new RuntimeException("Akses Ditolak: Product '" + product.getName() + "' bukan milik partner Anda.");
            }

            StockBalance stock = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                    product.getId(), request.getLocationType().toUpperCase(), request.getLocationId()
            ).orElse(null);

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
                stock.setUpdatedAt(LocalDateTime.now());
            }

            StockBalance saved = stockBalanceRepository.save(stock);
            long qtyDiff = saved.getQty() - oldQty;
            
            if (qtyDiff != 0) {
                recordMutation(saved, Math.abs(qtyDiff), "ADJUSTMENT", "STOCK_OPNAME", saved.getId(),
                        "Stock awal batch untuk " + product.getName(), currentUser);
            }
            results.add(mapToResponse(saved));
        }
        return results;
    }

    @Transactional
    public void adjustStock(Long productId, String locationType, Long locationId, Long adjustment) {
        // Method internal/sistem (biasanya dipicu dari transaksi kasir/opname yang udah lolos auth controller)
        StockBalance stock = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                productId, locationType.toUpperCase(), locationId
        ).orElseThrow(() -> new RuntimeException("Stock tidak ditemukan untuk product id=" + productId));

        long currentQty = stock.getQty() != null ? stock.getQty() : 0L;
        long newQty = currentQty + adjustment;

        if (newQty < 0) {
            throw new RuntimeException("Stok tidak mencukupi untuk produk ID: " + productId + ". Sisa: " + currentQty);
        }

        stock.setQty(newQty);
        stock.setUpdatedAt(LocalDateTime.now());
        stockBalanceRepository.save(stock);
    }

    @Transactional
    public StockBalanceResponse createFromWarehouse(WarehouseStockInRequest request) {
        Warehouses warehouse = warehousesRepository.findById(request.getWarehouseId()).orElseThrow();
        Product product = productRepository.findById(request.getProductId()).orElseThrow();
        checkPermission(getAuthenticatedUser(), "stock_balance.store");

        User currentUser = getAuthenticatedUser();
        StockBalance stock = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                request.getProductId(), "WAREHOUSE", request.getWarehouseId()
        ).orElse(new StockBalance());

        if (stock.getId() == null) {
            stock.setProduct(product);
            stock.setLocationType("WAREHOUSE");
            stock.setLocationId(warehouse.getId());
            stock.setQty(request.getQty());
            stock.setCreatedBy(currentUser);
        } else {
            stock.setQty(stock.getQty() + request.getQty());
        }

        stock.setUpdatedBy(currentUser);
        stock.setUpdatedAt(LocalDateTime.now());
        StockBalance stockBalance = stockBalanceRepository.save(stock);

        StockMutation stockMutation = new StockMutation();
        stockMutation.setProduct(stockBalance.getProduct());
        stockMutation.setQty(request.getQty()); 
        stockMutation.setPartner(stockBalance.getProduct().getPartner());
        stockMutation.setReferenceType(StockMutation.ReferenceType.PURCHASE_RECEIPT);
        stockMutation.setType(StockMutation.Type.PURCHASE_IN);
        stockMutation.setToLocationType(StockMutation.Location.valueOf(stockBalance.getLocationType()));
        stockMutation.setToLocationId(stockBalance.getLocationId());
        stockMutation.setCreatedBy(currentUser);
        stockMutation.setCreatedAt(LocalDateTime.now());
        stockMutationRepository.save(stockMutation);

        return mapToResponse(stockBalance);
    }

    @Transactional
    public StockBalanceResponse createFromBranch(BranchStockInRequest request) {       
        Branches branch = branchesRepository.findById(request.getBranchId()).orElseThrow();
        Product product = productRepository.findById(request.getProductId()).orElseThrow();
        checkPermission(getAuthenticatedUser(), "stock_balance.store");

        User currentUser = getAuthenticatedUser();
        StockBalance stock = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                request.getProductId(), "BRANCH", request.getBranchId()
        ).orElse(new StockBalance());

        if (stock.getId() == null) {
            stock.setProduct(product);
            stock.setLocationType("BRANCH");
            stock.setLocationId(branch.getId());
            stock.setQty(request.getQty());
            stock.setCreatedBy(currentUser);
        } else {
            stock.setQty(stock.getQty() + request.getQty());
        }

        stock.setUpdatedBy(currentUser);
        stock.setUpdatedAt(LocalDateTime.now());
        StockBalance stockBalance = stockBalanceRepository.save(stock);

        StockMutation stockMutation = new StockMutation();
        stockMutation.setProduct(stockBalance.getProduct());
        stockMutation.setQty(request.getQty()); 
        stockMutation.setPartner(stockBalance.getProduct().getPartner());        
        stockMutation.setReferenceType(StockMutation.ReferenceType.PURCHASE_RECEIPT);
        stockMutation.setType(StockMutation.Type.PURCHASE_IN);
        stockMutation.setToLocationType(StockMutation.Location.valueOf(stockBalance.getLocationType()));
        stockMutation.setToLocationId(stockBalance.getLocationId());
        stockMutation.setCreatedBy(currentUser);
        stockMutation.setCreatedAt(LocalDateTime.now());
        stockMutationRepository.save(stockMutation);

        return mapToResponse(stockBalance);
    }

    @Transactional
    public StockBalanceResponse transferStock(StockTransferRequest request) {
        Long productId = request.getProductId();
        Long qty = request.getQty();
        checkPermission(getAuthenticatedUser(), "stock_balance.transfer");
    
        if (request.getFromLocationType().equalsIgnoreCase(request.getToLocationType()) && 
                request.getFromLocationId().equals(request.getToLocationId())) {
            throw new RuntimeException("Lokasi asal dan tujuan tidak boleh sama persis!");
        }
    
        User currentUser = getAuthenticatedUser();
    
        // 1. POTONG STOK DI LOKASI ASAL (FROM)
        StockBalance sourceStock = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                productId, request.getFromLocationType().toUpperCase(), request.getFromLocationId()
        ).orElseThrow(() -> new RuntimeException("Stok tidak ditemukan di lokasi asal"));
    
        long currentSourceQty = sourceStock.getQty() != null ? sourceStock.getQty() : 0L;
        if (currentSourceQty < qty) {
            throw new RuntimeException("Stok di lokasi asal tidak mencukupi! Sisa: " + currentSourceQty);
        }
    
        sourceStock.setQty(currentSourceQty - qty);
        sourceStock.setUpdatedBy(currentUser);
        sourceStock.setUpdatedAt(LocalDateTime.now());
        stockBalanceRepository.save(sourceStock);
    
        // 2. TAMBAH STOK DI LOKASI TUJUAN (TO)
        StockBalance destStock = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                productId, request.getToLocationType().toUpperCase(), request.getToLocationId()
        ).orElse(new StockBalance());

        if (destStock.getId() == null) {
            Product product = productRepository.findById(productId).orElseThrow();
            destStock.setProduct(product);
            destStock.setLocationType(request.getToLocationType().toUpperCase());
            destStock.setLocationId(request.getToLocationId());
            destStock.setQty(qty);
            destStock.setCreatedBy(currentUser);
        } else {
            long currentDestQty = destStock.getQty() != null ? destStock.getQty() : 0L;
            destStock.setQty(currentDestQty + qty);
        }

        destStock.setUpdatedBy(currentUser);
        destStock.setUpdatedAt(LocalDateTime.now());
        StockBalance savedDestStock = stockBalanceRepository.save(destStock);
    
        // 3. CATAT MUTASI STOK (TRANSFER)
        StockMutation stockMutation = new StockMutation();
        stockMutation.setProduct(savedDestStock.getProduct());
        stockMutation.setPartner(savedDestStock.getProduct().getPartner());
        stockMutation.setReferenceType(StockMutation.ReferenceType.TRANSFER_REQUEST);
        stockMutation.setType(StockMutation.Type.TRANSFER);
        stockMutation.setQty(qty); 
    
        stockMutation.setFromLocationType(StockMutation.Location.valueOf(sourceStock.getLocationType().toUpperCase()));
        stockMutation.setFromLocationId(sourceStock.getLocationId());
        stockMutation.setToLocationType(StockMutation.Location.valueOf(savedDestStock.getLocationType().toUpperCase()));
        stockMutation.setToLocationId(savedDestStock.getLocationId());
    
        stockMutation.setCreatedBy(currentUser);
        stockMutation.setCreatedAt(LocalDateTime.now());
        stockMutationRepository.save(stockMutation);

        return mapToResponse(savedDestStock);
    }
}