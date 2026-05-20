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

    // --- HELPER ---

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin"));
    }

    private boolean isAdminPartner(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin-partners"));
    }

    // --- LOGIC UTAMA ---

    // GET ALL
    public List<UserResponse> findAll() {
        User currentUser = getAuthenticatedUser();

        // Super admin bisa lihat semua user
        if (isAdmin(currentUser)) {
            return userRepository.findAll(Sort.by("username").ascending())
                    .stream().map(this::toResponse).toList();
        }

        // Admin partner hanya lihat user partnernya sendiri
        if (isAdminPartner(currentUser) && currentUser.getPartner() != null) {
            return userRepository.findByPartnerId(currentUser.getPartner().getId())
                    .stream().map(this::toResponse).toList();
        }

        throw new RuntimeException("Akses Ditolak: Anda tidak punya akses ke data user.");
    }

    // GET ALL PAGINATED
    public Page<UserResponse> findAll(int page, int size, String search) {
        User currentUser = getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        if (isAdmin(currentUser)) {
            if (search != null && !search.isBlank()) {
                return userRepository.search(search, pageable).map(this::toResponse);
            }
            return userRepository.findAll(pageable).map(this::toResponse);
        }

        if (isAdminPartner(currentUser) && currentUser.getPartner() != null) {
            return userRepository.findByPartnerId(currentUser.getPartner().getId(), pageable)
                    .map(this::toResponse);
        }

        throw new RuntimeException("Akses Ditolak: Anda tidak punya akses ke data user.");
    }

    // GET BY ID
    public UserResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Super admin bisa lihat semua
        if (isAdmin(currentUser)) {
            return toResponse(user);
        }

        // Admin partner hanya bisa lihat user partnernya sendiri
        if (isAdminPartner(currentUser) && currentUser.getPartner() != null) {
            if (user.getPartner() == null ||
                    !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
            return toResponse(user);
        }

        throw new RuntimeException("Akses Ditolak: Anda tidak punya akses ke data user.");
    }

    // CREATE
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User currentUser = getAuthenticatedUser();

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ==========================================
        // SKENARIO 1: JIKA YANG LOGIN ADMIN PARTNER
        // ==========================================
        if (isAdminPartner(currentUser)) {
            if (currentUser.getPartner() == null) {
                throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
            }
            // 🔒 Otomatis kunci ke partner si admin itu sendiri
            Partners currentPartner = currentUser.getPartner();
            user.setPartner(currentPartner);

            // 🔒 Otomatis kunci role ke employee-partners
            Set<Role> roles = new HashSet<>();
            Role employeeRole = roleRepository.findBySlug("employee-partners")
                    .orElseThrow(() -> new RuntimeException("Role employee-partners tidak ditemukan"));
            roles.add(employeeRole);
            user.setRoles(roles);

            // 🔒 Validasi Branch: Jika dia nge-assign branch, branch itu WAJIB milik partnernya sendiri
            if (request.getBranchId() != null) {
                Branches branch = branchesRepository.findById(request.getBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch tidak ditemukan"));
                
                if (branch.getPartners() == null || !branch.getPartners().getId().equals(currentPartner.getId())) {
                    throw new RuntimeException("Akses Ditolak: Anda tidak bisa memasukkan user ke Cabang milik Partner lain!");
                }
                user.setBranch(branch);
            }

        // ==========================================
        // SKENARIO 2: JIKA YANG LOGIN SUPER ADMIN
        // ==========================================
        } else if (isAdmin(currentUser)) {
            // 🛠️ Super Admin WAJIB menentukan target Partnernya
            if (request.getPartnerId() == null) {
                throw new IllegalArgumentException("Super Admin wajib mengisi partnerId untuk user baru.");
            }
            Partners targetPartner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partner tidak ditemukan"));
            user.setPartner(targetPartner);

            // 🛠️ Super Admin WAJIB menentukan Role-nya apa
            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                throw new IllegalArgumentException("Super Admin wajib mengisi minimal satu roleId.");
            }
            user.setRoles(resolveRoles(request.getRoleIds()));

            // 🛠️ Super Admin set branch (opsional, tapi divalidasi harus sinkron dengan partner yang dipilih)
            if (request.getBranchId() != null) {
                Branches branch = branchesRepository.findById(request.getBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch tidak ditemukan"));
                
                if (branch.getPartners() == null || !branch.getPartners().getId().equals(targetPartner.getId())) {
                    throw new RuntimeException("Error: Cabang yang dipilih tidak sinkron dengan Partner yang dituju.");
                }
                user.setBranch(branch);
            }

        } else {
            throw new RuntimeException("Akses Ditolak: Anda tidak punya akses untuk membuat user.");
        }

        return toResponse(userRepository.save(user));
    }

    // UPDATE
    public UserResponse update(Long id, UpdateUserRequest request) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Admin partner hanya bisa update user partnernya sendiri
        if (isAdminPartner(currentUser)) {
            if (user.getPartner() == null ||
                    !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        } else if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak punya akses untuk mengubah user.");
        }

        if (request.getUsername() != null) {
            if (!request.getUsername().equals(user.getUsername()) &&
                    userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
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

        // Hanya super admin yang bisa ganti role
        if (isAdmin(currentUser) && request.getRoleIds() != null) {
            Set<Role> roles = resolveRoles(request.getRoleIds());
            user.setRoles(roles);
            permissionCacheService.evict(user.getUsername());
        }

        return toResponse(userRepository.save(user));
    }

    // DELETE
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Admin partner hanya bisa delete user partnernya sendiri
        if (isAdminPartner(currentUser)) {
            if (user.getPartner() == null ||
                    !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        } else if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak punya akses untuk menghapus user.");
        }

        permissionCacheService.evict(user.getUsername());
        if (user.getAvatar() != null) {
            deleteFileDisk(user.getAvatar());
        }
        userRepository.deleteById(id);
    }

    // CHANGE PASSWORD
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // MAPPING
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

    private Set<Role> resolveRoles(List<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
            roles.add(role);
        }
        return roles;
    }
}