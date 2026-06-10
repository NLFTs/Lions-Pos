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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPhotoService {

    private final ProductPhotoRepository productPhotoRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ─── FILE SYSTEM UTILS ──────────────────────────────────────────────────

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

    // ─── PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // KUNCI DINAMIS: Check permission dinamis dari database tanpa kaku nge-lock nama role
    private void checkPermission(User user, String permissionSlug) {
        // Raja Super Admin (partner null) bypass seluruh jenis gate permission
        if (user.getPartner() == null) {
            return;
        }

        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    // ─── MULTI-TENANT GUARD (ANTI NULL POINTER) ──────────────────────────

    private ProductPhoto getValidatedPhoto(Long id, User currentUser) {
        ProductPhoto photo = productPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhoto", id));

        // Super Admin global bebas bypass pengecekan tenant id
        if (currentUser.getPartner() == null) {
            return photo;
        }

        if (photo.getProduct() == null || photo.getProduct().getPartner() == null || 
                !photo.getProduct().getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Foto ini milik produk partner lain.");
        }
        return photo;
    }

    // ─── MAIN METHODS (SUDAH DISERAGAMKAN POLANYA) ──────────────────────────

    // GET / View foto produk berdasarkan ID produk
    public List<ProductPhoto> findByProductId(Long productId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.show"); // Ikut ke permission show produk
        
        // Handling aman super admin / partner user scope
        Product product;
        if (currentUser.getPartner() == null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        } else {
            product = productRepository.findByIdAndPartner(productId, currentUser.getPartner());
            if (product == null) {
                throw new ResourceNotFoundException("Product", productId);
            }
        }
        
        return productPhotoRepository.findByProductId(productId);
    }

    // ==========================================
    // CREATE (Basis Permission)
    // ==========================================
    @Transactional
    public ProductPhoto create(ProductPhotoRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.update");
        
        Partners partner = currentUser.getPartner();
        Product product;

        if (partner == null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        } else {
            product = productRepository.findByIdAndPartner(request.getProductId(), partner);
            if (product == null) {
                throw new ResourceNotFoundException("Product", request.getProductId());
            }
        }

        // Guard: jangan simpan duplikat URL untuk produk yang sama
        boolean alreadyExists = productPhotoRepository.findByProductId(product.getId())
                .stream().anyMatch(p -> p.getUrl() != null && p.getUrl().equals(request.getUrl()));
        if (alreadyExists) {
            // Return foto yang sudah ada daripada buat baru (idempotent)
            return productPhotoRepository.findByProductId(product.getId())
                    .stream()
                    .filter(p -> p.getUrl().equals(request.getUrl()))
                    .findFirst()
                    .orElseThrow();
        }

        ProductPhoto photo = new ProductPhoto();
        photo.setProduct(product);
        photo.setUrl(request.getUrl());
        photo.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        photo.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        return productPhotoRepository.save(photo);
    }

    // ==========================================
    // UPDATE (Berbasis Permission)
    // ==========================================
    @Transactional
    public ProductPhoto update(Long id, ProductPhotoRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.update");

        ProductPhoto photo = getValidatedPhoto(id, currentUser);

        if (request.getUrl() != null) {
            if (photo.getUrl() != null && !photo.getUrl().equals(request.getUrl())) {
                if (productPhotoRepository.countByUrl(photo.getUrl()) <= 1) {
                    deleteFileDisk(photo.getUrl());
                }
            }
            photo.setUrl(request.getUrl());
        }
        if (request.getIsPrimary() != null) photo.setIsPrimary(request.getIsPrimary());
        if (request.getSortOrder() != null) photo.setSortOrder(request.getSortOrder());

        return productPhotoRepository.save(photo);
    }

    // ==========================================
    // DELETE (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.delete"); // Sikat pake permission delete produk

        ProductPhoto photo = getValidatedPhoto(id, currentUser);
        if (photo.getUrl() != null) {
            if (productPhotoRepository.countByUrl(photo.getUrl()) <= 1) {
                deleteFileDisk(photo.getUrl());
            }
        }
        productPhotoRepository.delete(photo);
    }
}