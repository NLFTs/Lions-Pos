package com.dak.spravel.controller;

import com.dak.spravel.dto.request.CreateModuleRequest;
import com.dak.spravel.dto.request.UpdateModuleRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.ModuleResponse;
import com.dak.spravel.service.ModuleService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Provides full CRUD operations for permission modules that group permissions
 * in the role matrix UI.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleController {
    private final ModuleService moduleService;

    /**
     * GET /api/v1/modules
     * List all modules ordered by slug.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('module.index')")
    public ResponseEntity<ResData<List<ModuleResponse>>> index() {
        log.info("[GET] /api/v1/modules");
        return ResponseBuilder.ok(moduleService.findAll());
    }

    /**
     * GET /api/v1/modules/{id}
     * Get a single module by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('module.show')")
    public ResponseEntity<ResData<ModuleResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/modules/{}", id);
        return ResponseBuilder.ok(moduleService.findById(id));
    }

    /**
     * POST /api/v1/modules
     * Create a new module.
     */
    @PostMapping
@PreAuthorize("hasAuthority('module.store')")
    public ResponseEntity<ResData<ModuleResponse>> store(
            @Valid @RequestBody CreateModuleRequest request) {
        log.info("[POST] /api/v1/modules slug={}", request.getSlug());
        return ResponseBuilder.created(moduleService.create(request));
    }

    /**
     * PUT /api/v1/modules/{id}
     * Updates name and description only. Slug is immutable.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('module.update')")
    public ResponseEntity<ResData<ModuleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateModuleRequest request) {
        log.info("[PUT] /api/v1/modules/{}", id);
        return ResponseBuilder.ok(moduleService.update(id, request));
    }

    /**
     * DELETE /api/v1/modules/{id}
     * Delete a module by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('module.delete')")
    public ResponseEntity<ResData<Void>> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/modules/{}", id);
        moduleService.delete(id);
        return ResponseBuilder.ok();
    }
}
