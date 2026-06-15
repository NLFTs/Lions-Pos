package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.BranchWarehousesRequestDTO;
import com.dak.spravel.model.inventory.BranchWarehouses;
import com.dak.spravel.service.inventory.BranchWarehousesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branch-warehouses")
@RequiredArgsConstructor
public class BranchWarehousesController {

    private final BranchWarehousesService branchWarehousesService;

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('branch_warehouse.admin')") // Sesuaikan authority dengan seeder
    public ResponseEntity<List<BranchWarehouses>> findAllAdmin() {
        log.info("[GET] /api/v1/branch-warehouses/admin/all - Request by Admin");
        return ResponseEntity.ok(branchWarehousesService.findAllAdmin());
    }

    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('branch_warehouse.admin')")
    public ResponseEntity<Page<BranchWarehouses>> findPageAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/branch-warehouses/admin/page - page: {}, size: {}", page, size);
        return ResponseEntity.ok(branchWarehousesService.findPageAdmin(page, size));
    }

    @GetMapping("/branch/{branchesId}")
    @PreAuthorize("hasAuthority('branch_warehouse.index')")
    public ResponseEntity<List<BranchWarehouses>> getByBranch(@PathVariable Long branchesId) {
        log.info("[GET] /api/v1/branch-warehouses/branch/{}", branchesId);
        return ResponseEntity.ok(branchWarehousesService.findByBranchId(branchesId));
    }

    @GetMapping("/warehouse/{warehousesId}")
    @PreAuthorize("hasAuthority('branch_warehouse.index')")
    public ResponseEntity<List<BranchWarehouses>> getByWarehouse(@PathVariable Long warehousesId) {
        log.info("[GET] /api/v1/branch-warehouses/warehouse/{}", warehousesId);
        return ResponseEntity.ok(branchWarehousesService.findByWarehouseId(warehousesId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('branch_warehouse.store')")
    public ResponseEntity<BranchWarehouses> assign(@Valid @RequestBody BranchWarehousesRequestDTO request) {
        log.info("[POST] /api/v1/branch-warehouses assign branchId={} warehouseId={}",
                request.getBranchesId(), request.getWarehousesId());
        return ResponseEntity.status(HttpStatus.CREATED).body(branchWarehousesService.assign(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('branch_warehouse.delete')")
    public ResponseEntity<Void> unassign(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/branch-warehouses/{}", id);
        branchWarehousesService.unassign(id);
        return ResponseEntity.noContent().build();
    }
}