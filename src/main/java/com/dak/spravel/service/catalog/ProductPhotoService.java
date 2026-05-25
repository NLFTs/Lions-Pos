package com.dak.spravel.service.catalog;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@lombok.extern.slf4j.Slf4j
@Service
@RequiredArgsConstructor
public class ProductPhotoService {

    private final ProductPhotoRepository productPhotoRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private void deleteFileDisk(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String cleanPath = fileUrl;
            if (cleanPath.startsWith("/uploads/")) {
                cleanPath = cleanPath.substring("/uploads/".length());
            }
            java.nio.file.Path path = java.nio.file.Paths.get(uploadDir, cleanPath);
            java.nio.file.Files.deleteIfExists(path);
            log.info("[DELETE FILE] Berhasil menghapus file foto produk lama: {}", path);
        } catch (Exception e) {
            log.error("[DELETE FILE] Gagal menghapus file foto produk lama {}: {}", fileUrl, e.getMessage());
        }
    }

    // --- HELPER: AUTH & BLOCK ADMIN (Standardized) ---

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    //  HELPER UTAMA HARI INI: Cek murni role Owner
    private boolean isOwner(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("owner"));
    }

    // 🛠️ MODIFIKASI: Hanya mengizinkan Owner Partner murni
    private User getAuthenticatedOwnerUser() {
        User user = getAuthenticatedUser();
        
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin")
                        || role.getSlug().equalsIgnoreCase("super_admin"));

        if (isSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Super Admin tidak diperbolehkan masuk menu partner.");
        }

        if (!isOwner(user)) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner yang diizinkan mengelola data master produk.");
        }

        if (user.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: User tidak terasosiasi dengan Partner manapun.");
        }

        return user;
    }

    private ProductPhoto getValidatedPhoto(Long id, Partners partner) {
        ProductPhoto photo = productPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhoto", id));

        if (partner == null || !photo.getProduct().getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Foto ini milik produk partner lain.");
        }
        return photo;
    }

    // --- MAIN METHODS ---

    // Owner → melihat foto produk miliknya sendiri
    public List<ProductPhoto> findByProductId(Long productId) {
        User currentUser = getAuthenticatedOwnerUser();
        
        Product product = productRepository.findByIdAndPartner(productId, currentUser.getPartner());
        if (product == null) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return productPhotoRepository.findByProductId(productId);
    }

    // ==========================================
    // CREATE ( KUNCI: Hanya Owner)
    // ==========================================
    @Transactional
    public ProductPhoto create(ProductPhotoRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        
        if (!isOwner(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner yang diperbolehkan menambahkan foto produk.");
        }
        
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: User tidak terasosiasi dengan partner.");
        }
        
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

    // ==========================================
    // UPDATE (🔒 KUNCI: Hanya Owner)
    // ==========================================
    @Transactional
    public ProductPhoto update(Long id, ProductPhotoRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        
        if (!isOwner(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner yang diperbolehkan mengubah foto produk.");
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: User tidak terasosiasi dengan partner.");
        }

        ProductPhoto photo = getValidatedPhoto(id, currentUser.getPartner());

        if (request.getUrl() != null) {
            if (photo.getUrl() != null && !photo.getUrl().equals(request.getUrl())) {
                deleteFileDisk(photo.getUrl());
            }
            photo.setUrl(request.getUrl());
        }
        if (request.getIsPrimary() != null) photo.setIsPrimary(request.getIsPrimary());
        if (request.getSortOrder() != null) photo.setSortOrder(request.getSortOrder());

        return productPhotoRepository.save(photo);
    }

    // ==========================================
    // DELETE ( KUNCI: Hanya Owner)
    // ==========================================
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        
        if (!isOwner(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya Owner yang diperbolehkan menghapus foto produk.");
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: User tidak terasosiasi dengan partner.");
        }

        ProductPhoto photo = getValidatedPhoto(id, currentUser.getPartner());
        if (photo.getUrl() != null) {
            deleteFileDisk(photo.getUrl());
        }
        productPhotoRepository.delete(photo);
    }
}