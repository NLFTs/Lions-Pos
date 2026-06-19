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

    // ─── UTILS AUTENTIKASI ───────────────────────────────────────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private void checkPermission(User user, String permissionSlug) {
        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    private boolean isOwner(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("owner"));
    }

    private boolean isAdminOrSuperadmin(User user) {
        return user.getRoles().stream().anyMatch(role -> 
            role.getSlug().equalsIgnoreCase("super-admin") || role.getSlug().equalsIgnoreCase("admin")
        );
    }

    /**
     * Helper untuk memvalidasi pendelegasian hak akses secara turun-temurun.
     * Mencegah Owner memberikan hak akses kepada perannya yang dia sendiri tidak memilikinya.
     */
    private void validatePermissionDelegation(User currentUser, Set<Permission> requestedPerms) {
        if (isOwner(currentUser)) {
            Set<String> ownerPermSlugs = currentUser.getRoles().stream()
                    .filter(r -> r.getPermissions() != null)
                    .flatMap(r -> r.getPermissions().stream())
                    .map(p -> p.getSlug().toLowerCase())
                    .collect(Collectors.toSet());

            for (Permission p : requestedPerms) {
                if (!ownerPermSlugs.contains(p.getSlug().toLowerCase())) {
                    throw new RuntimeException("Akses Ditolak: Anda tidak berhak mendelegasikan hak akses '" 
                            + p.getName() + "' karena Anda sendiri tidak memilikinya.");
                }
            }
        }
    }

    /**
     * Memformat slug secara aman agar terisolasi per partner mitra.
     * Contoh: "kasir" menjadi "partner-3-kasir".
     */
    private String resolveSafeSlug(User currentUser, String requestedSlug) {
        String cleanSlug = requestedSlug.toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");
        if (currentUser.getPartner() != null) {
            String prefix = "partner-" + currentUser.getPartner().getId() + "-";
            if (!cleanSlug.startsWith(prefix)) {
                return prefix + cleanSlug;
            }
        }
        return cleanSlug;
    }

    // ─── INDEX ROLE ──────────────────────────────────────────────────────────

    public List<RoleResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.index"); 

        // Jika Owner -> Hanya melihat role eksternal bawaan sistem ATAU role kustom buatan mitranya sendiri
        if (isOwner(currentUser)) {
            Long partnerId = currentUser.getPartner() != null ? currentUser.getPartner().getId() : null;
            return roleRepository.findAll().stream()
                    .filter(role -> {
                        // Harus bertipe EXTERNAL
                        if (role.getType() != Role.Type.EXTERNAL) return false;
                        // Tidak boleh melihat/mengedit role 'owner' utama sistem untuk proteksi
                        if (role.getSlug().equalsIgnoreCase("owner")) return false;

                        // Jika kustom buatan partner, pastikan id partner cocok
                        if (role.getPartner() != null) {
                            return partnerId != null && role.getPartner().getId().equals(partnerId);
                        }
                        // Jika partner null, berarti ini role default sistem (bisa dipakai bersama)
                        return true;
                    })
                    .map(this::toResponse)
                    .toList();
        }
        
        // Jika Superadmin / Admin -> Melihat semua role secara global
        if (isAdminOrSuperadmin(currentUser)) {
            return roleRepository.findAll().stream().map(this::toResponse).toList();
        }

        throw new RuntimeException("Akses Ditolak: Peran akun Anda tidak dikenali untuk melihat data ini.");
    }

    // ─── SHOW ROLE ───────────────────────────────────────────────────────────

    public RoleResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.show");

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        // Validasi batasan Owner
        if (isOwner(currentUser)) {
            if (role.getType() != Role.Type.EXTERNAL || role.getSlug().equalsIgnoreCase("owner")) {
                throw new RuntimeException("Akses Ditolak: Anda tidak memiliki wewenang melihat detail role ini.");
            }
            if (role.getPartner() != null) {
                if (currentUser.getPartner() == null || !role.getPartner().getId().equals(currentUser.getPartner().getId())) {
                    throw new RuntimeException("Akses Ditolak: Peran kustom ini milik mitra perusahaan lain.");
                }
            }
        }
        
        return toResponse(role);
    }

    // ─── CREATE ROLE (DINAMIS & AMAN UNTUK OWNER) ────────────────────────────

    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.store"); 

        String safeSlug = resolveSafeSlug(currentUser, request.getSlug());
        
        if (roleRepository.existsBySlug(safeSlug)) {
            throw new IllegalArgumentException("Kode peran '" + request.getSlug() + "' sudah terdaftar di sistem.");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setSlug(safeSlug);
        role.setType(Role.Type.EXTERNAL);        
        role.setCreatedBy(currentUser);
        role.setCreatedAt(LocalDateTime.now());

        // Bind otomatis ke partner Owner
        if (currentUser.getPartner() != null) {
            role.setPartner(currentUser.getPartner());
        }

        // Set hak akses (Turun-temurun)
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            validatePermissionDelegation(currentUser, perms);
            role.setPermissions(perms);
        } else {
            role.setPermissions(new HashSet<>());
        }

        return toResponse(roleRepository.save(role));
    }

    // ─── UPDATE ROLE (DINAMIS & AMAN UNTUK OWNER) ────────────────────────────

    @Transactional
    public RoleResponse update(Long id, UpdateRoleRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.update");

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        // Validasi kepemilikan data bagi Owner
        if (isOwner(currentUser)) {
            if (role.getPartner() == null || !role.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda hanya diperbolehkan mengubah peran kustom perusahaan Anda sendiri.");
            }
        }

        if (request.getName() != null) {
            role.setName(request.getName());
        }
        
        if (request.getSlug() != null && !request.getSlug().trim().isEmpty()) {
            String safeSlug = resolveSafeSlug(currentUser, request.getSlug());
            if (!safeSlug.equals(role.getSlug()) && roleRepository.existsBySlug(safeSlug)) {
                throw new IllegalArgumentException("Kode peran '" + request.getSlug() + "' sudah digunakan.");
            }
            role.setSlug(safeSlug);
        }
        
        role.setUpdatedBy(currentUser);
        role.setUpdatedAt(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(role);
        permissionCacheService.evictAll();
        return toResponse(savedRole);
    }

    // ─── DELETE ROLE ─────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.delete");

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        // Validasi kepemilikan data bagi Owner
        if (isOwner(currentUser)) {
            if (role.getPartner() == null || !role.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda dilarang menghapus peran sistem default atau milik perusahaan lain.");
            }
        }

        roleRepository.deleteById(id);
        permissionCacheService.evictAll();
    }

    // ─── ASSIGN PERMISSIONS (MATRIKS HAK AKSES) ─────────────────────────────

    @Transactional
    public RoleResponse assignPermissions(Long id, AssignPermissionsRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.update"); 

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        // Validasi kepemilikan data bagi Owner
        if (isOwner(currentUser)) {
            if (role.getPartner() == null || !role.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda hanya bisa menyunting hak akses milik perusahaan Anda.");
            }
        }

        Set<Permission> perms = request.getPermissionIds() != null
                ? new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()))
                : new HashSet<>();

        // Validasi hak akses turun-temurun
        validatePermissionDelegation(currentUser, perms);

        role.setPermissions(perms);
        role.setUpdatedBy(currentUser);
        role.setUpdatedAt(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(role);
        permissionCacheService.evictAll();
        return toResponse(savedRole);
    }

    // ─── GROUPED PERMISSIONS LIST (TURUN-TEMURUN GUARDED) ────────────────────

    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getAllPermissionsGrouped() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.show"); 
        
        Set<String> blacklistedModules = Set.of("partner", "permission", "module", "log");

        // Jika Owner, ambil semua ID permission yang dimiliki Owner itu sendiri
        Set<Long> ownerPermissionIds = isOwner(currentUser) 
            ? currentUser.getRoles().stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getId)
                .collect(Collectors.toSet())
            : new HashSet<>();

        return permissionRepository.findAll().stream()
                .filter(p -> {
                    if (isOwner(currentUser)) {
                        // Saringan 1: Owner tidak boleh mendelegasikan permission di luar kepemilikannya sendiri
                        if (!ownerPermissionIds.contains(p.getId())) {
                            return false;
                        }
                        // Saringan 2: Hilangkan modul internal pusat
                        String moduleSlug = p.getModule().getSlug().toLowerCase();
                        return !blacklistedModules.contains(moduleSlug);
                    }
                    return true; 
                })
                .map(this::toPermissionResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModuleSlug));
    }

    // ─── MAPPERS ─────────────────────────────────────────────────────────────

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