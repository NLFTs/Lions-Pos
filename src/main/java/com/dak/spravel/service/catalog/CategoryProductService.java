package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.CategoryProductCreate;
import com.dak.spravel.dto.response.catalogresponse.CategoryProductResponse;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryProductService {

    private final CategoryProductRepository categoryProductRepository;
    private final PartnerRepository partnersRepository;
    private final UserRepository userRepository;

    // ================= AUTH =================

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin")
                        || role.getSlug().equals("admin"));
    }

    private CategoryProduct getValidatedCategory(Long id, User currentUser) {
        CategoryProduct category = categoryProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));

        if (currentUser.getPartner() == null ||
                !category.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: beda partner");
        }

        return category;
    }

    // ================= READ =================

    public List<CategoryProductResponse> findAll() {
        User user = getAuthenticatedUser();

        return categoryProductRepository
                .findAllByPartner(user.getPartner(), Sort.by("name").ascending())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<CategoryProductResponse> findAll(int page, int size) {
        User user = getAuthenticatedUser();

        return categoryProductRepository
                .findAllByPartner(
                        user.getPartner(),
                        PageRequest.of(page, size, Sort.by("name").ascending())
                )
                .map(this::mapToResponse);
    }

    // ================= CREATE =================

    @Transactional
    public CategoryProductResponse create(CategoryProductCreate request) {
        User user = getAuthenticatedUser();

        Partners partner = user.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner manapun.");
        }

        CategoryProduct parent = null;
        if (request.getParentId() != null) {
            parent = getValidatedCategory(request.getParentId(), user);
        }

        if (categoryProductRepository.existsByNameAndPartnerId(request.getName(), partner.getId())) {
            throw new RuntimeException("Kategori sudah ada di partner ini");
        }

        CategoryProduct category = new CategoryProduct();
        category.setPartner(partner);
        category.setParent(parent);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        AuditHelper.setCreated(category);

        return mapToResponse(categoryProductRepository.save(category));
    }

    // ================= UPDATE =================

    @Transactional
    public CategoryProductResponse update(Long id, CategoryProductCreate request) {
        User user = getAuthenticatedUser();

        CategoryProduct category = getValidatedCategory(id, user);

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        if (request.getParentId() != null) {
            category.setParent(getValidatedCategory(request.getParentId(), user));
        } else {
            category.setParent(null);
        }

        AuditHelper.setUpdated(category);

        return mapToResponse(categoryProductRepository.save(category));
    }

    // ================= DELETE =================

    @Transactional
    public void delete(Long id) {
        User user = getAuthenticatedUser();

        CategoryProduct category = getValidatedCategory(id, user);

        categoryProductRepository.delete(category);
    }

    // ================= MAPPER =================

    private CategoryProductResponse mapToResponse(CategoryProduct category) {
        if (category == null) return null;

        CategoryProductResponse response = new CategoryProductResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSortOrder(category.getSortOrder());

        if (category.getPartner() != null) {
            PartnerSimpleDto pt = new PartnerSimpleDto();
            pt.setId(category.getPartner().getId());
            pt.setName(category.getPartner().getName());
            response.setPartner(pt);
        }

        if (category.getParent() != null) {
            CategoryProductResponse.ParentDto p = new CategoryProductResponse.ParentDto();
            p.setId(category.getParent().getId());
            p.setName(category.getParent().getName());
            response.setParent(p);
        }

        response.setCreatedBy(mapUser(category.getCreatedBy()));
        response.setUpdatedBy(mapUser(category.getUpdatedBy()));
        response.setDeletedBy(mapUser(category.getDeletedBy()));

        return response;
    }

    private UserSimpleDto mapUser(User user) {
        if (user == null) return null;

        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}