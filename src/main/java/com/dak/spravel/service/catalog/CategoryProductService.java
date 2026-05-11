package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.CategoryProductCreate;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
// import com.dak.spravel.repository.common.PartnerRepository;
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
    // private final PartnerRepository partnersRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        // VALIDASI 1: Admin dilarang masuk (Block Total)
        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Category Product.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        // Cek slug super_admin atau admin (sesuaikan dengan seeder lo)
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    /**
     * Helper untuk validasi apakah sebuah kategori milik partner si user yang sedang login
     */
    private CategoryProduct getValidatedCategory(Long id, User currentUser) {
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

    public List<CategoryProduct> findAll() {
        User currentUser = getAuthenticatedUser();
        Sort sort = Sort.by("name").ascending();

        // Admin sudah mental di getAuthenticatedUser, jadi di sini pasti user partner
        return categoryProductRepository.findAllByPartner(currentUser.getPartner(), sort);
    }

    public Page<CategoryProduct> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

        return categoryProductRepository.findAllByPartner(currentUser.getPartner(), pageRequest);
    }

    @Transactional
    public CategoryProduct create(CategoryProductCreate request) {
        User currentUser = getAuthenticatedUser();

        // Ambil partner dari user yang sedang login (biar aman, jangan percaya request.getPartnerId())
        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        // Validasi Parent Category (jika ada) - Pastiin parent-nya milik partner yang sama
        CategoryProduct parent = null;
        if (request.getParentId() != null) {
            parent = getValidatedCategory(request.getParentId(), currentUser);
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

        AuditHelper.setCreated(category);
        return categoryProductRepository.save(category);
    }

    @Transactional
    public CategoryProduct update(Long id, CategoryProductCreate request) {
        User currentUser = getAuthenticatedUser();

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