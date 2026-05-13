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
public class CategoryProductService {

    private final CategoryProductRepository categoryProductRepository;
    private final UserRepository userRepository;

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

    private CategoryProduct getValidatedCategory(Long id, User currentUser) {
        CategoryProduct category = categoryProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));

        if (currentUser.getPartner() == null ||
                !category.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Kategori ini milik partner lain.");
        }
        return category;
    }

    // --- METHODS ---
    public List<CategoryProductResponse> findAllCategoryProduct() {
        getAuthenticatedSuperAdmin();
        return categoryProductRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<CategoryProductResponse> findAllCategoryProduct(int page, int size) {
        getAuthenticatedSuperAdmin();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        return categoryProductRepository.findAll(pageRequest).map(this::mapToResponse);
    }

    public List<CategoryProductResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Sort sort = Sort.by("sortOrder").ascending();

        return categoryProductRepository.findAllByPartner(currentUser.getPartner(), sort)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<CategoryProductResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

        return categoryProductRepository.findAllByPartner(currentUser.getPartner(), pageRequest)
                .map(this::mapToResponse);
    }

    @Transactional
    public CategoryProductResponse create(CategoryProductCreate request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();

        if (partner == null) throw new RuntimeException("User tidak terasosiasi dengan Partner.");

        CategoryProduct parent = (request.getParentId() != null) ? getValidatedCategory(request.getParentId(), currentUser) : null;

        if (categoryProductRepository.existsByNameAndPartnerId(request.getName(), partner.getId())) {
            throw new IllegalArgumentException("Kategori '" + request.getName() + "' sudah ada.");
        }

        CategoryProduct category = new CategoryProduct();
        category.setPartner(partner);
        category.setParent(parent);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setCreatedBy(currentUser);
        category.setCreatedAt(LocalDateTime.now());

        return mapToResponse(categoryProductRepository.save(category));
    }

    @Transactional
    public CategoryProductResponse update(Long id, CategoryProductCreate request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Category Product.");
        }

        // Cari kategori yang mau diupdate + validasi kepemilikan partner
        CategoryProduct category = getValidatedCategory(id, currentUser);

        // Jika mau ganti Parent, validasi lagi parent barunya milik partner yang sama atau nggak
        if (request.getParentId() != null) {
            category.setParent(getValidatedCategory(request.getParentId(), currentUser));
        } else {
            category.setParent(null);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setUpdatedBy(currentUser);
        category.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(categoryProductRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Category Product.");
        }

        CategoryProduct category = getValidatedCategory(id, currentUser);
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