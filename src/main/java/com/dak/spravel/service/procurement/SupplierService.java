package com.dak.spravel.service.procurement;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.procurement.SupplierRepository;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    /**
     * Helper untuk validasi User & Role (Perintah 3: Super Admin Dilarang CRUD)
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        // Proteksi: Admin & Super Admin dilarang masuk ke fungsi CRUD Partner
        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola data Supplier.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    // --- PERINTAH 1: INDEX ALL KHUSUS SUPER ADMIN ---
    public List<Supplier> findAllAdmin() {
        return supplierRepository.findAll(Sort.by("id").descending());
    }

    // --- PERINTAH 2: PAGINATED KHUSUS SUPER ADMIN ---
    public Page<Supplier> findPageAdmin(int page, int size) {
        return supplierRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // --- FUNGSI UNTUK PARTNER ---

    public List<Supplier> findAllByPartner() {
        User currentUser = getAuthenticatedUser();
        return supplierRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        User currentUser = getAuthenticatedUser();

        // Cek duplikasi nama
        if (supplierRepository.existsByNameAndPartnerIdAndDeletedAtIsNull(supplier.getName(), currentUser.getPartner().getId())) {
            throw new IllegalArgumentException("Supplier dengan nama '" + supplier.getName() + "' sudah terdaftar.");
        }

        supplier.setPartner(currentUser.getPartner());
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setCreatedBy(currentUser);

        return supplierRepository.save(supplier);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getPartner().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Supplier tidak ditemukan atau milik partner lain."));

        // Soft Delete (Sesuai model kamu yang ada deletedAt)
        supplier.setDeletedAt(LocalDateTime.now());
        supplier.setDeletedBy(currentUser);
        supplierRepository.save(supplier);
    }
}