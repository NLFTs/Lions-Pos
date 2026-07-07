package com.fts.twin.service.catalog;

import com.fts.twin.dto.request.catalog.CategoryProductCreate;
import com.fts.twin.dto.response.catalogresponse.CategoryProductResponse;
import com.fts.twin.dto.response.components.PartnerSimpleDto;
import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fts.twin.handler.ResourceNotFoundException;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.catalog.CategoryProduct;
import com.fts.twin.model.common.Partners;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.catalog.CategoryProductRepository;
import com.fts.twin.repository.catalog.ProductRepository;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class CategoryProductService {

    private final CategoryProductRepository categoryProductRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

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

    // ─── MULTI-TENANT GUARD (ANTI NULL POINTER) ──────────────────────────

    private CategoryProduct getValidatedCategory(Long id, User currentUser) {
        CategoryProduct category = categoryProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));

        // Super Admin global bebas bypass pengecekan tenant id
        if (currentUser.getPartner() == null) {
            return category;
        }

        if (category.getPartner() == null || !category.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Category bukan milik partner Anda");
        }

        return category;
    }

    // ─── MAIN METHODS (SUDAH DISERAGAMKAN POLANYA) ──────────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<CategoryProductResponse> findAllCategoryProduct() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return categoryProductRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<CategoryProductResponse> findAllCategoryProduct(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return categoryProductRepository.findAll(
                PageRequest.of(page, size, Sort.by("name").ascending())
        ).map(this::mapToResponse);
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION MATRIKS SLUG)

    public List<CategoryProductResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "category.index");
        
        if (currentUser.getPartner() == null) {
            return categoryProductRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        return categoryProductRepository
                .findAllByPartner(currentUser.getPartner(), Sort.by("sortOrder").ascending())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<CategoryProductResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "category.index");

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        if (currentUser.getPartner() == null) {
            return categoryProductRepository.findAll(pageable).map(this::mapToResponse);
        }

        return categoryProductRepository
                .findAllByPartner(currentUser.getPartner(), pageable)
                .map(this::mapToResponse);
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Transactional
    public CategoryProductResponse create(CategoryProductCreate request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "category.store");

        Partners partner = currentUser.getPartner();
        CategoryProduct parent = null;

        if (request.getParentId() != null) {
            parent = getValidatedCategory(request.getParentId(), currentUser);
        }

        if (partner != null && categoryProductRepository.existsByNameAndPartnerId(request.getName(), partner.getId())) {
            throw new RuntimeException("Category '" + request.getName() + "' sudah ada di partner ini.");
        }

        CategoryProduct c = new CategoryProduct();
        c.setPartner(partner);
        c.setParent(parent);
        c.setName(request.getName());
        c.setDescription(request.getDescription());
        if (request.getSortOrder() != null) {
            c.setSortOrder(request.getSortOrder());
        } else {
            int nextOrder = categoryProductRepository.countByPartner(partner) + 1;
            c.setSortOrder(nextOrder);
        }
        c.setCreatedAt(LocalDateTime.now());
        c.setCreatedBy(currentUser);

        return mapToResponse(categoryProductRepository.save(c));
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Transactional
    public CategoryProductResponse update(Long id, CategoryProductCreate request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "category.update");

        CategoryProduct c = getValidatedCategory(id, currentUser);

        if (request.getParentId() != null) {
            c.setParent(getValidatedCategory(request.getParentId(), currentUser));
        } else {
            c.setParent(null);
        }

        c.setName(request.getName());
        c.setDescription(request.getDescription());
        c.setSortOrder(request.getSortOrder());
        c.setUpdatedAt(LocalDateTime.now());
        c.setUpdatedBy(currentUser);

        return mapToResponse(categoryProductRepository.save(c));
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "category.delete");

        CategoryProduct c = getValidatedCategory(id, currentUser);

        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException(
                "Kategori '" + c.getName() + "' tidak dapat dihapus karena masih digunakan oleh " +
                productCount + " produk. Pindahkan atau ubah kategori produk tersebut terlebih dahulu."
            );
        }

        categoryProductRepository.delete(c);
    }

    // ─── PRIVATE MAPPERS & UTILS ───────────────────────────────────────────

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

        response.setCreatedAt(c.getCreatedAt());
        response.setUpdatedAt(c.getUpdatedAt());
        response.setDeletedAt(c.getDeletedAt());

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
}