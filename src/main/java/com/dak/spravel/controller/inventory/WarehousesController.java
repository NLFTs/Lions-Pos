package com.dak.spravel.controller.inventory;

import java.util.List;

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

import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.service.inventory.WarehousesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehousesController {

    private final WarehousesService warehousesService;

    // --- SUPER ADMIN ---
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('warehouse.admin')")
    public ResponseEntity<List<Warehouses>> findAllAdmin() {
        log.info("[GET] /api/v1/warehouses/admin/all - Superadmin access");
        return ResponseEntity.ok(warehousesService.findAllAdmin());
    }

    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('warehouse.admin')")
    public ResponseEntity<Page<Warehouses>> findPageAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/warehouses/admin/page - Superadmin access, Page: {}", page);
        return ResponseEntity.ok(warehousesService.findPageAdmin(page, size));
    }

    // --- PARTNER ---
    @GetMapping
    @PreAuthorize("hasAuthority('warehouse.index')")
    public ResponseEntity<List<Warehouses>> findAll() {
        return ResponseEntity.ok(warehousesService.findAllByPartner());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('warehouse.store')")
    public ResponseEntity<Warehouses> create(@Valid @RequestBody Warehouses warehouse) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehousesService.create(warehouse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehousesService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('warehouse.index')")
    public ResponseEntity<Page<Warehouses>> findPageByPartner(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    log.info("[GET] /api/v1/warehouses/page - Access by Partner, Page: {}", page);
    return ResponseEntity.ok(warehousesService.findPageByPartner(page, size));
}
}