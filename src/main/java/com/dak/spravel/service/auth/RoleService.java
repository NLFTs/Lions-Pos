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
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.PermissionRepository;
import com.dak.spravel.repository.auth.RoleRepository;
import org.springframework.stereotype.Service;
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
    private final PermissionRepository permissionRepository;
    private final PermissionCacheService permissionCacheService;

    // 💡 Modifikasi: Cuma ambil role yang berhak dilihat oleh partner bersangkutan + role global
    public List<RoleResponse> findAll(User currentUser) {
        if (currentUser.getPartner() != null) {
            return roleRepository.findAllByPartnerIdOrPartnerIsNull(currentUser.getPartner().getId())
                    .stream().map(this::toResponse).toList();
        }
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RoleResponse findById(Long id, User currentUser) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        // 🔒 Proteksi: Mencegah partner lain mengintip role partner lain
        validatePartnerAccess(role, currentUser);
        
        return toResponse(role);
    }

    public RoleResponse create(CreateRoleRequest request, User currentUser) {
        Partners partner = currentUser.getPartner();
        
        boolean slugExists = (partner != null) 
                ? roleRepository.existsBySlugAndPartnerId(request.getSlug(), partner.getId())
                : roleRepository.existsBySlugAndPartnerIsNull(request.getSlug());

        if (slugExists) {
            throw new IllegalArgumentException("Role slug '" + request.getSlug() + "' already exists for this partner scope");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setSlug(request.getSlug());
        role.setPartner(partner); 
        role.setCreatedBy(currentUser);
        role.setCreatedAt(LocalDateTime.now());

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(perms);
        }
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse update(Long id, UpdateRoleRequest request, User currentUser) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        // 🔒 Proteksi: Cuma pemilik role yang bisa update
        validatePartnerAccess(role, currentUser);

        if (request.getName() != null) role.setName(request.getName());
        if (request.getSlug() != null && !request.getSlug().equals(role.getSlug())) {
            Partners partner = currentUser.getPartner();
            boolean slugExists = (partner != null) 
                    ? roleRepository.existsBySlugAndPartnerId(request.getSlug(), partner.getId())
                    : roleRepository.existsBySlugAndPartnerIsNull(request.getSlug());

            if (slugExists) {
                throw new IllegalArgumentException("Role slug '" + request.getSlug() + "' already exists");
            }
            role.setSlug(request.getSlug());
        }
        
        role.setUpdatedBy(currentUser);
        role.setUpdatedAt(LocalDateTime.now());
        
        return toResponse(roleRepository.save(role));
    }

    public void delete(Long id, User currentUser) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        // 🔒 Proteksi: Cuma pemilik role yang bisa delete
        validatePartnerAccess(role, currentUser);

        if ("admin".equals(role.getSlug()) && role.getPartner() == null) {
            throw new IllegalArgumentException("The global master admin role cannot be deleted");
        }
        
        roleRepository.deleteById(id);
        permissionCacheService.evictAll();
    }

    public RoleResponse assignPermissions(Long id, AssignPermissionsRequest request, User currentUser) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        
        // 🔒 Proteksi
        validatePartnerAccess(role, currentUser);

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
        return permissionRepository.findAll().stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModuleSlug));
    }

    // ─── Helper Security Gate ──────────────────────────────────────────────────

    private void validatePartnerAccess(Role role, User currentUser) {
        // Jika user yang login adalah staff partner, dan role yang diakses milik partner lain
        if (currentUser.getPartner() != null && role.getPartner() != null) {
            if (!role.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new SecurityException("Gak boleh ngutak-ngatik role punya partner lain, Mip!");
            }
        }
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