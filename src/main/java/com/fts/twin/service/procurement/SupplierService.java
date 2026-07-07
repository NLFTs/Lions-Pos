package com.fts.twin.service.procurement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fts.twin.handler.ResourceNotFoundException;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.procurement.Supplier;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.procurement.SupplierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    // ─── PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // KUNCI DINAMIS: Cek hak akses dari database berdasarkan centang matriks UI Nuxt lu
    private void checkPermission(User user, String permissionSlug) {
        // Raja Super Admin (partner null) bypass seluruh jenis gate permission
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

    // ─── MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private Supplier getValidatedSupplier(Long id, User currentUser) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        // Super Admin global bebas bypass pengecekan tenant ID
        if (currentUser.getPartner() == null) {
            return supplier;
        }

        if (supplier.getPartner() == null || !supplier.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Data Supplier ini bukan milik partner Anda.");
        }

        return supplier;
    }

    // ─── MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<Supplier> findAllAdmin() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return supplierRepository.findAll(Sort.by("id").descending());
    }

    public Page<Supplier> findPageAdmin(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return supplierRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION MATRIKS)

    public List<Supplier> findAllByPartner() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "supplier.index"); // Saring via permission index vendor

        // Handling Super Admin Global: Tarik semua riwayat supplier tanpa filter tenant
        if (currentUser.getPartner() == null) {
            return supplierRepository.findAll(Sort.by("id").descending());
        }

        return supplierRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    // CREATE SUPPLIER
    @Transactional
    public Supplier create(Supplier supplier) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "supplier.store"); // Siapapun boleh input data supplier baru asal diizinkan

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan mendaftarkan data supplier langsung.");
        }

        if (supplierRepository.existsByNameAndPartnerIdAndDeletedAtIsNull(supplier.getName(), currentUser.getPartner().getId())) {
            throw new IllegalArgumentException("Gagal: Supplier dengan nama '" + supplier.getName() + "' sudah terdaftar di sistem.");
        }

        supplier.setPartner(currentUser.getPartner());
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setCreatedBy(currentUser);

        return supplierRepository.save(supplier);
    }

    // UPDATE SUPPLIER
    @Transactional
    public Supplier update(Long id, Supplier updatedData) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "supplier.update");

        Supplier supplier = getValidatedSupplier(id, currentUser);

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

    // DELETE SUPPLIER
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "supplier.delete");

        Supplier supplier = getValidatedSupplier(id, currentUser);

        supplier.setDeletedAt(LocalDateTime.now());
        supplier.setDeletedBy(currentUser);
        supplierRepository.save(supplier);
    }
}