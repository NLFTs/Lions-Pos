package com.dak.spravel.controller.procurement;

import com.dak.spravel.dto.request.procurement.SupplierRequestDTO;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.service.procurement.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('supplier.index')")
    public ResponseEntity<List<Supplier>> index() {
        log.info("[GET] /api/v1/suppliers");
        return ResponseEntity.ok(supplierService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('supplier.index')")
    public ResponseEntity<Page<Supplier>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/suppliers/page page={} size={}", page, size);
        return ResponseEntity.ok(supplierService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier.show')")
    public ResponseEntity<Supplier> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/suppliers/{}", id);
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('supplier.store')")
    public ResponseEntity<Supplier> store(@Valid @RequestBody SupplierRequestDTO request) {
        log.info("[POST] /api/v1/suppliers name={}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier.update')")
    public ResponseEntity<Supplier> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequestDTO request) {
        log.info("[PUT] /api/v1/suppliers/{}", id);
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/suppliers/{}", id);
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}