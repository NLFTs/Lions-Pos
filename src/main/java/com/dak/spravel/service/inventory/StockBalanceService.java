package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.StockBalanceResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;

import jakarta.transaction.Transactional;
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
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        return user;
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
        .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses Di Tolak: Anda Bukan Super Admmin"); {
            return user;
        }
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") || 
                                role.getSlug().equalsIgnoreCase("employee"));
        
        boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));

        if (!isAuthorized || !isStaff) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin") || role.getSlug().equals("admin"));
    }

    private StockBalance getValidatedStockBalance(Long id, User currentUser) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));

        if (currentUser.getPartner() == null ||
                !stock.getProduct().getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock balance bukan milik partner Anda.");
        }

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Akses Di Tolak Admin Tidak Di Perbolehkan Mengelola Stock Balances");
        }

        return stock;
    }

    // mapToResponse
    private StockBalanceResponse mapToResponse(StockBalance stock) {

    // Product DTO
    StockBalanceResponse.ProductSimpleDto productDto =
            new StockBalanceResponse.ProductSimpleDto();

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

    return stockBalanceRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
}

    public List<StockBalanceResponse> findAll() {
    User currentUser = getAuthenticatedAdminPartnerOrEmployee();

    return stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId())
            .stream()
            .map(this::mapToResponse)
            .toList();
}

    // GET ALL PAGINATED
    public Page<StockBalance> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        return stockBalanceRepository.findByProductPartnerId(currentUser
            .getPartner()
            .getId(), PageRequest.of(page, size, Sort.by("createdAt")
            .descending()));
    }

    // GET BY ID
    public StockBalance findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedStockBalance(id, currentUser);
    }

    // GET BY PRODUCT
    public List<StockBalance> findByProductId(Long productId) {
        User currentUser = getAuthenticatedUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (currentUser.getPartner() == null ||
                !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        return stockBalanceRepository.findByProductId(productId);
    }

    // GET BY LOCATION
    public List<StockBalance> findByLocation(String locationType, Long locationId) {
        User currentUser = getAuthenticatedUser();
        return stockBalanceRepository.findByLocationTypeAndLocationId(locationType, locationId);
    }

    // CREATE
    @Transactional
    public StockBalance create(StockBalanceRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (currentUser.getPartner() == null ||
                !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
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
        stock.setQty(request.getQty());

        return stockBalanceRepository.save(stock);
    }

    // UPDATE
    public StockBalance update(Long id, StockBalanceRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        StockBalance stock = getValidatedStockBalance(id, currentUser);

        if (request.getQty() != null) stock.setQty(request.getQty());
        if (request.getLocationType() != null) stock.setLocationType(request.getLocationType());
        if (request.getLocationId() != null) stock.setLocationId(request.getLocationId());

        return stockBalanceRepository.save(stock);
    }

}