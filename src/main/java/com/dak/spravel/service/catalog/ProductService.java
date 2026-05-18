package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.dto.response.catalogresponse.ProductResponse;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryProductRepository categoryRepository;
    private final UserRepository userRepository;
    private final com.dak.spravel.repository.catalog.ProductPhotoRepository productPhotoRepository;

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

    // --- HELPER: AUTH & VALIDATION ---

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

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        // Cek apakah dia punya role operasional
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") || 
                                role.getSlug().equalsIgnoreCase("employee-partners"));
        
        // Blokir jika dia SUPER_ADMIN atau tidak punya role yang sesuai
        boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));

        if (!isAuthorized || !isStaff) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin"));
    }
    

    private Product getValidatedProduct(Long id, Partners partner) {
        // Cari produk berdasarkan ID dan Partner (cegah intip tetangga)
        Product product = productRepository.findByIdAndPartner(id, partner);
        if (product == null) {
            throw new ResourceNotFoundException("Product", id);
        }
        return product;
    }


    // Find Page untuk Super Admine
    public Page<ProductResponse> findAllProduct(int Page , int size) {
        getAuthenticatedSuperAdmin();

        PageRequest pageRequest = PageRequest.of(Page, size, Sort.by("name").ascending());

        return productRepository.findAll(pageRequest)
                .map(this::mapToResponse);
    }

    // --- MAIN METHODS ---
    // Khusus untuk Admin partner 
    @Transactional
    public ProductResponse create(ProductRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Produk.");
        }

        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        CategoryProduct category = null;
        if (request.getCategoryId() != null) {
            // Pastiin kategori yang dipilih juga milik partner yang sama
            category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getPartner().getId().equals(partner.getId()))
                    .orElseThrow(() -> new RuntimeException("Category not found or not belongs to your partner"));
        }

        Product product = new Product();
        product.setPartner(partner);

        if (category != null) {
            // throw new RuntimeErrorException();
        }
        product.setCategory(category);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Nama Produk harus diisi.");
        }
        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());
        
        if (request.getTrackStock() != null) product.setTrackStock(request.getTrackStock());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());

        // Logic SKU
        String finalSku = request.getSku();
        if (finalSku == null || finalSku.trim().isEmpty()) {
            finalSku = generateUniqueSku(product.getName(), partner);
        } else {
            finalSku = finalSku.trim().toUpperCase();
            if (productRepository.existsBySkuAndPartner(finalSku, partner)) {
                throw new RuntimeException("SKU " + finalSku + " sudah terdaftar di toko Anda!");
            }
        }
        product.setSku(finalSku);
        product.setCreatedBy(currentUser);
        product.setCreatedAt(LocalDateTime.now());

        return mapToResponse(productRepository.save(product));
    }

    public Page<ProductResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return productRepository.findAllByPartner(partner, pageRequest)
                .map(this::mapToResponse);
    }

    public ProductResponse findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();
        Product product = getValidatedProduct(id, partner);
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse patchProduct(Long id, ProductRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();
        Product product = getValidatedProduct(id, partner);
        
        System.out.println("DEBUG: Patching product " + id + " - isActive: " + request.getIsActive());

        if (request.getCategoryId() != null) {
            CategoryProduct category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getPartner().getId().equals(partner.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getTrackStock() != null) product.setTrackStock(request.getTrackStock());

        if (request.getSku() != null && !product.getSku().equals(request.getSku().trim().toUpperCase())) {
            String newSku = request.getSku().trim().toUpperCase();
            if (productRepository.existsBySkuAndPartner(newSku, partner)) {
                throw new RuntimeException("SKU " + newSku + " sudah terpakai!");
            }
            product.setSku(newSku);
        }

        product.setUpdatedBy(currentUser);
        AuditHelper.setUpdated(product);

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse softDeleteProduct(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Product product = getValidatedProduct(id, currentUser.getPartner());
        product.setIsActive(false);

        product.setUpdatedBy(currentUser);
        AuditHelper.setUpdated(product);
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse restoreProduct(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Product product = getValidatedProduct(id, currentUser.getPartner());
        product.setIsActive(true);

        product.setUpdatedBy(currentUser);
        AuditHelper.setUpdated(product);
        return mapToResponse(productRepository.save(product));

    }

    @Transactional
    public ProductResponse setTrueTrackStock(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Product product = getValidatedProduct(id, currentUser.getPartner());
        product.setTrackStock(true);

        product.setUpdatedBy(currentUser);
        AuditHelper.setUpdated(product);
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse setFalseTrackStock(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Product product = getValidatedProduct(id, currentUser.getPartner());
        product.setTrackStock(false);

        product.setUpdatedBy(currentUser);
        AuditHelper.setUpdated(product);
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Product product = getValidatedProduct(id, currentUser.getPartner());
        List<com.dak.spravel.model.catalog.ProductPhoto> photos = productPhotoRepository.findByProductId(product.getId());
        for (com.dak.spravel.model.catalog.ProductPhoto photo : photos) {
            if (photo.getUrl() != null) {
                deleteFileDisk(photo.getUrl());
            }
        }
        productRepository.delete(product);
    }

    // --- PRIVATE UTILS ---

    private String generateUniqueSku(String name, Partners partner) {
        String newSku;
        String cleanName = name.replaceAll("[^a-zA-Z]", "").toUpperCase();
        String prefix = cleanName.length() >= 3 ? "" + cleanName.charAt(0) + cleanName.charAt(cleanName.length() / 2)
                + cleanName.charAt(cleanName.length() - 1) : (cleanName + "XXX").substring(0, 3);

        do {
            int randomDigits = (int) (Math.random() * 900) + 100;
            newSku = prefix + "-" + randomDigits;
        } while (productRepository.existsBySkuAndPartner(newSku, partner));

        return newSku;
    }

    private ProductResponse mapToResponse(Product product) {
        if (product == null)
            return null;

        ProductResponse resp = new ProductResponse();
        resp.setId(product.getId());
        resp.setName(product.getName());
        resp.setSku(product.getSku());
        resp.setBasePrice(product.getBasePrice());
        resp.setTrackStock(product.getTrackStock());
        resp.setActive(product.getIsActive());

        if (product.getPartner() != null) {
            PartnerSimpleDto pDto = new PartnerSimpleDto();
            pDto.setId(product.getPartner().getId());
            pDto.setName(product.getPartner().getName());
            resp.setPartnerId(pDto);
        }

        if (product.getCategory() != null) {
            ProductResponse.CategoryProductSimpleDto cDto = new ProductResponse.CategoryProductSimpleDto();
            cDto.setId(product.getCategory().getId());
            cDto.setName(product.getCategory().getName());
            resp.setCategoryId(cDto);
        }

        resp.setCreatedBy(mapUserToDto(product.getCreatedBy()));
        resp.setUpdatedBy(mapUserToDto(product.getUpdatedBy()));
        resp.setDeletedBy(mapUserToDto(product.getDeletedBy()));

        resp.setCreatedAt(product.getCreatedAt());
        resp.setUpdatedAt(product.getUpdatedAt());
        resp.setDeletedAt(product.getDeletedAt());

        return resp;
    }

    private UserSimpleDto mapUserToDto(User user) {
        if (user == null)
            return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}