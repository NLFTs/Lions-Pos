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
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.TokenRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
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
    private final WarehousesRepository warehousesRepository;
    private final PartnerRepository partnerRepository;
    private final TokenRepository tokenRepository;

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

    private void checkPermission(User user, String permissionSlug) {
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

    // 🚨 KUNCI UTAMA: Validasi Role Sekarang Bersifat Global Khusus Buat Master Pusat
    private Set<Role> resolveRoles(List<Long> roleIds, User currentUser) {
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
            
            // 🔥 PROTEKSI 1: Partner dilarang keras memakai atau menembak role master 'admin' atau 'super-admin'
            if (currentUser.getPartner() != null) {
                String roleSlug = role.getSlug().toLowerCase();
                if ("admin".equals(roleSlug) || "super-admin".equals(roleSlug)) {
                    throw new RuntimeException("Akses Ditolak: Hak istimewa ilegal! Anda tidak diizinkan menggunakan role pusat '" + role.getName() + "'.");
                }
            }
            roles.add(role);
        }
        return roles;
    }

    // ─── 🚀 CORE METHODS MANAGEMENT USER ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "user.index");

        if (currentUser.getPartner() == null) {
            return userRepository.findAll(Sort.by("username").ascending())
                    .stream().map(this::toResponse).toList();
        }

        return userRepository.findByPartnerId(currentUser.getPartner().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        
        if (currentUser.getId().equals(id)) {
            return toResponse(currentUser);
        }

        checkPermission(currentUser, "user.show");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (currentUser.getPartner() != null) {
            if (user.getPartner() == null || !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        }

        return toResponse(user);
    }

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
        user.setIsActive(true);

        Partners targetPartner;

        // Validasi penentuan partner & penyematan role global
        if (currentUser.getPartner() != null) {
            targetPartner = currentUser.getPartner();
            user.setPartner(targetPartner);

            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                user.setRoles(resolveRoles(request.getRoleIds(), currentUser));
            }
            // Role boleh kosong — user bisa dibuat tanpa role dulu
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
            user.setRoles(resolveRoles(request.getRoleIds(), currentUser));
        }

        // ─── 🛡️ BRANCH GUARD ───
        if (request.getBranchId() != null) {
            Branches branch = branchesRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            if (branch.getPartners() == null || !branch.getPartners().getId().equals(targetPartner.getId())) {
                throw new RuntimeException("Akses Ditolak: Cabang tidak sinkron dengan perusahaan target.");
            }
            user.setBranch(branch);
            user.setWarehouse(null);
        }

        // ─── 🛡️ WAREHOUSE GUARD ───
        if (request.getWarehouseId() != null) {
            Warehouses warehouse = warehousesRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));
            if (warehouse.getPartners() == null || !warehouse.getPartners().getId().equals(targetPartner.getId())) {
                throw new RuntimeException("Akses Ditolak: Gudang tidak sinkron dengan perusahaan target.");
            }
            user.setWarehouse(warehouse);
            user.setBranch(null);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        boolean isSelfUpdate = currentUser.getId().equals(id);

        if (!isSelfUpdate) {
            checkPermission(currentUser, "user.update");
            if (currentUser.getPartner() != null) {
                if (user.getPartner() == null || !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                    throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
                }
            }
        }

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

        Partners partnerContext = currentUser.getPartner() != null ? currentUser.getPartner() : user.getPartner();

        if (Boolean.TRUE.equals(request.getClearBranch())) {
            user.setBranch(null);
        }
        if (Boolean.TRUE.equals(request.getClearWarehouse())) {
            user.setWarehouse(null);
        }
        
        // Update Cabang Kerja
        if (request.getBranchId() != null) {
            Branches branch = branchesRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            if (branch.getPartners() == null || !branch.getPartners().getId().equals(partnerContext.getId())) {
                throw new RuntimeException("Cabang tidak cocok dengan tenant.");
            }
            user.setBranch(branch);
            user.setWarehouse(null);
        }

        // Update Gudang Kerja
        if (request.getWarehouseId() != null) {
            Warehouses warehouse = warehousesRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));
            if (warehouse.getPartners() == null || !warehouse.getPartners().getId().equals(partnerContext.getId())) {
                throw new RuntimeException("Gudang tidak cocok dengan tenant.");
            }
            user.setWarehouse(warehouse);
            user.setBranch(null);
        }

        if (request.getRoleIds() != null) {
            if (isSelfUpdate) {
                throw new RuntimeException("Akses Ditolak: Demi keamanan, Anda tidak bisa memanipulasi Role Anda sendiri.");
            }
            // 🔥 Diubah: Kirim objek `currentUser` untuk mencegah suntikan role pusat
            user.setRoles(resolveRoles(request.getRoleIds(), currentUser));
            permissionCacheService.evict(user.getUsername());
        }

        if (request.getIsActive() != null) {
            if (isSelfUpdate) {
                throw new RuntimeException("Akses Ditolak: Anda tidak bisa menonaktifkan akun sendiri.");
            }
            user.setIsActive(request.getIsActive());
            if (!request.getIsActive()) {
                permissionCacheService.evict(user.getUsername());
                tokenRepository.deleteAllByUsername(user.getUsername());
            }
        }

        return toResponse(userRepository.save(user));
    }

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
        res.setIsActive(user.getIsActive() == null || user.getIsActive());

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

        if (user.getWarehouse() != null) {
            res.setWarehouseId(user.getWarehouse().getId());
            res.setWarehouseName(user.getWarehouse().getName());
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