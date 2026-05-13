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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryProductRepository categoryRepository;
    private final UserRepository userRepository;

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
                .anyMatch(role -> role.getName().equalsIgnoreCase("SUPER_ADMIN"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        // Cek apakah dia punya role operasional
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN_PARTNER") || 
                                role.getName().equalsIgnoreCase("EMPLOYEE"));
        
        // Blokir jika dia SUPER_ADMIN atau tidak punya role yang sesuai
        boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("SUPER_ADMIN"));

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

    // Find All untuk Super Admin
    public List<ProductResponse> findAllProduct() {
        getAuthenticatedSuperAdmin();

        List<Product> allProducts = productRepository.findAllProduct();

        return allProducts.stream()
                .map(this::mapToResponse)
                .toList();
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
        product.setCategory(category);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Nama Produk harus diisi.");
        }
        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());

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

    public List<ProductResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();

        if(isAdmin(currentUser)) {
            
        }

        return productRepository.findAllByPartner(partner).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();
        Product product = getValidatedProduct(id, partner);
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();
        Product product = getValidatedProduct(id, partner);

        if (request.getCategoryId() != null) {
            CategoryProduct category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getPartner().getId().equals(partner.getId()))
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getBasePrice() != null)
            product.setBasePrice(request.getBasePrice());

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

        // Map Partner (Cuma ID & Name)
        if (product.getPartner() != null) {
            PartnerSimpleDto pDto = new PartnerSimpleDto();
            pDto.setId(product.getPartner().getId());
            pDto.setName(product.getPartner().getName());
            resp.setPartnerId(pDto);
        }

        // Map Category (Cuma ID & Name)
        if (product.getCategory() != null) {
            ProductResponse.CategoryProductSimpleDto cDto = new ProductResponse.CategoryProductSimpleDto();
            cDto.setId(product.getCategory().getId());
            cDto.setName(product.getCategory().getName());
            resp.setCategoryId(cDto);
        }

        // Map CreatedBy (Cuma ID & Username)
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