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

    // --- STANDARDIZED AUTH HELPERS ---

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedOwner() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("owner"));

        boolean isStaff = !user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));

        if (!isAuthorized || !isStaff) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin"));
    }

    private boolean isOwner(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("owner"));
    }


    // --- VALIDASI STOCK BALANCE ---

    private StockBalance getValidatedStockBalance(Long id, User currentUser) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));

        if (currentUser.getPartner() == null
                || stock.getProduct() == null
                || stock.getProduct().getPartner() == null
                || !stock.getProduct().getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock balance bukan milik partner Anda.");
        }

        // Tambahan proteksi detail: Employee dilarang mengintip ID stock milik lokasi lain
        if (isEmployee(currentUser)) {
            if (!"BRANCH".equalsIgnoreCase(stock.getLocationType()) || 
                currentUser.getBranch() == null || 
                !stock.getLocationId().equals(currentUser.getBranch().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda tidak memiliki akses ke lokasi data stok ini.");
            }
        }

        return stock;
    }

    // --- HELPER: Resolve nama lokasi ---

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

    // --- HELPER: Validasi lokasi milik partner ---

    private void validateLocation(String locationType, Long locationId, Partners partner) {
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
            throw new RuntimeException("locationType tidak valid. Gunakan 'BRANCH' atau 'WAREHOUSE'.");
        }
    }

    // --- MAP TO RESPONSE ---

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

    // --- RECORD MUTATION ---

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

    // --- MAIN METHODS ---

    // KHUSUS SUPER ADMIN

    public List<StockBalanceResponse> findAllStockBalance() {
        getAuthenticatedSuperAdmin();
        return stockBalanceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // FIND ALL — PARTNER / EMPLOYEE (Saring Ketat Modul D)
    // =========================
    public List<StockBalanceResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        
        List<StockBalance> allStocks = stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId());

        // 🔒 LOCK VALIDASI: Employee murni cuma boleh lihat Branch asalnya sendiri, gudang & cabang lain disembunyikan
        if (isEmployee(currentUser)) {
            if (currentUser.getBranch() == null) {
                throw new RuntimeException("Akses Ditolak: Akun Employee Anda belum ditempatkan di Cabang (Branch) manapun.");
            }
            
            Long employeeBranchId = currentUser.getBranch().getId();

            return allStocks.stream()
                    .filter(stock -> "BRANCH".equalsIgnoreCase(stock.getLocationType()) 
                            && stock.getLocationId() != null 
                            && stock.getLocationId().equals(employeeBranchId))
                    .map(this::mapToResponse)
                    .toList();
        }
        
        // Admin Partner bebas melihat seluruh ekosistem lokasinya
        return allStocks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // PAGINATION — PARTNER / EMPLOYEE
    // =========================
    public Page<StockBalanceResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedOwner();

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        
        // 🔒 LOCK VALIDASI: Berlaku filter terikat lokasi yang sama di pagination
        if (isEmployee(currentUser)) {
            if (currentUser.getBranch() == null) {
                throw new RuntimeException("Akses Ditolak: Akun Employee Anda belum ditempatkan di Cabang (Branch) manapun.");
            }
            return stockBalanceRepository
                    .findByLocationTypeAndLocationId("BRANCH", currentUser.getBranch().getId(), pageRequest)
                    .map(this::mapToResponse);
        }

        return stockBalanceRepository
                .findByProductPartnerId(currentUser.getPartner().getId(), pageRequest)
                .map(this::mapToResponse);
    }

    public List<StockLocationSummaryResponse> findStockSummary() {
        User currentUser = getAuthenticatedOwner();
        Long partnerId = currentUser.getPartner().getId();

        List<StockBalance> allBalances = stockBalanceRepository.findByProductPartnerId(partnerId);

        // 🔒 LOCK VALIDASI: Jika Employee, potong data summary agar tidak mengintip breakdown lokasi lain
        if (isEmployee(currentUser)) {
            Long branchId = currentUser.getBranch() != null ? currentUser.getBranch().getId() : 0L;
            allBalances = allBalances.stream()
                    .filter(sb -> "BRANCH".equalsIgnoreCase(sb.getLocationType()) && sb.getLocationId().equals(branchId))
                    .toList();
        }

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

    public StockBalanceResponse findById(Long id) {
        User currentUser = getAuthenticatedOwner();
        return mapToResponse(getValidatedStockBalance(id, currentUser));
    }

    public List<StockBalanceResponse> findByLocation(String locationType, Long locationId) {
        User currentUser = getAuthenticatedOwner();

        // 🔒 LOCK VALIDASI: Employee dilarang menembak ID lokasi milik orang lain
        if (isEmployee(currentUser)) {
            if (!"BRANCH".equalsIgnoreCase(locationType) || !locationId.equals(currentUser.getBranch().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda hanya diizinkan melihat lokasi branch sendiri.");
            }
        }

        return stockBalanceRepository
                .findByLocationTypeAndLocationId(locationType.toUpperCase(), locationId)
                .stream()
                .filter(s -> s.getProduct() != null
                        && s.getProduct().getPartner() != null
                        && s.getProduct().getPartner().getId().equals(currentUser.getPartner().getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockBalanceResponse> findByBranch(Long branchId) {
        User currentUser = getAuthenticatedOwner();

        // 🔒 LOCK VALIDASI
        if (isEmployee(currentUser) && !branchId.equals(currentUser.getBranch().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda hanya diizinkan melihat data branch sendiri.");
        }

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan"));
        if (branch.getPartners() == null || !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
        }

        return stockBalanceRepository
                .findByLocationTypeAndLocationId("BRANCH", branchId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockBalanceResponse> findByWarehouse(Long warehouseId) {
        User currentUser = getAuthenticatedOwner();

        // 🔒 LOCK VALIDASI: Employee murni dilarang keras menembak API Warehouse langsung
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Otoritas Employee tidak diizinkan mengakses data Warehouse.");
        }

        Warehouses warehouse = warehousesRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan dengan ID: " + warehouseId));
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
    // =========================
    @Transactional
    public StockBalanceResponse create(StockBalanceRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        
        // 🔥 VALIDASI MODUL D: Employee Dilarang Create/Inisiasi Stock Awal Manual
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak diizinkan menginisiasi stock.");
        }

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner manapun.");
        }

        Product product = productRepository.findById(request.getProduct())
                .orElseThrow(() -> new RuntimeException("Product tidak ditemukan"));

        if (product.getPartner() == null || !product.getPartner().getId().equals(partner.getId())) {
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

        recordMutation(saved, request.getQty(), "ADJUSTMENT",
                "STOCK_OPNAME", saved.getId(),
                "Stock awal manual untuk " + product.getName(), currentUser);

        return mapToResponse(saved);
    }

    // =========================
    // INISIASI STOCK AWAL BATCH
    // =========================
    @Transactional
    public List<StockBalanceResponse> initializeStock(StockBalanceInitRequest request) {
        User currentUser = getAuthenticatedOwner();

        // 🔥 VALIDASI MODUL D: Employee Dilarang Initialize Batch Stock
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak diizinkan melakukan inisiasi batch stock.");
        }

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner manapun.");
        }

        validateLocation(request.getLocationType(), request.getLocationId(), partner);

        List<StockBalanceResponse> results = new ArrayList<>();

        for (StockBalanceItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product tidak ditemukan: id=" + itemRequest.getProductId()));

            if (product.getPartner() == null || !product.getPartner().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Product '" + product.getName() + "' bukan milik partner Anda.");
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

    @Transactional
    public void adjustStock(Long productId, String locationType, Long locationId, Long adjustment) {
        StockBalance stock = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(productId, locationType.toUpperCase(), locationId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock tidak ditemukan untuk product id=" + productId
                                + " di " + locationType + " id=" + locationId));

        long currentQty = stock.getQty() != null ? stock.getQty() : 0L;
        long newQty = currentQty + adjustment;

        if (newQty < 0) {
            throw new RuntimeException("Stok tidak mencukupi untuk produk ID: " + productId
                    + ". Stock saat ini: " + currentQty);
        }

        stock.setQty(newQty);
        stock.setUpdatedAt(LocalDateTime.now());
        stockBalanceRepository.save(stock);
    }

    @Transactional
    public StockBalanceResponse createFromWarehouse(WarehouseStockInRequest request) {
        User currentUser = getAuthenticatedUser();
        
        // Employee dilarang input barang masuk ke Warehouse
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak diizinkan memproses stok masuk gudang pusat.");
        }

        Warehouses warehouse = warehousesRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan dengan ID: " + request.getWarehouseId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product tidak ditemukan dengan ID: " + request.getProductId()));

        StockBalance stock = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(request.getProductId(), "WAREHOUSE", request.getWarehouseId())
                .orElse(new StockBalance());

        if (stock.getId() == null) {
            stock.setProduct(product);
            stock.setLocationType("WAREHOUSE");
            stock.setLocationId(warehouse.getId());
            stock.setQty(request.getQty());
            stock.setCreatedBy(currentUser);
        } else {
            long currentQty = stock.getQty() != null ? stock.getQty() : 0L;
            stock.setQty(currentQty + request.getQty());
        }

        stock.setUpdatedBy(currentUser);
        stock.setUpdatedAt(LocalDateTime.now());

        StockBalance stockBalance = stockBalanceRepository.save(stock);

        StockMutation stockMutation = new StockMutation();
        stockMutation.setProduct(stockBalance.getProduct());
        stockMutation.setQty(request.getQty()); // Fix: gunakan request qty aslinya
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
        User currentUser = getAuthenticatedUser();
        
        // 🔒 LOCK VALIDASI MODUL E: Employee bisa menerima barang tapi HANYA di branch miliknya sendiri
        if (isEmployee(currentUser)) {
            if (currentUser.getBranch() == null || !request.getBranchId().equals(currentUser.getBranch().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda hanya bisa menerima barang di branch Anda sendiri.");
            }
        }

        Branches branch = branchesRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan dengan ID: " + request.getBranchId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product tidak ditemukan dengan ID: " + request.getProductId()));

        StockBalance stock = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(request.getProductId(), "BRANCH", request.getBranchId())
                .orElse(new StockBalance());

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
        stockMutation.setQty(request.getQty()); // Fix: gunakan request qty aslinya
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
    
        if (request.getFromLocationType().equals(request.getToLocationType()) && request.getFromLocationId().equals(request.getToLocationId())) {
            throw new RuntimeException("Lokasi asal dan tujuan tidak boleh sama persis!");
        }
    
        User currentUser = getAuthenticatedUser();
        
        // 🔒 LOCK VALIDASI MODUL D: Transfer Request oleh Employee dikunci harus melibatkan lokasi tujuannya
        if (isEmployee(currentUser)) {
            if (!"BRANCH".equalsIgnoreCase(request.getToLocationType()) || 
                currentUser.getBranch() == null || 
                !request.getToLocationId().equals(currentUser.getBranch().getId())) {
                throw new RuntimeException("Akses Ditolak: Employee hanya bisa melakukan transfer request menuju ke branch miliknya sendiri.");
            }
        }
    
        // STEP 1: POTONG STOK DI LOKASI ASAL (FROM)
        StockBalance sourceStock = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(productId, request.getFromLocationType(), request.getFromLocationId())
                .orElseThrow(() -> new RuntimeException("Stok tidak ditemukan di lokasi asal."));
    
        long currentSourceQty = sourceStock.getQty() != null ? sourceStock.getQty() : 0L;
        if (currentSourceQty < qty) {
            throw new RuntimeException("Stok di lokasi asal tidak mencukupi!");
        }
    
        sourceStock.setQty(currentSourceQty - qty);
        sourceStock.setUpdatedBy(currentUser);
        sourceStock.setUpdatedAt(LocalDateTime.now());
        stockBalanceRepository.save(sourceStock);
    
        // STEP 2: TAMBAH STOK DI LOKASI TUJUAN (TO)
        StockBalance destStock = stockBalanceRepository
                .findByProductIdAndLocationTypeAndLocationId(productId, request.getToLocationType(), request.getToLocationId())
                .orElse(new StockBalance());

        if (destStock.getId() == null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product tidak ditemukan"));
            destStock.setProduct(product);
            destStock.setLocationType(request.getToLocationType());
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
    
        // STEP 3: CATAT MUTASI STOK (TRANSFER)
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