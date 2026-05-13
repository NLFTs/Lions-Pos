package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.CategoryProductCreate;
import com.dak.spravel.dto.response.catalogresponse.CategoryProductResponse;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
// import com.dak.spravel.repository.common.PartnerRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryProductService {

    private final CategoryProductRepository categoryProductRepository;
    // private final PartnerRepository partnersRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException(    "User tidak ditemukan di database"));

        return user;
    }

    // 1. Khusus Super Admin
    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        
        // Cek apakah di dalam Set<Role> ada yang namanya "SUPER_ADMIN"
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin"));

        if (!isSuperAdmin) {
            throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        }
        return user;
    }

    // 2. Gabungan Admin Partner dan Employee
    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        
        // Cek apakah ada role ADMIN_PARTNER atau EMPLOYEE di dalam Set
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin-partners") || 
                                role.getSlug().equals("employee-partners"));
        
        if (!isAuthorized) {
            throw new RuntimeException("Akses ditolak: Role tidak sesuai");
        }

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Kategori Produk.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        // Cek slug super_admin atau admin (sesuaikan dengan seeder lo)
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

        /**
     * Helper untuk validasi apakah sebuah kategori milik partner si user yang sedang l ogin
     */
    private CategoryProduct getValidatedCategory(Long id, User currentUser) {

        getAuthenticatedAdminPartnerOrEmployee();
        
        CategoryProduct category = categoryProductRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));

            // VALIDASI 2: Cross-Partner Check
            // Bandingkan Partner ID milik kategori dengan Partner ID milik user login
        if (currentUser.getPartner() == null || 
        !category.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses kategori dari partner lain.");
        }
        
        return category;
    }
    
    // Khusus untuk Superadmin
    public List<CategoryProductResponse> findAllCategoryProduct() {

        getAuthenticatedSuperAdmin();

        List<CategoryProduct> allCategories = categoryProductRepository.findAll();

        return allCategories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public Page<CategoryProductResponse> findAllCategoryProduct(int page, int size) {
    
        getAuthenticatedSuperAdmin();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

        return categoryProductRepository.findAll(pageRequest)
                .map(this::mapToResponse);
    }

    // Khusus untuk Admin partner dan employee
    public List<CategoryProductResponse> findAll() {
        getAuthenticatedAdminPartnerOrEmployee();
        
        List<CategoryProduct> allCategories = categoryProductRepository.findAllByPartner(getAuthenticatedUser().getPartner(), Sort.by("name").ascending());

        return allCategories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<CategoryProductResponse> findAll(int page, int size) {
        getAuthenticatedAdminPartnerOrEmployee();

        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

        return categoryProductRepository.findAllByPartner(getAuthenticatedUser().getPartner(), pageRequest)
                .map(this::mapToResponse);
    }

    @Transactional
    public CategoryProductResponse create(CategoryProductCreate request) {
        getAuthenticatedAdminPartnerOrEmployee();

        // Ambil partner dari user yang sedang login (biar aman, jangan percaya request.getPartnerId())
        Partners partner = getAuthenticatedUser().getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        // Validasi Parent Category (jika ada) - Pastiin parent-nya milik partner yang sama
        CategoryProduct parent = null;
        if (request.getParentId() != null) {
            parent = getValidatedCategory(request.getParentId(), getAuthenticatedUser());
        }

        // Cek Duplikasi Nama di partner yang sama
        if (categoryProductRepository.existsByNameAndPartnerId(request.getName(), partner.getId())) {
            throw new IllegalArgumentException("Kategori '" + request.getName() + "' sudah ada.");
        }

        CategoryProduct category = new CategoryProduct();
        category.setPartner(partner); // Pakai partner si user login
        category.setParent(parent);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setCreatedBy(getAuthenticatedUser());
        category.setCreatedAt(LocalDateTime.now());

        return mapToResponse(categoryProductRepository.save(category));
    }

    @Transactional
    public CategoryProductResponse update(Long id, CategoryProductCreate request) {
        getAuthenticatedAdminPartnerOrEmployee();
        
        // Cari kategori yang mau diupdate + validasi kepemilikan partner
        CategoryProduct category = getValidatedCategory(id, getAuthenticatedUser());

        // Jika mau ganti Parent, validasi lagi parent barunya milik partner yang sama atau nggak
        if (request.getParentId() != null) {
            category.setParent(getValidatedCategory(request.getParentId(), getAuthenticatedUser()));
        } else {
            category.setParent(null);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setUpdatedBy(getAuthenticatedUser());
        category.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(categoryProductRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        getAuthenticatedAdminPartnerOrEmployee();

        CategoryProduct category = getValidatedCategory(id, getAuthenticatedUser());
        categoryProductRepository.delete(category);
    }

    public CategoryProductResponse mapToResponse(CategoryProduct categoryProduct) {
        if (categoryProduct == null) return null;

        CategoryProductResponse response = new CategoryProductResponse();
        response.setId(categoryProduct.getId());
        response.setName(categoryProduct.getName());
        response.setDescription(categoryProduct.getDescription());
        response.setSortOrder(categoryProduct.getSortOrder());

        if (categoryProduct.getPartner() != null) {
            PartnerSimpleDto ptDto = new PartnerSimpleDto();
            ptDto.setId(categoryProduct.getPartner().getId());
            ptDto.setName(categoryProduct.getPartner().getName());
            response.setPartnerId(ptDto);
        }

        if (categoryProduct.getParent() != null) {
            CategoryProductResponse.ParentDto pDto = new CategoryProductResponse.ParentDto();
            pDto.setId(categoryProduct.getParent().getId());
            pDto.setName(categoryProduct.getParent().getName());
            response.setParent(pDto);
        }
        response.setCreatedBy(mapUserToDto(categoryProduct.getCreatedBy()));
        response.setUpdatedBy(mapUserToDto(categoryProduct.getUpdatedBy()));
        response.setDeletedBy(mapUserToDto(categoryProduct.getDeletedBy()));

        return response;
    }
    private UserSimpleDto mapUserToDto(User user) {
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user. getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}