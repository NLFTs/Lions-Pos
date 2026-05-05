package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.BranchesRequestDTO;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.service.inventory.BranchesService;
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
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchesController {

    private final BranchesService branchesService;

    @GetMapping
    @PreAuthorize("hasAuthority('branch.index')")
    public ResponseEntity<List<Branches>> index() {
        log.info("[GET] /api/v1/branches");
        return ResponseEntity.ok(branchesService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('branch.index')")
    public ResponseEntity<Page<Branches>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/branches/page page={} size={}", page, size);
        return ResponseEntity.ok(branchesService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('branch.show')")
    public ResponseEntity<Branches> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/branches/{}", id);
        return ResponseEntity.ok(branchesService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('branch.store')")
    public ResponseEntity<Branches> store(@Valid @RequestBody BranchesRequestDTO request) {
        log.info("[POST] /api/v1/branches name={}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(branchesService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('branch.update')")
    public ResponseEntity<Branches> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchesRequestDTO request) {
        log.info("[PUT] /api/v1/branches/{}", id);
        return ResponseEntity.ok(branchesService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('branch.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/branches/{}", id);
        branchesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}