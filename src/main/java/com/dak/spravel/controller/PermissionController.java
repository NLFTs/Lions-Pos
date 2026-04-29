package com.dak.spravel.controller;

import com.dak.spravel.dto.request.CreatePermissionRequest;
import com.dak.spravel.dto.request.UpdatePermissionRequest;
import com.dak.spravel.dto.response.PermissionResponse;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.service.PermissionService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Provides full CRUD operations for permissions with flat and grouped-by-module endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    /**
     * GET /api/v1/permissions/grouped
     * Returns all permissions grouped by module — for the frontend matrix UI.
     */
    @GetMapping("/grouped")
    @PreAuthorize("hasAuthority('permission.index')")
    public ResponseEntity<ResData<Map<String, List<PermissionResponse>>>> grouped() {
        log.info("[GET] /api/v1/permissions/grouped");
        return ResponseBuilder.ok(permissionService.findAllGrouped());
    }

    /**
     * GET /api/v1/permissions
     * Returns flat ordered list of all permissions.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('permission.index')")
    public ResponseEntity<ResData<List<PermissionResponse>>> index() {
        log.info("[GET] /api/v1/permissions");
        return ResponseBuilder.ok(permissionService.findAll());
    }

    /**
     * GET /api/v1/permissions/{id}
     * Returns a single permission by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission.show')")
    public ResponseEntity<ResData<PermissionResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/permissions/{}", id);
        return ResponseBuilder.ok(permissionService.findById(id));
    }

    /**
     * POST /api/v1/permissions
     * Creates a new permission.
     */
    @PostMapping
@PreAuthorize("hasAuthority('permission.store')")
    public ResponseEntity<ResData<PermissionResponse>> store(
            @Valid @RequestBody CreatePermissionRequest request) {
        log.info("[POST] /api/v1/permissions slug={}", request.getSlug());
        return ResponseBuilder.created(permissionService.create(request));
    }

    /**
     * PUT /api/v1/permissions/{id}
     * Updates name and/or module of a permission.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission.update')")
    public ResponseEntity<ResData<PermissionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        log.info("[PUT] /api/v1/permissions/{}", id);
        return ResponseBuilder.ok(permissionService.update(id, request));
    }

    /**
     * DELETE /api/v1/permissions/{id}
     * Deletes a permission. Roles that held this permission will lose it automatically.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission.delete')")
    public ResponseEntity<ResData<Void>> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/permissions/{}", id);
        permissionService.delete(id);
        return ResponseBuilder.ok();
    }
}
