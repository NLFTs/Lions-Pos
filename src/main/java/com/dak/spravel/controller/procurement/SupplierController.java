package com.dak.spravel.controller.procurement;

import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.service.procurement.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // --- ENDPOINT SUPER ADMIN ---

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('supplier.admin')")
    public ResponseEntity<List<Supplier>> findAllAdmin() {
        log.info("[GET] /api/v1/suppliers/admin/all - Access by Super Admin");
        return ResponseEntity.ok(supplierService.findAllAdmin());
    }

    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('supplier.admin')")
    public ResponseEntity<Page<Supplier>> findPageAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/suppliers/admin/page - Access by Super Admin, Page: {}", page);
        return ResponseEntity.ok(supplierService.findPageAdmin(page, size));
    }

    // --- ENDPOINT PARTNER ---

    @GetMapping
    @PreAuthorize("hasAuthority('supplier.index')")
    public ResponseEntity<List<Supplier>> findAll() {
        return ResponseEntity.ok(supplierService.findAllByPartner());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('supplier.store')")
    public ResponseEntity<Supplier> create(@Valid @RequestBody Supplier supplier) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(supplier));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}