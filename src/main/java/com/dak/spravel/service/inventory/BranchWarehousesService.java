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

    // ─── PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // KUNCI UTAMA SAKTI: Bebas cek permission apa aja tanpa hardcode kasta role!
    private void checkPermission(User user, String permissionSlug) {
        // Raja Super Admin (partner null) bypass semua jenis gate permission
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
            throw new RuntimeException("Akses ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── CORE METHODS (SUDAH DISERAGAMKAN POLANYA) ──────────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<BranchWarehouses> findAllAdmin() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return branchWarehousesRepository.findAll(Sort.by("id").descending());
    }

    public Page<BranchWarehouses> findPageAdmin(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);
        return branchWarehousesRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    // KHUSUS OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION)

    public List<BranchWarehouses> findAllByPartner() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch_warehouse.index"); // Check via permission slug

        if (currentUser.getPartner() == null) {
            return branchWarehousesRepository.findAll();
        }

        return branchWarehousesRepository.findAll().stream()
                .filter(bw -> bw.getBranches() != null && bw.getBranches().getPartners() != null &&
                        bw.getBranches().getPartners().getId().equals(currentUser.getPartner().getId()))
                .toList();
    }

    public List<BranchWarehouses> findByBranchId(Long branchesId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch_warehouse.index");

        // Multi-Tenant Guard: Mencegah cross-tenant data peeking
        if (currentUser.getPartner() != null) {
            Branches branch = branchesRepository.findById(branchesId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", branchesId));
            if (branch.getPartners() == null || !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
            }
        }

        return branchWarehousesRepository.findByBranchesId(branchesId);
    }

    public List<BranchWarehouses> findByWarehouseId(Long warehousesId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch_warehouse.index");

        // Multi-Tenant Guard
        if (currentUser.getPartner() != null) {
            Warehouses warehouse = warehousesRepository.findById(warehousesId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehousesId));
            if (warehouse.getPartners() == null || !warehouse.getPartners().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda.");
            }
        }

        return branchWarehousesRepository.findByWarehousesId(warehousesId);
    }

    @Transactional
    public BranchWarehouses assign(BranchWarehousesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch_warehouse.store"); // Siapapun bisa asal dikasih izin Owner via UI
        Partners partner = currentUser.getPartner();

        Branches branch = branchesRepository.findById(request.getBranchesId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchesId()));

        Warehouses warehouse = warehousesRepository.findById(request.getWarehousesId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehousesId()));

        // Validasi kepemilikan relasi data (hanya jika dia staff partner, super admin bypass)
        if (partner != null) {
            if (branch.getPartners() == null || !branch.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Branch bukan milik partner Anda.");
            }
            if (warehouse.getPartners() == null || !warehouse.getPartners().getId().equals(partner.getId())) {
                throw new RuntimeException("Warehouse bukan milik partner Anda.");
            }
        }

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
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch_warehouse.delete"); // Sikat pake permission slug

        BranchWarehouses bw = branchWarehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BranchWarehouse", id));

        if (currentUser.getPartner() != null) {
            if (bw.getBranches() == null || bw.getBranches().getPartners() == null ||
                    !bw.getBranches().getPartners().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Data bukan milik partner Anda.");
            }
        }

        branchWarehousesRepository.delete(bw);
    }
}