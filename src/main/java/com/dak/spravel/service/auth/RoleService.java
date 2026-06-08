package com.dak.spravel.service.auth;

import com.dak.spravel.dto.request.user.AssignPermissionsRequest;
import com.dak.spravel.dto.request.user.CreateRoleRequest;
import com.dak.spravel.dto.request.user.UpdateRoleRequest;
import com.dak.spravel.dto.response.PermissionResponse;
import com.dak.spravel.dto.response.RoleResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Permission;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.PermissionRepository;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionCacheService permissionCacheService;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // Validasi Permission Dinamis dari DB
    private void checkPermission(User user, String permissionSlug) {
        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    // Helper untuk cek status Owner
    private boolean isOwner(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("owner"));
    }

    // Helper untuk cek status Superadmin / Admin
    private boolean isAdminOrSuperadmin(User user) {
        return user.getRoles().stream().anyMatch(role -> 
            role.getSlug().equalsIgnoreCase("super-admin") || role.getSlug().equalsIgnoreCase("admin")
        );
    }

    // ─── INDEX ───────────────────────────────────────────────────────────────
    public List<RoleResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.index"); 

        // Jika dia Owner -> hanya melihat list external dan bukan owner
        if (isOwner(currentUser)) {
            return roleRepository.findExternalRolesExceptOwner().stream().map(this::toResponse).toList();
        }
        
        // Jika Superadmin / Admin -> melihat semuanya
        if (isAdminOrSuperadmin(currentUser)) {
            return roleRepository.findAll().stream().map(this::toResponse).toList();
        }

        throw new RuntimeException("Akses Ditolak: Role Anda tidak dikenali untuk memuat data ini.");
    }

    // ─── SHOW ────────────────────────────────────────────────────────────────
    public RoleResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.show");

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        // Proteksi Tambahan: Jika dia Owner, pastikan data yang di-show bukan data 'owner' atau internal
        if (isOwner(currentUser)) {
            if (role.getSlug().equalsIgnoreCase("owner") || role.getType() != Role.Type.EXTERNAL) {
                throw new RuntimeException("Akses Ditolak: Anda tidak berhak melihat detail role ini.");
            }
        }
        
        return toResponse(role);
    }

    // ─── STORE (MUTASI DATA BANNED FOR OWNER) ────────────────────────────────
    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.store"); 

        if (isOwner(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Owner hanya memiliki akses read-only (Index & Show).");
        }

        boolean slugExists = roleRepository.existsBySlug(request.getSlug());
        if (slugExists) {
            throw new IllegalArgumentException("Role slug '" + request.getSlug() + "' already exists global system scope");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setSlug(request.getSlug());
        role.setType(Role.Type.EXTERNAL);        
        role.setCreatedBy(currentUser);
        role.setCreatedAt(LocalDateTime.now());

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(perms);
        }
        return toResponse(roleRepository.save(role));
    }

    // ─── UPDATE (MUTASI DATA BANNED FOR OWNER) ────────────────────────────────
    @Transactional
    public RoleResponse update(Long id, UpdateRoleRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.update");

        if (isOwner(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Owner hanya memiliki akses read-only (Index & Show).");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        if (request.getName() != null) role.setName(request.getName());
        if (request.getSlug() != null && !request.getSlug().equals(role.getSlug())) {
            if (roleRepository.existsBySlug(request.getSlug())) {
                throw new IllegalArgumentException("Role slug '" + request.getSlug() + "' already exists");
            }
            role.setSlug(request.getSlug());
        }
        
        role.setUpdatedBy(currentUser);
        role.setUpdatedAt(LocalDateTime.now());
        
        return toResponse(roleRepository.save(role));
    }

    // ─── DELETE (MUTASI DATA BANNED FOR OWNER) ────────────────────────────────
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.delete");

        if (isOwner(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Owner hanya memiliki akses read-only (Index & Show).");
        }

        roleRepository.deleteById(id);
        permissionCacheService.evictAll();
    }

    // ─── ASSIGN PERMISSIONS (MUTASI DATA BANNED FOR OWNER) ────────────────────
    @Transactional
    public RoleResponse assignPermissions(Long id, AssignPermissionsRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.update"); 

        if (isOwner(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Owner tidak diizinkan mengubah hak akses (Matrix Permission).");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        Set<Permission> perms = request.getPermissionIds() != null
                ? new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()))
                : new HashSet<>();
        role.setPermissions(perms);
        role.setUpdatedBy(currentUser);
        role.setUpdatedAt(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(role);
        permissionCacheService.evictAll();
        return toResponse(savedRole);
    }

    public Map<String, List<PermissionResponse>> getAllPermissionsGrouped() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.show"); 
        
        Set<String> blacklistedModules = Set.of("partner", "permission", "module", "log");

        return permissionRepository.findAll().stream()
                .filter(p -> {
                    if (isOwner(currentUser)) {
                        String moduleSlug = p.getModule().getSlug().toLowerCase();
                        return !blacklistedModules.contains(moduleSlug);
                    }
                    return true; 
                })
                .map(this::toPermissionResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModuleSlug));
    }

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