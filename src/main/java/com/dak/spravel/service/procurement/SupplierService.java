package com.dak.spravel.service.procurement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.procurement.SupplierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    // AUTH HELPERS
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        // 🛠️ MODIFIKASI: Daftarkan role "employee" agar bisa melihat data supplier
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee-partners") ||
                        role.getSlug().equalsIgnoreCase("employee"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    // 💡 HELPER BARU: Cek apakah user murni seorang Employee
    private boolean isEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("employee"));
    }

    // KHUSUS SUPER ADMIN
    public List<Supplier> findAllAdmin() {
        getAuthenticatedSuperAdmin();
        return supplierRepository.findAll(Sort.by("id").descending());
    }

    public Page<Supplier> findPageAdmin(int page, int size) {
        getAuthenticatedSuperAdmin();
        return supplierRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // KHUSUS PARTNER / EMPLOYEE (Bisa diakses employee, Read-Only)
    public List<Supplier> findAllByPartner() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }
        return supplierRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        // 🔥 VALIDASI: Employee dilarang membuat data supplier baru
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak memiliki akses untuk menambah supplier.");
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        if (supplierRepository.existsByNameAndPartnerIdAndDeletedAtIsNull(supplier.getName(), currentUser.getPartner().getId())) {
            throw new IllegalArgumentException("Supplier dengan nama tersebut sudah ada.");
        }

        supplier.setPartner(currentUser.getPartner());
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setCreatedBy(currentUser);

        return supplierRepository.save(supplier);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        // 🔥 VALIDASI: Employee dilarang menghapus data supplier
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak memiliki akses untuk menghapus supplier.");
        }

        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getPartner().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Supplier tidak ditemukan"));

        supplier.setDeletedAt(LocalDateTime.now());
        supplier.setDeletedBy(currentUser);
        supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, Supplier updatedData) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        // 🔥 VALIDASI: Employee dilarang mengedit/mengubah data supplier
        if (isEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Employee tidak memiliki akses untuk mengubah data supplier.");
        }

        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getPartner().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Supplier tidak ditemukan atau bukan milik partner Anda"));

        if (updatedData.getName() != null && !updatedData.getName().isBlank()) {
            supplier.setName(updatedData.getName());
        }
        if (updatedData.getPhone() != null) supplier.setPhone(updatedData.getPhone());
        if (updatedData.getEmail() != null) supplier.setEmail(updatedData.getEmail());
        if (updatedData.getAddress() != null) supplier.setAddress(updatedData.getAddress());
        if (updatedData.getNotes() != null) supplier.setNotes(updatedData.getNotes());

        supplier.setUpdatedAt(LocalDateTime.now());
        supplier.setUpdatedBy(currentUser);
        return supplierRepository.save(supplier);
    }
}