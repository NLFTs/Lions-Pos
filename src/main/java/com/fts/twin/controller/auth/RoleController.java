package com.fts.twin.controller.auth;

import com.fts.twin.dto.request.user.AssignPermissionsRequest;
import com.fts.twin.dto.request.user.CreateRoleRequest;
import com.fts.twin.dto.request.user.UpdateRoleRequest;
import com.fts.twin.dto.response.PermissionResponse;
import com.fts.twin.dto.response.ResData;
import com.fts.twin.dto.response.RoleResponse;
import com.fts.twin.service.auth.RoleService;
import com.fts.twin.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
/**
 * Provides full CRUD operations for roles plus permission assignment
 * and available permissions listing. Purely decoupled from authentication layer.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;

    /**
     * GET /api/v1/roles/permissions
     * Returns all available permissions grouped by module. Use this for the role matrix UI.
     */
    @GetMapping("/permissions")
    public ResponseEntity<ResData<Map<String, List<PermissionResponse>>>> allPermissions() {
        log.info("[GET] /api/v1/roles/permissions");
        return ResponseBuilder.ok(roleService.getAllPermissionsGrouped());
    }

    @GetMapping
    public ResponseEntity<ResData<List<RoleResponse>>> index() {
        log.info("[GET] /api/v1/roles");
        return ResponseBuilder.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResData<RoleResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/roles/{}", id);
        return ResponseBuilder.ok(roleService.findById(id));
    }

    /**
     * POST /api/v1/roles
     * Create a new role for the current partner scope.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role.store')")
    public ResponseEntity<ResData<RoleResponse>> store(@Valid @RequestBody CreateRoleRequest request) {
        log.info("[POST] /api/v1/roles slug={}", request.getSlug());
        return ResponseBuilder.created(roleService.create(request));
    }

    /**
     * PUT /api/v1/roles/{id}
     * Update an existing role's name or slug by ID.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role.update')")
    public ResponseEntity<ResData<RoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("[PUT] /api/v1/roles/{}", id);
        return ResponseBuilder.ok(roleService.update(id, request));
    }

    /**
     * DELETE /api/v1/roles/{id}
     * Delete a role by ID. Restricted for system-critical roles.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role.delete')")
    public ResponseEntity<ResData<Void>> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/roles/{}", id);
        roleService.delete(id);
        return ResponseBuilder.ok();
    }

    /**
     * PUT /api/v1/roles/{id}/permissions
     * Sync permissions to a specific role.
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role.update')")
    public ResponseEntity<ResData<RoleResponse>> assignPermissions(
            @PathVariable Long id,
            @RequestBody AssignPermissionsRequest request) {
        log.info("[PUT] /api/v1/roles/{}/permissions", id);
        return ResponseBuilder.ok(roleService.assignPermissions(id, request));
    }
}