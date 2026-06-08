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

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // 🔥 KUNCI DINAMIS: Cek permission langsung dari database tanpa hardcode nama role kaku
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Raja Super Admin (partner null) bypass seluruh jenis gate permission
        if (user.getPartner() == null) {
            return;
        }

        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    // ─── 🚀 MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // GET ALL ROLES (Semua user ngelihat list role global yang sama dari pusat)
    public List<RoleResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.index"); 

        // 💡 FIX: Tidak perlu lagi membedakan query kaku findAllByPartnerIdOrPartnerIsNull
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    // GET ROLE BY ID
    public RoleResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.show");

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        return toResponse(role);
    }

    public RoleResponse forOwnerOrAdminPartner(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.show");

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        return toResponse(role);
    }

    // CREATE ROLE
    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        User currentUser = getAuthenticatedUser();
        
        // 🚨 BLOCK ACCESS: Owner Pusat / Admin Partner dilarang bikin role baru!
        if (currentUser.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Owner atau Admin Partner tidak berhak membuat role baru. Fitur ini dikunci oleh Super Admin Pusat.");
        }
        
        checkPermission(currentUser, "role.store"); 

        // 💡 FIX: Cek duplikasi slug murni global tanpa melihat scope partner id lagi
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

    // UPDATE ROLE
    @Transactional
    public RoleResponse update(Long id, UpdateRoleRequest request) {
        User currentUser = getAuthenticatedUser();
        
        // 🚨 BLOCK ACCESS: Owner Pusat / Admin Partner dilarang edit konfigurasi role!
        if (currentUser.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Owner atau Admin Partner tidak berhak mengubah konfigurasi role.");
        }
        
        checkPermission(currentUser, "role.update");

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        if (request.getName() != null) role.setName(request.getName());
        if (request.getSlug() != null && !request.getSlug().equals(role.getSlug())) {
            // 💡 FIX: Pengecekan slug bersifat unik secara global di database
            if (roleRepository.existsBySlug(request.getSlug())) {
                throw new IllegalArgumentException("Role slug '" + request.getSlug() + "' already exists");
            }
            role.setSlug(request.getSlug());
        }
        
        role.setUpdatedBy(currentUser);
        role.setUpdatedAt(LocalDateTime.now());
        
        return toResponse(roleRepository.save(role));
    }

    // DELETE ROLE
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        
        // 🚨 BLOCK ACCESS: Owner Pusat / Admin Partner dilarang hapus role!
        if (currentUser.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Owner atau Admin Partner tidak berhak menghapus role sistem.");
        }
        
        checkPermission(currentUser, "role.delete");

        roleRepository.deleteById(id);
        permissionCacheService.evictAll();
    }

    // ASSIGN MATRIX PERMISSIONS TO ROLE
    @Transactional
    public RoleResponse assignPermissions(Long id, AssignPermissionsRequest request) {
        User currentUser = getAuthenticatedUser();
        
        // 🚨 BLOCK ACCESS: Owner Pusat / Admin Partner dilarang otak-atik matriks checklist permission!
        if (currentUser.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Owner atau Admin Partner tidak diizinkan mengubah hak akses (Matrix Permission) dari role ini.");
        }
        
        checkPermission(currentUser, "role.update"); 

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

    // FETCH ALL SYSTEM PERMISSIONS (GROUPED BY MODULE)
    public Map<String, List<PermissionResponse>> getAllPermissionsGrouped() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "role.show"); 
        
        // Modul-modul rahasia yang tidak boleh disentuh/dilihat partner luar pusat
        Set<String> blacklistedModules = Set.of("partner", "permission", "module", "log");

        return permissionRepository.findAll().stream()
                .filter(p -> {
                    if (currentUser.getPartner() != null) {
                        String moduleSlug = p.getModule().getSlug().toLowerCase();
                        return !blacklistedModules.contains(moduleSlug);
                    }
                    return true; 
                })
                .map(this::toPermissionResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModuleSlug));
    }

    // ─── 🔄 PRIVATE MAPPERS SECTION ───────────────────────────────────────────

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