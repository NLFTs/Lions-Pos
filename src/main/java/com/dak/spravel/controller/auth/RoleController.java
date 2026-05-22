package com.dak.spravel.controller.auth;

import com.dak.spravel.dto.request.user.AssignPermissionsRequest;
import com.dak.spravel.dto.request.user.CreateRoleRequest;
import com.dak.spravel.dto.request.user.UpdateRoleRequest;
import com.dak.spravel.dto.response.PermissionResponse;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.RoleResponse;
import com.dak.spravel.model.auth.User; 
import com.dak.spravel.service.auth.PermissionService;
import com.dak.spravel.service.auth.RoleService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * and available permissions listing scoped by partner.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final PermissionService permissionService;

    /**
     * GET /api/v1/roles/permissions
     * Returns all available permissions grouped by module. Use this for the role matrix UI.
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('role.index')")
    public ResponseEntity<ResData<Map<String, List<PermissionResponse>>>> allPermissions() {
        log.info("[GET] /api/v1/roles/permissions");
        return ResponseBuilder.ok(permissionService.findAllGrouped());
    }

    /**
     * GET /api/v1/roles
     * List all roles scoped by partner.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role.index')")
    public ResponseEntity<ResData<List<RoleResponse>>> index(@AuthenticationPrincipal User currentUser) {
        log.info("[GET] /api/v1/roles by user={}", currentUser.getUsername());
        return ResponseBuilder.ok(roleService.findAll(currentUser));
    }

    /**
     * GET /api/v1/roles/{id}
     * Get a specific role by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role.show')")
    public ResponseEntity<ResData<RoleResponse>> show(
            @PathVariable Long id, 
            @AuthenticationPrincipal User currentUser) {
        log.info("[GET] /api/v1/roles/{}", id);
        return ResponseBuilder.ok(roleService.findById(id, currentUser));
    }

    /**
     * POST /api/v1/roles
     * Create a new role for the current partner.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role.store')")
    public ResponseEntity<ResData<RoleResponse>> store(
            @Valid @RequestBody CreateRoleRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[POST] /api/v1/roles slug={} by user={}", request.getSlug(), currentUser.getUsername());
        return ResponseBuilder.created(roleService.create(request, currentUser));
    }

    /**
     * PUT /api/v1/roles/{id}
     * Update an existing role's name or slug by ID.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role.update')")
    public ResponseEntity<ResData<RoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[PUT] /api/v1/roles/{}", id);
        return ResponseBuilder.ok(roleService.update(id, request, currentUser));
    }

    /**
     * DELETE /api/v1/roles/{id}
     * Delete a role by ID. Restricted for system-critical roles like 'admin'.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role.delete')")
    public ResponseEntity<ResData<Void>> destroy(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        log.info("[DELETE] /api/v1/roles/{}", id);
        roleService.delete(id, currentUser);
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
            @RequestBody AssignPermissionsRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("[PUT] /api/v1/roles/{}/permissions count={} by user={}", id,
                request.getPermissionIds() != null ? request.getPermissionIds().size() : 0, currentUser.getUsername());
        return ResponseBuilder.ok(roleService.assignPermissions(id, request, currentUser));
    }
}