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

    // =========================
    // AUTH HELPERS
    // =========================

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

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("admin"));

        if (!isAdmin) {
            throw new RuntimeException("Akses Ditolak: Super Admin saja");
        }

        return user;
    }

    private User getAuthenticatedOwner() {
        User user = getAuthenticatedUser();

        boolean isOwner = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("owner"));

        if (!isOwner) {
            throw new RuntimeException("Akses Ditolak: hanya Owner yang diizinkan");
        }

        if (user.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner");
        }

        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();

        boolean allowed = user.getRoles().stream()
                .anyMatch(r ->
                        r.getSlug().equalsIgnoreCase("admin-partners") ||
                                r.getSlug().equalsIgnoreCase("employee-partners") ||
                                r.getSlug().equalsIgnoreCase("owner") ||
                                r.getSlug().equalsIgnoreCase("employee")
                );

        if (!allowed) {
            throw new RuntimeException("Akses Ditolak: Role tidak diizinkan");
        }

        if (user.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner");
        }

        return user;
    }


    private CategoryProduct getValidatedCategory(Long id, User user) {
        CategoryProduct category = categoryProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));

        if (category.getPartner() == null ||
                !category.getPartner().getId().equals(user.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Category bukan milik partner Anda");
        }

        return category;
    }

    private boolean isOwner(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("owner"));
    }

    private CategoryProductResponse mapToResponse(CategoryProduct c) {
        if (c == null) return null;

        CategoryProductResponse response = new CategoryProductResponse();
        response.setId(c.getId());
        response.setName(c.getName());
        response.setDescription(c.getDescription());
        response.setSortOrder(c.getSortOrder());

        if (c.getPartner() != null) {
            PartnerSimpleDto p = new PartnerSimpleDto();
            p.setId(c.getPartner().getId());
            p.setName(c.getPartner().getName());
            response.setPartnerId(p);
        }

        if (c.getParent() != null) {
            CategoryProductResponse.ParentDto parent = new CategoryProductResponse.ParentDto();
            parent.setId(c.getParent().getId());
            parent.setName(c.getParent().getName());
            response.setParent(parent);
        }

        response.setCreatedBy(mapUserToDto(c.getCreatedBy()));
        response.setUpdatedBy(mapUserToDto(c.getUpdatedBy()));
        response.setDeletedBy(mapUserToDto(c.getDeletedBy()));

        return response;
    }

    private UserSimpleDto mapUserToDto(User user) {
        if (user == null) return null;

        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    public List<CategoryProductResponse> findAllCategoryProduct() {
        getAuthenticatedSuperAdmin();

        return categoryProductRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<CategoryProductResponse> findAllCategoryProduct(int page, int size) {
        getAuthenticatedSuperAdmin();

        return categoryProductRepository.findAll(
                PageRequest.of(page, size, Sort.by("name").ascending())
        ).map(this::mapToResponse);
    }

    // =========================
    // PARTNER / EMPLOYEE
    // =========================

    public List<CategoryProductResponse> findAll() {
        User user = getAuthenticatedAdminPartnerOrEmployee();

        return categoryProductRepository
                .findAllByPartner(user.getPartner(), Sort.by("sortOrder").ascending())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<CategoryProductResponse> findAll(int page, int size) {
        User user = getAuthenticatedAdminPartnerOrEmployee();

        return categoryProductRepository
                .findAllByPartner(
                        user.getPartner(),
                        PageRequest.of(page, size, Sort.by("name").ascending())
                )
                .map(this::mapToResponse);
    }

    // =========================
    // CREATE
    // =========================

    @Transactional
    public CategoryProductResponse create(CategoryProductCreate request) {
        User user = getAuthenticatedOwner();

        Partners partner = user.getPartner();

        CategoryProduct parent = null;

        if (request.getParentId() != null) {
            parent = getValidatedCategory(request.getParentId(), user);
        }

        if (categoryProductRepository.existsByNameAndPartnerId(
                request.getName(),
                partner.getId()
        )) {
            throw new RuntimeException("Category sudah ada");
        }

        CategoryProduct c = new CategoryProduct();
        c.setPartner(partner);
        c.setParent(parent);
        c.setName(request.getName());
        c.setDescription(request.getDescription());
        c.setSortOrder(request.getSortOrder());
        c.setCreatedAt(LocalDateTime.now());
        c.setCreatedBy(user);

        return mapToResponse(categoryProductRepository.save(c));
    }

    // =========================
    // UPDATE
    // =========================

    @Transactional
    public CategoryProductResponse update(Long id, CategoryProductCreate request) {
        User user = getAuthenticatedOwner();

        CategoryProduct c = getValidatedCategory(id, user);

        if (request.getParentId() != null) {
            c.setParent(getValidatedCategory(request.getParentId(), user));
        } else {
            c.setParent(null);
        }

        c.setName(request.getName());
        c.setDescription(request.getDescription());
        c.setSortOrder(request.getSortOrder());
        c.setUpdatedAt(LocalDateTime.now());
        c.setUpdatedBy(user);

        return mapToResponse(categoryProductRepository.save(c));
    }

    // =========================
    // DELETE
    // =========================

    @Transactional
    public void delete(Long id) {
        User user = getAuthenticatedOwner();

        CategoryProduct c = getValidatedCategory(id, user);

        categoryProductRepository.delete(c);
    }
}