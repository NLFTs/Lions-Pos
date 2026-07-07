package com.fts.twin.controller.inventory;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fts.twin.dto.request.inventory.WarehousesRequestDTO;
import com.fts.twin.dto.response.ResData;
import com.fts.twin.dto.response.inventoryresponse.WarehouseResponse;
import com.fts.twin.service.inventory.WarehousesService;
import com.fts.twin.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehousesController {

    private final WarehousesService warehousesService;

    @GetMapping("/admin")
    public ResponseEntity<ResData<Page<WarehouseResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/warehouses/admin - Superadmin access, page: {}, size: {}", page, size);
        return ResponseBuilder.ok(warehousesService.findPageAdmin(page, size));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('warehouse.index')")
    public ResponseEntity<ResData<List<WarehouseResponse>>> findAll(
            @RequestParam(required = false) Long branchId) {
        log.info("[GET] /api/v1/warehouses - Fetching all warehouses for branch ID: {}", branchId);
        return ResponseBuilder.ok(warehousesService.findAllByPartner(branchId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('warehouse.store')")
    public ResponseEntity<ResData<WarehouseResponse>> create(@Valid @RequestBody WarehousesRequestDTO request) {
        return ResponseBuilder.created(warehousesService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehousesService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.update')")
    public ResponseEntity<ResData<WarehouseResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody WarehousesRequestDTO request) {
        log.info("[PUT] /api/v1/warehouses/{}", id);
        return ResponseBuilder.ok(warehousesService.update(id, request));
    }
    
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('warehouse.index')")
    public ResponseEntity<ResData<Page<WarehouseResponse>>> findPageByPartner(
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/warehouses/page - Access by Partner, Branch ID: {}, Page: {}", branchId, page);
        return ResponseBuilder.ok(warehousesService.findPageByPartner(branchId, page, size));
    }
}