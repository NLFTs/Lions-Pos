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

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // PERINTAH 3: Admin & Super Admin dilarang CRUD data partner
        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola data Warehouse.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    // --- PERINTAH 1 & 2: ADMIN GLOBAL ---

    public List<Warehouses> findAllAdmin() {
        return warehousesRepository.findAll(Sort.by("id").descending());
    }

    public Page<Warehouses> findPageAdmin(int page, int size) {
        return warehousesRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // --- FUNGSI UNTUK PARTNER ---

    public List<Warehouses> findAllByPartner() {
        User currentUser = getAuthenticatedUser();
        return warehousesRepository.findByPartnersIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    @Transactional
    public Warehouses create(Warehouses warehouse) {
        User currentUser = getAuthenticatedUser();

        if (warehousesRepository.existsByNameAndPartnersIdAndDeletedAtIsNull(warehouse.getName(), currentUser.getPartner().getId())) {
            throw new IllegalArgumentException("Nama warehouse sudah digunakan di partner Anda.");
        }

        warehouse.setPartners(currentUser.getPartner());
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setCreatedBy(currentUser);

        return warehousesRepository.save(warehouse);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Warehouses warehouse = warehousesRepository.findById(id)
                .filter(w -> w.getPartners().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan atau milik partner lain."));

        warehouse.setDeletedAt(LocalDateTime.now());
        warehouse.setDeletedBy(currentUser);
        warehousesRepository.save(warehouse);
    }
    // Fungsi Pagination untuk Partner
    public Page<Warehouses> findPageByPartner(int page, int size) {
        User currentUser = getAuthenticatedUser();
        // panggil method repository findByPartnersIdAndDeletedAtIsNull
        return warehousesRepository.findByPartnersIdAndDeletedAtIsNull(
                currentUser.getPartner().getId(),
                PageRequest.of(page, size, Sort.by("id").descending())
        );
    }
}