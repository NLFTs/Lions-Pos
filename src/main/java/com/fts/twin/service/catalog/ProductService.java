package com.fts.twin.service.catalog;

import com.fts.twin.dto.request.product.ProductRequest;
import com.fts.twin.dto.response.catalogresponse.ProductResponse;
import com.fts.twin.dto.response.components.PartnerSimpleDto;
import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fts.twin.handler.ResourceNotFoundException;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.catalog.CategoryProduct;
import com.fts.twin.model.catalog.Product;
import com.fts.twin.model.catalog.ProductPhoto;
import com.fts.twin.model.common.Partners;
import com.fts.twin.model.inventory.StockBalance;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.catalog.CategoryProductRepository;
import com.fts.twin.repository.catalog.ProductPhotoRepository;
import com.fts.twin.repository.catalog.ProductRepository;
import com.fts.twin.repository.inventory.StockBalanceRepository;
import com.fts.twin.service.system.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final ProductPhotoRepository productPhotoRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final NotificationService notificationService;

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

    private void checkPermission(User user, String permissionSlug) {
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

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private Product getValidatedProduct(Long id, User currentUser) {
        if (currentUser.getPartner() == null) {
            return productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        }

        // Partner biasa dikunci strict agar tidak bisa mengintip silang produk kompetitor
        Product product = productRepository.findByIdAndPartner(id, currentUser.getPartner());
        if (product == null) {
            throw new ResourceNotFoundException("Product", id);
        }
        return product;
    }

    // ─── MAIN CORE METHODS (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public Page<ProductResponse> findAllProduct(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findAll(pageRequest).map(this::mapToResponse);
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)

    @Transactional
    public ProductResponse create(ProductRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.store");
        
        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat produk langsung tanpa scope partner.");
        }

        CategoryProduct category = null;
        if (request.getCategoryId() != null) {
            // Pastiin kategori yang dipilih juga milik partner yang sama
            category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getPartner() != null && c.getPartner().getId().equals(partner.getId()))
                    .orElseThrow(() -> new RuntimeException("Category tidak ditemukan atau bukan milik perusahaan Anda."));
        }

        Product product = new Product();
        product.setPartner(partner);
        product.setCategory(category);

        if (request.getName() == null || request.getName().trim().isEmpty()) { throw new RuntimeException("Nama Produk harus diisi."); }
        product.setName(request.getName());

        if (request.getBasePrice() == null) { throw new RuntimeException("Harga dasar tidak boleh kosong."); }
        if (request.getBasePrice().compareTo(java.math.BigDecimal.ZERO) < 0) { throw new RuntimeException("Harga dasar tidak boleh negatif."); }
        product.setBasePrice(request.getBasePrice());
        
        if (request.getTrackStock() != null) product.setTrackStock(request.getTrackStock());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());

        // Logic Otomatisasi SKU Tenant
        String finalSku = request.getSku();
        if (finalSku == null || finalSku.trim().isEmpty()) {
            finalSku = generateUniqueSku(product.getName(), partner);
        } else {
            finalSku = finalSku.trim().toUpperCase();
            if (productRepository.existsBySkuAndPartner(finalSku, partner)) {
                throw new RuntimeException("SKU '" + finalSku + "' sudah terdaftar di toko Anda!");
            }
        }
        product.setSku(finalSku);
        product.setCreatedBy(currentUser);
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        notificationService.createOrUpdateProductNotification(
                partner,
                savedProduct.getName(),
                currentUser
        );

        // Foto produk diurus sepenuhnya oleh endpoint POST /api/v1/product-photos
        // yang dipanggil frontend setelah produk dibuat — tidak perlu auto-create di sini
        // agar tidak terjadi duplikasi record foto

        return mapToResponse(savedProduct);
    }

    public Page<ProductResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.index"); // Sikat pake permission index

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (currentUser.getPartner() == null) {
            return productRepository.findAll(pageable).map(this::mapToResponse);
        }

        return productRepository.findAllByPartner(currentUser.getPartner(), pageable)
                .map(this::mapToResponse);
    }

    public ProductResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.show");

        Product product = getValidatedProduct(id, currentUser);
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse patchProduct(Long id, ProductRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.update"); // Diperbarui ke permission update

        Product product = getValidatedProduct(id, currentUser);
        Partners partner = product.getPartner(); 

        if (request.getCategoryId() != null && partner != null) {
            CategoryProduct category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getPartner() != null && c.getPartner().getId().equals(partner.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getTrackStock() != null) product.setTrackStock(request.getTrackStock());

        if (request.getSku() != null && partner != null && !product.getSku().equals(request.getSku().trim().toUpperCase())) {
            String newSku = request.getSku().trim().toUpperCase();
            if (productRepository.existsBySkuAndPartner(newSku, partner)) {
                throw new RuntimeException("SKU " + newSku + " sudah terpakai!");
            }
            product.setSku(newSku);
        }

        // Sinkronisasi primary photo: pindahkan flag isPrimary ke foto yang baru dipilih
        // TIDAK menghapus file dari disk — penghapusan file hanya via DELETE /product-photos/{id}
        if (request.getImageUrl() != null) {
            List<com.fts.twin.model.catalog.ProductPhoto> existingPhotos = productPhotoRepository.findByProductId(product.getId());

            if (request.getImageUrl().trim().isEmpty()) {
                // image_url dikosongkan → lepas semua flag isPrimary
                existingPhotos.forEach(p -> {
                    p.setIsPrimary(false);
                    productPhotoRepository.save(p);
                });
            } else {
                String newPrimaryUrl = request.getImageUrl().trim();
                boolean found = false;
                for (com.fts.twin.model.catalog.ProductPhoto p : existingPhotos) {
                    // Hanya SATU foto yang boleh jadi primary — yang pertama match URL
                    boolean shouldBePrimary = !found && newPrimaryUrl.equals(p.getUrl());
                    if (shouldBePrimary) found = true;
                    p.setIsPrimary(shouldBePrimary);
                    productPhotoRepository.save(p);
                }
                // Kalau URL baru tidak ditemukan di product_photos → buat record baru
                if (!found) {
                    com.fts.twin.model.catalog.ProductPhoto newPhoto = new com.fts.twin.model.catalog.ProductPhoto();
                    newPhoto.setProduct(product);
                    newPhoto.setUrl(newPrimaryUrl);
                    newPhoto.setIsPrimary(true);
                    newPhoto.setSortOrder(0);
                    newPhoto.setCreatedAt(LocalDateTime.now());
                    newPhoto.setCreatedBy(currentUser);
                    productPhotoRepository.save(newPhoto);
                }
            }
        }

        product.setUpdatedBy(currentUser);
        product.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse softDeleteProduct(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.delete"); // Diubah murni ke permission delete

        Product product = getValidatedProduct(id, currentUser);
        product.setIsActive(false);

        product.setUpdatedBy(currentUser);
        product.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse restoreProduct(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.update"); // Diubah murni ke permission update

        Product product = getValidatedProduct(id, currentUser);
        product.setIsActive(true);

        product.setUpdatedBy(currentUser);
        product.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse setTrueTrackStock(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.update"); // Diubah murni ke permission update 

        Product product = getValidatedProduct(id, currentUser);
        product.setTrackStock(true);

        product.setUpdatedBy(currentUser);
        product.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse setFalseTrackStock(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.update"); 

        Product product = getValidatedProduct(id, currentUser);
        product.setTrackStock(false);

        product.setUpdatedBy(currentUser);
        product.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "produk.delete");

        Product product = getValidatedProduct(id, currentUser);

        // GUARD: Cek apakah masih ada stok aktif di lokasi manapun
        List<StockBalance> stockBalances = stockBalanceRepository.findByProductId(product.getId());
        long nonZeroStocks = stockBalances.stream()
                .filter(sb -> sb.getQty() != null && sb.getQty() > 0)
                .count();
        if (nonZeroStocks > 0) {
            throw new RuntimeException(
                "Produk '" + product.getName() + "' tidak dapat dihapus karena masih memiliki stok di " +
                nonZeroStocks + " lokasi. Kosongkan semua stok terlebih dahulu."
            );
        }

        // Hapus semua foto produk terlebih dahulu
        List<ProductPhoto> photos = productPhotoRepository.findByProductId(product.getId());
        for (ProductPhoto photo : photos) {
            if (photo.getUrl() != null) {
                if (productPhotoRepository.countByUrl(photo.getUrl()) <= 1) {
                    deleteFileDisk(photo.getUrl());
                }
            }
        }
        productPhotoRepository.deleteAll(photos);

        // Hapus semua stock balance (qty 0) yang terkait produk ini
        if (!stockBalances.isEmpty()) {
            stockBalanceRepository.deleteAll(stockBalances);
        }

        productRepository.delete(product);
    }

    // ─── PRIVATE UTILS & MAPPERS ────────────────────────────────────────────

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
        if (product == null) return null;

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

        List<ProductPhoto> photos = productPhotoRepository.findByProductId(product.getId());
        ProductPhoto primaryPhoto = photos.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsPrimary()))
                .findFirst()
                .orElse(photos.isEmpty() ? null : photos.get(0));
        if (primaryPhoto != null) {
            resp.setImageUrl(primaryPhoto.getUrl());
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
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}