package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.BranchWarehousesRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.inventory.BranchWarehouses;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.inventory.BranchWarehousesRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchWarehousesService {

    private final BranchWarehousesRepository branchWarehousesRepository;
    private final BranchesRepository branchesRepository;
    private final WarehousesRepository warehousesRepository;

    // GET BY BRANCH
    public List<BranchWarehouses> findByBranchId(Long branchesId) {
        return branchWarehousesRepository.findByBranchesId(branchesId);
    }

    // GET BY WAREHOUSE
    public List<BranchWarehouses> findByWarehouseId(Long warehousesId) {
        return branchWarehousesRepository.findByWarehousesId(warehousesId);
    }

    // ASSIGN
    public BranchWarehouses assign(BranchWarehousesRequestDTO request) {
        Branches branch = branchesRepository.findById(request.getBranchesId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchesId()));

        Warehouses warehouse = warehousesRepository.findById(request.getWarehousesId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehousesId()));

        if (branchWarehousesRepository.existsByBranchesIdAndWarehousesId(request.getBranchesId(), request.getWarehousesId())) {
            throw new IllegalArgumentException("Warehouse already assigned to this branch");
        }

        BranchWarehouses bw = new BranchWarehouses();
        bw.setBranches(branch);
        bw.setWarehouses(warehouse);
        bw.setCreatedAt(LocalDateTime.now());

        return branchWarehousesRepository.save(bw);
    }

    // UNASSIGN
    public void unassign(Long id) {
        if (!branchWarehousesRepository.existsById(id)) {
            throw new ResourceNotFoundException("BranchWarehouse", id);
        }
        branchWarehousesRepository.deleteById(id);
    }
}