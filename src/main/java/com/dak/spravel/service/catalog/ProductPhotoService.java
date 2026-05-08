package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.ProductPhotoRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.catalog.ProductPhoto;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductPhotoRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPhotoService {

    private final ProductPhotoRepository productPhotoRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // --- HELPER: AUTH & BLOCK ADMIN ---
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
        
        if (isAdmin) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Foto Produk.");
        }

        return user;
    }

    // Helper: Validasi kepemilikan foto (via Product -> Partner)
    private ProductPhoto getValidatedPhoto(Long id, Partners partner) {
        ProductPhoto photo = productPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhoto", id));

        // Cek apakah produk di dalam foto ini milik partner si user
        if (!photo.getProduct().getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Foto ini milik produk partner lain.");
        }
        return photo;
    }

    // --- MAIN METHODS ---

    // GET ALL BY PRODUCT
    public List<ProductPhoto> findByProductId(Long productId) {
        User currentUser = getAuthenticatedUser();
        // Pastiin produknya emang punya dia sebelum narik list foto
        Product product = productRepository.findByIdAndPartner(productId, currentUser.getPartner());
        if (product == null) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return productPhotoRepository.findByProductId(productId);
    }

    @Transactional
    public ProductPhoto create(ProductPhotoRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        
        // Pastiin produk yang mau dikasih foto emang milik partner si user
        Product product = productRepository.findByIdAndPartner(request.getProductId(), currentUser.getPartner());
        if (product == null) {
            throw new ResourceNotFoundException("Product", request.getProductId());
        }

        ProductPhoto photo = new ProductPhoto();
        photo.setProduct(product);
        photo.setUrl(request.getUrl());
        photo.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        photo.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        return productPhotoRepository.save(photo);
    }

    @Transactional
    public ProductPhoto update(Long id, ProductPhotoRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        ProductPhoto photo = getValidatedPhoto(id, currentUser.getPartner());

        if (request.getUrl() != null) photo.setUrl(request.getUrl());
        if (request.getIsPrimary() != null) photo.setIsPrimary(request.getIsPrimary());
        if (request.getSortOrder() != null) photo.setSortOrder(request.getSortOrder());

        return productPhotoRepository.save(photo);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        ProductPhoto photo = getValidatedPhoto(id, currentUser.getPartner());
        productPhotoRepository.delete(photo);
    }
}