package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.BranchWarehousesRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
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
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Branch Warehouse.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    // GET BY BRANCH
    public List<BranchWarehouses> findByBranchId(Long branchesId) {
        User currentUser = getAuthenticatedUser();

        Branches branch = branchesRepository.findById(branchesId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchesId));

        if (currentUser.getPartner() == null ||
                !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
        }

        return branchWarehousesRepository.findByBranchesId(branchesId);
    }

    // GET BY WAREHOUSE
    public List<BranchWarehouses> findByWarehouseId(Long warehousesId) {
        User currentUser = getAuthenticatedUser();

        Warehouses warehouse = warehousesRepository.findById(warehousesId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehousesId));

        if (currentUser.getPartner() == null ||
                !warehouse.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda.");
        }

        return branchWarehousesRepository.findByWarehousesId(warehousesId);
    }

    // ASSIGN
    public BranchWarehouses assign(BranchWarehousesRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        Branches branch = branchesRepository.findById(request.getBranchesId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchesId()));

        Warehouses warehouse = warehousesRepository.findById(request.getWarehousesId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehousesId()));

        // Validasi branch & warehouse milik partner yang sama dengan user login
        if (currentUser.getPartner() == null ||
                !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
        }

        if (!warehouse.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda.");
        }

        if (branchWarehousesRepository.existsByBranchesIdAndWarehousesId(
                request.getBranchesId(), request.getWarehousesId())) {
            throw new IllegalArgumentException("Warehouse sudah di-assign ke branch ini.");
        }

        BranchWarehouses bw = new BranchWarehouses();
        bw.setBranches(branch);
        bw.setWarehouses(warehouse);
        bw.setCreatedAt(LocalDateTime.now());

        return branchWarehousesRepository.save(bw);
    }

    // UNASSIGN
    public void unassign(Long id) {
        User currentUser = getAuthenticatedUser();

        BranchWarehouses bw = branchWarehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BranchWarehouse", id));

        if (currentUser.getPartner() == null ||
                !bw.getBranches().getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Data bukan milik partner Anda.");
        }

        branchWarehousesRepository.deleteById(id);
    }
}