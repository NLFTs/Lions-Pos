package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
<<<<<<< HEAD
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.inventory.StockBalance;
=======
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.repository.auth.UserRepository;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
<<<<<<< HEAD
=======
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockBalanceService {

    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;
<<<<<<< HEAD

    // GET ALL
    public List<StockBalance> findAll() {
        return stockBalanceRepository.findAll(Sort.by("id").ascending());
=======
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
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }

    // GET ALL PAGINATED
    public Page<StockBalance> findAll(int page, int size) {
<<<<<<< HEAD
=======
        User currentUser = getAuthenticatedUser();
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        return stockBalanceRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    // GET BY ID
    public StockBalance findById(Long id) {
<<<<<<< HEAD
        return stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));
=======
        User currentUser = getAuthenticatedUser();
        return getValidatedStockBalance(id, currentUser);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }

    // GET BY PRODUCT
    public List<StockBalance> findByProductId(Long productId) {
<<<<<<< HEAD
=======
        User currentUser = getAuthenticatedUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (currentUser.getPartner() == null ||
                !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        return stockBalanceRepository.findByProductId(productId);
    }

    // GET BY LOCATION
    public List<StockBalance> findByLocation(String locationType, Long locationId) {
<<<<<<< HEAD
=======
        User currentUser = getAuthenticatedUser();
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        return stockBalanceRepository.findByLocationTypeAndLocationId(locationType, locationId);
    }

    // CREATE
    public StockBalance create(StockBalanceRequestDTO request) {
<<<<<<< HEAD
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        // Cek apakah sudah ada balance untuk kombinasi ini
        stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                request.getProductId(), request.getLocationType(), request.getLocationId()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Stock balance already exists for this product and location");
=======
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
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        });

        StockBalance stock = new StockBalance();
        stock.setProduct(product);
        stock.setLocationType(request.getLocationType());
        stock.setLocationId(request.getLocationId());
        stock.setQty(request.getQty());

        return stockBalanceRepository.save(stock);
    }

<<<<<<< HEAD
    // UPDATE QTY
    public StockBalance update(Long id, StockBalanceRequestDTO request) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));
=======
    // UPDATE
    public StockBalance update(Long id, StockBalanceRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        StockBalance stock = getValidatedStockBalance(id, currentUser);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635

        if (request.getQty() != null) stock.setQty(request.getQty());
        if (request.getLocationType() != null) stock.setLocationType(request.getLocationType());
        if (request.getLocationId() != null) stock.setLocationId(request.getLocationId());

        return stockBalanceRepository.save(stock);
    }

    // DELETE
    public void delete(Long id) {
<<<<<<< HEAD
        if (!stockBalanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("StockBalance", id);
        }
        stockBalanceRepository.deleteById(id);
=======
        User currentUser = getAuthenticatedUser();
        StockBalance stock = getValidatedStockBalance(id, currentUser);
        stockBalanceRepository.deleteById(stock.getId());
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }
}