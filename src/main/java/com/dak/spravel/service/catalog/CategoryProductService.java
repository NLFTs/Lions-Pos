package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.CategoryProductCreate;
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
                .anyMatch(role -> role.getSlug().equals("super_admin"));
    }

    private CategoryProduct getValidatedCategory(Long id, User currentUser) {
        CategoryProduct category = categoryProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));

        // Validasi Ownership
        if (!isAdmin(currentUser) && !category.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access Denied: Kamu tidak memiliki akses ke kategori ini");
        }
        return category;
    }

    // --- MAIN METHODS ---

    public List<CategoryProduct> findAll() {
        User currentUser = getAuthenticatedUser();
        Sort sort = Sort.by("name").ascending();

        if (isAdmin(currentUser)) {
            return categoryProductRepository.findAll(sort);
        }
        return categoryProductRepository.findAllByPartner(currentUser.getPartner(), sort);
    }

    public Page<CategoryProduct> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

        if (isAdmin(currentUser)) {
            return categoryProductRepository.findAll(pageRequest);
        }
        return categoryProductRepository.findAllByPartner(currentUser.getPartner(), pageRequest);
    }

    @Transactional
    public CategoryProduct create(CategoryProductCreate request) {
        User currentUser = getAuthenticatedUser();

        // 1. Ambil & Validasi Partner
        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        if (!isAdmin(currentUser) && !partner.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Kamu tidak memiliki akses untuk menambah kategori ke partner ini");
        }

        // 2. Validasi Parent Category (jika ada)
        CategoryProduct parent = null;
        if (request.getParentId() != null) {
            parent = getValidatedCategory(request.getParentId(), currentUser);
        }

        // 3. Cek Duplikasi Nama
        if (categoryProductRepository.existsByNameAndPartnerId(request.getName(), request.getPartnerId())) {
            throw new IllegalArgumentException("Kategori dengan nama '" + request.getName() + "' sudah ada di partner ini");
        }

        // 4. Save
        CategoryProduct category = new CategoryProduct();
        category.setPartner(partner);
        category.setParent(parent);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        AuditHelper.setCreated(category); 
        return categoryProductRepository.save(category);
    }

    @Transactional
    public CategoryProduct update(Long id, CategoryProductCreate request) {
        User currentUser = getAuthenticatedUser();
        CategoryProduct category = getValidatedCategory(id, currentUser);

        // Validasi: Apakah partner ID diubah ke partner yang bukan miliknya?
        Partners newPartner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));
        
        if (!isAdmin(currentUser) && !newPartner.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Tidak bisa memindahkan kategori ke partner lain tanpa akses");
        }

        category.setPartner(newPartner);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        if (request.getParentId() != null) {
            category.setParent(getValidatedCategory(request.getParentId(), currentUser));
        } else {
            category.setParent(null);
        }

        AuditHelper.setUpdated(category);
        return categoryProductRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        CategoryProduct category = getValidatedCategory(id, currentUser);
        categoryProductRepository.delete(category);
    }
}