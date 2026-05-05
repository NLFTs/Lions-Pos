package com.dak.spravel.service.auth;

import com.dak.spravel.dto.request.user.AssignPermissionsRequest;
import com.dak.spravel.dto.request.user.CreateRoleRequest;
import com.dak.spravel.dto.request.user.UpdateRoleRequest;
import com.dak.spravel.dto.response.PermissionResponse;
import com.dak.spravel.dto.response.RoleResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Permission;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.repository.auth.PermissionRepository;
import com.dak.spravel.repository.auth.RoleRepository;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Business logic for roles with admin protection and permission sync.
 */
@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionCacheService permissionCacheService;

    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RoleResponse findById(Long id) {
        return toResponse(roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id)));
    }

    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Role slug '" + request.getSlug() + "' already exists");
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setSlug(request.getSlug());
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(perms);
        }
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse update(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        if (request.getName() != null) role.setName(request.getName());
        if (request.getSlug() != null) {
            if (!request.getSlug().equals(role.getSlug()) && roleRepository.existsBySlug(request.getSlug())) {
                throw new IllegalArgumentException("Role slug '" + request.getSlug() + "' already exists");
            }
            role.setSlug(request.getSlug());
        }
        return toResponse(roleRepository.save(role));
    }

    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        if ("admin".equals(role.getSlug())) {
            throw new IllegalArgumentException("The admin role cannot be deleted");
        }
        roleRepository.deleteById(id);
        // Role deleted — any user who had this role now has different permissions
        permissionCacheService.evictAll();
    }

    public RoleResponse assignPermissions(Long id, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        Set<Permission> perms = request.getPermissionIds() != null
                ? new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()))
                : new HashSet<>();
        role.setPermissions(perms);
        Role savedRole = roleRepository.save(role);
        // Permissions changed — evict all, any cached user with this role is now stale
        permissionCacheService.evictAll();
        return toResponse(savedRole);
    }

    public Map<String, List<PermissionResponse>> getAllPermissionsGrouped() {
        return permissionRepository.findAll().stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModuleSlug));
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private RoleResponse toResponse(Role role) {
        RoleResponse res = new RoleResponse();
        res.setId(role.getId());
        res.setSlug(role.getSlug());
        res.setName(role.getName());
        res.setPermissions(role.getPermissions().stream().map(this::toPermissionResponse).toList());
        return res;
    }

    private PermissionResponse toPermissionResponse(Permission p) {
        PermissionResponse res = new PermissionResponse();
        res.setId(p.getId());
        res.setSlug(p.getSlug());
        res.setName(p.getName());
        res.setModuleSlug(p.getModule().getSlug());
        return res;
    }
}
