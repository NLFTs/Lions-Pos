package com.dak.spravel.service.inventory;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
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
public class WarehousesService {

    private final WarehousesRepository warehousesRepository;
    private final UserRepository userRepository;

    // =========================
    // AUTH HELPERS (POLA BARU)
    // =========================
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
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee-partners"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<Warehouses> findAllAdmin() {
        getAuthenticatedSuperAdmin();
        return warehousesRepository.findAll(Sort.by("id").descending());
    }

    public Page<Warehouses> findPageAdmin(int page, int size) {
        getAuthenticatedSuperAdmin();
        return warehousesRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<Warehouses> findAllByPartner() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return warehousesRepository.findByPartnersIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    @Transactional
    public Warehouses create(Warehouses warehouse) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        if (warehousesRepository.existsByNameAndPartnersIdAndDeletedAtIsNull(warehouse.getName(), currentUser.getPartner().getId())) {
            throw new IllegalArgumentException("Warehouse sudah terdaftar.");
        }

        warehouse.setPartners(currentUser.getPartner());
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setCreatedBy(currentUser);

        return warehousesRepository.save(warehouse);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Warehouses warehouse = warehousesRepository.findById(id)
                .filter(w -> w.getPartners().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Warehouse tidak ditemukan"));

        warehouse.setDeletedAt(LocalDateTime.now());
        warehouse.setDeletedBy(currentUser);
        warehousesRepository.save(warehouse);
    }
}