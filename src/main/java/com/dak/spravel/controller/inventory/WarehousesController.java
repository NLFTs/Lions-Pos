package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.WarehousesRequestDTO;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.service.inventory.WarehousesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehousesController {

    private final WarehousesService warehousesService;

    @GetMapping
    @PreAuthorize("hasAuthority('warehouse.index')")
    public ResponseEntity<List<Warehouses>> index() {
        log.info("[GET] /api/v1/warehouses");
        return ResponseEntity.ok(warehousesService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('warehouse.index')")
    public ResponseEntity<Page<Warehouses>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/warehouses/page page={} size={}", page, size);
        return ResponseEntity.ok(warehousesService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.show')")
    public ResponseEntity<Warehouses> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/warehouses/{}", id);
        return ResponseEntity.ok(warehousesService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('warehouse.store')")
    public ResponseEntity<Warehouses> store(@Valid @RequestBody WarehousesRequestDTO request) {
        log.info("[POST] /api/v1/warehouses name={}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(warehousesService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.update')")
    public ResponseEntity<Warehouses> update(
            @PathVariable Long id,
            @Valid @RequestBody WarehousesRequestDTO request) {
        log.info("[PUT] /api/v1/warehouses/{}", id);
        return ResponseEntity.ok(warehousesService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/warehouses/{}", id);
        warehousesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}