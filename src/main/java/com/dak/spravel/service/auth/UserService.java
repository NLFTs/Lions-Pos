package com.dak.spravel.service.auth;

import com.dak.spravel.dto.request.user.ChangePasswordRequest;
import com.dak.spravel.dto.request.user.CreateUserRequest;
import com.dak.spravel.dto.request.user.UpdateUserRequest;
import com.dak.spravel.dto.response.UserResponse;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@lombok.extern.slf4j.Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService permissionCacheService;
    private final BranchesRepository branchesRepository;
    private final PartnerRepository partnerRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ─── 💾 FILE SYSTEM UTILS ──────────────────────────────────────────────────

    private void deleteFileDisk(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String cleanPath = fileUrl;
            if (cleanPath.startsWith("/uploads/")) {
                cleanPath = cleanPath.substring("/uploads/".length());
            }
            java.nio.file.Path path = java.nio.file.Paths.get(uploadDir, cleanPath);
            java.nio.file.Files.deleteIfExists(path);
            log.info("[DELETE FILE] Berhasil menghapus file lama: {}", path);
        } catch (Exception e) {
            log.error("[DELETE FILE] Gagal menghapus file lama {}: {}", fileUrl, e.getMessage());
        }
    }

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // 🔥 KUNCI UTAMA: Cek permission slug tanpa intervensi teks nama role kaku
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Raja Super Admin (partner null) bypass seluruh jenis gate permission sistem
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

    // private void checkSuperAdminOnly(User user) {
    //     if (user.getPartner() != null) {
    //         throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
    //     }
    // }

    // 🛡️ AMAN MULTI-TENANCY: Resolusi role dan validasi kepemilikan tenant
    private Set<Role> resolveRoles(List<Long> roleIds, Partners partner) {
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
            
            // Proteksi cross-tenant role injection
            if (partner != null && role.getPartner() != null) {
                if (!role.getPartner().getId().equals(partner.getId())) {
                    throw new RuntimeException("Akses Ditolak: Role '" + role.getName() + "' bukan milik partner Anda.");
                }
            }
            roles.add(role);
        }
        return roles;
    }

    // ─── 🚀 CORE METHODS MANAGEMENT USER ────────────────────────────────────────

    // GET ALL USER
    public List<UserResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "user.index");

        // Super Admin narik semua user global
        if (currentUser.getPartner() == null) {
            return userRepository.findAll(Sort.by("username").ascending())
                    .stream().map(this::toResponse).toList();
        }

        // Tenant hanya narik bawahan di perusahaannya sendiri
        return userRepository.findByPartnerId(currentUser.getPartner().getId())
                .stream().map(this::toResponse).toList();
    }

    // GET ALL PAGINATED & SEARCH
    public Page<UserResponse> findAll(int page, int size, String search) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "user.index");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        if (currentUser.getPartner() == null) {
            if (search != null && !search.isBlank()) {
                return userRepository.search(search, pageable).map(this::toResponse);
            }
            return userRepository.findAll(pageable).map(this::toResponse);
        }

        return userRepository.findByPartnerId(currentUser.getPartner().getId(), pageable)
                .map(this::toResponse);
    }

    // GET BY ID
    public UserResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        
        // 👤 SELF PROFILE BYPASS: Setiap user selalu berhak melihat profilnya sendiri
        if (currentUser.getId().equals(id)) {
            return toResponse(currentUser);
        }

        checkPermission(currentUser, "user.show");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Multi-Tenant Isolation
        if (currentUser.getPartner() != null) {
            if (user.getPartner() == null || !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        }

        return toResponse(user);
    }

    // CREATE USER BARU
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "user.store");

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' sudah terdaftar.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        Partners targetPartner;

        // ==========================================
        // SKENARIO 1: OPERASIONAL TENANT / PARTNER
        // ==========================================
        if (currentUser.getPartner() != null) {
            targetPartner = currentUser.getPartner();
            user.setPartner(targetPartner);

            // Pasang role dinamis pilihan Owner, jika kosong auto-fallback ke role global template 'employee'
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                user.setRoles(resolveRoles(request.getRoleIds(), targetPartner));
            } else {
                throw new IllegalArgumentException("Pilih satu role untuk user ini.");
            }

        // ==========================================
        // SKENARIO 2: SUPER ADMIN GLOBAL SCOPE
        // ==========================================
        } else {
            if (request.getPartnerId() == null) {
                throw new IllegalArgumentException("Super Admin wajib melampirkan target partnerId.");
            }
            targetPartner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));
            user.setPartner(targetPartner);

            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                throw new IllegalArgumentException("Super Admin wajib melampirkan minimal satu target roleId.");
            }
            user.setRoles(resolveRoles(request.getRoleIds(), targetPartner));
        }

        // Validasi Alokasi Penempatan Cabang (Branch Guard)
        if (request.getBranchId() != null) {
            Branches branch = branchesRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            
            if (branch.getPartners() == null || !branch.getPartners().getId().equals(targetPartner.getId())) {
                throw new RuntimeException("Akses Ditolak: Cabang yang dipilih tidak sinkron dengan perusahaan target.");
            }
            user.setBranch(branch);
        }

        return toResponse(userRepository.save(user));
    }

    // UPDATE DATA USER
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        boolean isSelfUpdate = currentUser.getId().equals(id);

        // Jika bukan ngedit diri sendiri, wajib diinterogasi permission & tenant-nya
        if (!isSelfUpdate) {
            checkPermission(currentUser, "user.update");
            if (currentUser.getPartner() != null) {
                if (user.getPartner() == null || !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                    throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
                }
            }
        }

        // Update core info
        if (request.getUsername() != null) {
            if (!request.getUsername().equals(user.getUsername()) &&
                    userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username '" + request.getUsername() + "' sudah digunakan.");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getFullname() != null) user.setFullname(request.getFullname());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        
        if (request.getAvatar() != null) {
            if (user.getAvatar() != null && !user.getAvatar().equals(request.getAvatar())) {
                deleteFileDisk(user.getAvatar());
            }
            if (request.getAvatar().trim().isEmpty()) {
                user.setAvatar(null);
            } else {
                user.setAvatar(request.getAvatar());
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Update Hak Akses / Matriks Role (Dilarang keras memodifikasi role diri sendiri!)
        if (request.getRoleIds() != null) {
            if (isSelfUpdate) {
                throw new RuntimeException("Akses Ditolak: Demi keamanan, Anda tidak bisa memanipulasi Role Anda sendiri.");
            }
            Partners partnerContext = currentUser.getPartner() != null ? currentUser.getPartner() : user.getPartner();
            user.setRoles(resolveRoles(request.getRoleIds(), partnerContext));
            permissionCacheService.evict(user.getUsername());
        }

        return toResponse(userRepository.save(user));
    }

    // DELETE ACCOUNT USER
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "user.delete");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (currentUser.getId().equals(id)) {
            throw new RuntimeException("Akses Ditolak: Tindakan bunuh diri ilegal! Anda dilarang menghapus akun sendiri wkwk.");
        }

        if (currentUser.getPartner() != null) {
            if (user.getPartner() == null || !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        }

        permissionCacheService.evict(user.getUsername());
        if (user.getAvatar() != null) {
            deleteFileDisk(user.getAvatar());
        }
        userRepository.deleteById(id);
    }

    // MANDIRI CHANGE PASSWORD
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User currentUser = getAuthenticatedUser();
        
        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException("Akses Ditolak: Anda hanya diperbolehkan mengubah password milik Anda sendiri.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Password saat ini salah. Periksa kembali.");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    // ─── 🔄 PRIVATE MAPPERS & UTILS ───────────────────────────────────────────

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setFullname(user.getFullname());
        res.setEmail(user.getEmail());
        res.setAvatar(user.getAvatar());
        res.setCreatedAt(user.getCreatedAt());

        if (user.getPartner() != null) {
            PartnerSimpleDto partnerDto = new PartnerSimpleDto();
            partnerDto.setId(user.getPartner().getId());
            partnerDto.setName(user.getPartner().getName());
            res.setPartner(partnerDto);
        }

        if (user.getBranch() != null) {
            res.setBranchId(user.getBranch().getId());
            res.setBranchName(user.getBranch().getName());
        }

        List<UserResponse.RoleData> roleDataList = user.getRoles().stream().map(role -> {
            UserResponse.RoleData rd = new UserResponse.RoleData();
            rd.setId(role.getId());
            rd.setSlug(role.getSlug());
            rd.setName(role.getName());
            return rd;
        }).toList();
        res.setRoles(roleDataList);

        return res;
    }
}