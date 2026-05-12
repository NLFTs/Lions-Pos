package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
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

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Stock Balance.");
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

        return stock;
    }

    // GET ALL
    public List<StockBalance> findAll() {
        User currentUser = getAuthenticatedUser();
        return stockBalanceRepository.findByProductPartnerId(currentUser.getPartner().getId());
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