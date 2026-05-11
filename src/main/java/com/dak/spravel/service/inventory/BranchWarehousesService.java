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

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Proteksi Admin/Super Admin
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));

        if (isAdmin) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Branch Warehouse.");
        }

        return user;
    }

    // GET BY BRANCH
    public List<BranchWarehouses> findByBranchId(Long branchesId) {
        User currentUser = getAuthenticatedUser();
        
        // Cari branch & validasi partner ID sekaligus
        branchesRepository.findById(branchesId)
                .filter(b -> b.getPartners().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda."));

        return branchWarehousesRepository.findByBranchesId(currentUser.getPartner().getId());
    }

    // GET BY WAREHOUSE
    public List<BranchWarehouses> findByWarehouseId(Long warehousesId) {
        User currentUser = getAuthenticatedUser();

        warehousesRepository.findById(warehousesId)
                .filter(w -> w.getPartners().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda."));

        return branchWarehousesRepository.findByWarehousesId(warehousesId);
    }

    // ASSIGN
    @Transactional
    public BranchWarehouses assign(BranchWarehousesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        Partners partner = currentUser.getPartner();

        Branches branch = branchesRepository.findById(request.getBranchesId())
                .filter(b -> b.getPartners().getId().equals(partner.getId()))
                .orElseThrow(() -> new RuntimeException("Branch bukan milik partner Anda."));

        Warehouses warehouse = warehousesRepository.findById(request.getWarehousesId())
                .filter(w -> w.getPartners().getId().equals(partner.getId()))
                .orElseThrow(() -> new RuntimeException("Warehouse bukan milik partner Anda."));

        if (branchWarehousesRepository.existsByBranchesIdAndWarehousesId(branch, warehouse)) {
            throw new IllegalArgumentException("Warehouse sudah di-assign ke branch ini.");
        }

        BranchWarehouses bw = new BranchWarehouses();
        bw.setBranches(branch);
        bw.setWarehouses(warehouse);
        bw.setCreatedAt(LocalDateTime.now());

        return branchWarehousesRepository.save(bw);
    }

    // UNASSIGN
    @Transactional
    public void unassign(Long id) {
        User currentUser = getAuthenticatedUser();

        BranchWarehouses bw = branchWarehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BranchWarehouse", id));

        // Validasi kepemilikan
        if (!bw.getBranches().getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Data bukan milik partner Anda.");
        }

        branchWarehousesRepository.delete(bw);
    }
}