package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.BranchWarehousesRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.BranchWarehouses;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.BranchWarehousesRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
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
public class BranchWarehousesService {

    private final BranchWarehousesRepository branchWarehousesRepository;
    private final BranchesRepository branchesRepository;
    private final WarehousesRepository warehousesRepository;
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

    // KHUSUS SUPER ADMIN

    public List<BranchWarehouses> findAllAdmin() {
        getAuthenticatedSuperAdmin();
        return branchWarehousesRepository.findAll(Sort.by("id").descending());
    }

    public Page<BranchWarehouses> findPageAdmin(int page, int size) {
        getAuthenticatedSuperAdmin();
        return branchWarehousesRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // KHUSUS PARTNER / EMPLOYEE

    public List<BranchWarehouses> findAllByPartner() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        // Filter manual menggunakan Stream karena Repository tidak diubah
        return branchWarehousesRepository.findAll().stream()
                .filter(bw -> bw.getBranches() != null &&
                        bw.getBranches().getPartners().getId().equals(currentUser.getPartner().getId()))
                .toList();
    }

    public List<BranchWarehouses> findByBranchId(Long branchesId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Branches branch = branchesRepository.findById(branchesId)
                .filter(b -> b.getPartners().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda."));

        if (branch == null) throw new RuntimeException("Branch tidak ditemukan.");

        return branchWarehousesRepository.findByBranchesId(branchesId);
    }

    public List<BranchWarehouses> findByWarehouseId(Long warehousesId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        warehousesRepository.findById(warehousesId)
                .filter(w -> w.getPartners().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda."));

        return branchWarehousesRepository.findByWarehousesId(warehousesId);
    }

    @Transactional
    public BranchWarehouses assign(BranchWarehousesRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partner = currentUser.getPartner();

        if (partner == null) throw new RuntimeException("User tidak terasosiasi dengan Partner.");

        Branches branch = branchesRepository.findById(request.getBranchesId())
                .filter(b -> b.getPartners().getId().equals(partner.getId()))
                .orElseThrow(() -> new RuntimeException("Branch bukan milik partner Anda."));

        Warehouses warehouse = warehousesRepository.findById(request.getWarehousesId())
                .filter(w -> w.getPartners().getId().equals(partner.getId()))
                .orElseThrow(() -> new RuntimeException("Warehouse bukan milik partner Anda."));

        if (branchWarehousesRepository.existsByBranchesAndWarehouses(branch, warehouse)) {
            throw new IllegalArgumentException("Warehouse sudah di-assign ke branch ini.");
        }

        BranchWarehouses bw = new BranchWarehouses();
        bw.setBranches(branch);
        bw.setWarehouses(warehouse);
        bw.setCreatedAt(LocalDateTime.now());
        bw.setCreatedBy(currentUser);

        return branchWarehousesRepository.save(bw);
    }

    @Transactional
    public void unassign(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        BranchWarehouses bw = branchWarehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BranchWarehouse", id));

        // Validasi kepemilikan partner (lewat relasi Branches -> Partners)
        if (bw.getBranches() == null || bw.getBranches().getPartners() == null ||
                !bw.getBranches().getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Data bukan milik partner Anda.");
        }

        branchWarehousesRepository.delete(bw);
    }
}