package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.partner.BranchRequest;
import com.dak.spravel.dto.response.UserResponse;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.BranchResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dak.spravel.service.auth.PermissionCacheService;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.util.UserRoleUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;
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

    // 🔥 KUNCI DINAMIS: Cek permission tanpa hardcode nama role kaku
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Super Admin murni (partner null) bypass seluruh jenis gate permission
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

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── 🛡️ MULTI-TENANT GUARD ────────────────────────────────────────

    private Branches getValidatedBranch(Long id, User currentUser) {
        Branches branch = branchesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        // 👑 Super Admin bypass checking partner id
        if (currentUser.getPartner() == null) {
            return branch;
        }

        if (branch.getPartners() == null || !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses branch partner lain.");
        }

        return branch;
    }

    // ─── 🚀 MAIN METHODS CORRESPONDENCE ────────────────────────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<BranchResponse> findAllBranches() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return branchesRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<BranchResponse> findPageAdmin(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return branchesRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::mapToResponse);
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)

    public List<BranchResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.index");

        if (currentUser.getPartner() == null) {
            return branchesRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        return branchesRepository.findByPartners(currentUser.getPartner()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<BranchResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.index");

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        if (currentUser.getPartner() == null) {
            return branchesRepository.findAll(pageable).map(this::mapToResponse);
        }

        return branchesRepository.findByPartnersId(currentUser.getPartner().getId(), pageable)
                .map(this::mapToResponse);
    }

    public BranchResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.show");

        Branches branch = getValidatedBranch(id, currentUser);
        return mapToResponse(branch);
    }

    @Transactional
    public BranchResponse create(BranchRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.store");
        
        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan partner manapun.");
        }
    
        Branches branch = new Branches();
        branch.setPartners(partner);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setIsActive(true);
        branch.setCreatedAt(LocalDateTime.now());
        branch.setCreatedBy(currentUser);
    
        Branches savedBranch = branchesRepository.save(branch);
    
        // ── Buat Akun User Kasir/Staff Cabang Otomatis ────────────────────────
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String username = request.getUsername().trim();
            if (userRepository.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username '" + username + "' sudah digunakan.");
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                throw new IllegalArgumentException("Password wajib diisi dan minimal 6 karakter.");
            }
    
            User branchUser = new User();
            branchUser.setUsername(username);
            branchUser.setFullname("Cabang " + savedBranch.getName());
            branchUser.setEmail(username + "@gaptek.com");
            branchUser.setPassword(passwordEncoder.encode(request.getPassword()));
            branchUser.setPartner(partner);
            branchUser.setBranch(savedBranch);
    
            // AUTO-ASSIGN: Role pengelola-cabang diberikan otomatis berdasarkan konteks cabang
            Role pengelolaCabangRole = roleRepository.findBySlug("pengelola-cabang")
                .orElseThrow(() -> new RuntimeException("Role pengelola-cabang tidak ditemukan. Pastikan seeder sudah berjalan."));
            branchUser.setRoles(new HashSet<>(Set.of(pengelolaCabangRole)));
    
            userRepository.save(branchUser);
        }
    
        // ── Inisialisasi Kebutuhan Stock Balance Awal (+0 Qty) ──────────────────
        List<Product> products = productRepository.findAllByPartner(partner);
        for (Product product : products) {
            StockBalance stock = new StockBalance();
            stock.setProduct(product);
            stock.setLocationType("BRANCH");
            stock.setLocationId(savedBranch.getId());
            stock.setQty(0L);
            stock.setCreatedAt(LocalDateTime.now());
            stock.setCreatedBy(currentUser);
            stock.setUpdatedBy(currentUser);
            stock.setUpdatedAt(LocalDateTime.now());
            stockBalanceRepository.save(stock);
        }
    
        return mapToResponse(savedBranch);
    }
    
    // ── 🛡️ HELPER FIX: Resolusi Role Bersifat Global Tanpa Dependensi Partner Scope ──
    // private Set<Role> resolveRoles(List<Long> roleIds, User currentUser) {
    //     List<Role> roles = roleRepository.findAllById(roleIds);
        
    //     if (roles.size() != roleIds.size()) {
    //         throw new RuntimeException("Satu atau lebih Role yang dipilih tidak ditemukan.");
    //     }
    
    //     for (Role role : roles) {
    //         if (currentUser.getPartner() != null) {
    //             String roleSlug = role.getSlug().toLowerCase();
    //             if ("admin".equals(roleSlug) || "super-admin".equals(roleSlug)) {
    //                 throw new RuntimeException("Akses Ditolak: Tindakan ilegal! Anda tidak diperbolehkan memasang role master pusat '" + role.getName() + "' ke staff cabang.");
    //             }
    //         }
    //     }
        
    //     return new HashSet<>(roles);
    // }

    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.update");

        Branches branch = getValidatedBranch(id, currentUser);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setUpdatedBy(currentUser);
        AuditHelper.setUpdated(branch);

        return mapToResponse(branchesRepository.save(branch));
    }

    @Transactional
    public BranchResponse softDelete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.delete");

        Branches branch = getValidatedBranch(id, currentUser);
        branch.setIsActive(false);
        branch.setDeletedBy(currentUser);
        AuditHelper.setUpdated(branch);

        return mapToResponse(branchesRepository.save(branch));
    }

    @Transactional
    public BranchResponse restoreBranch(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.update");

        Branches branch = getValidatedBranch(id, currentUser);
        branch.setIsActive(true);
        branch.setUpdatedBy(currentUser);
        AuditHelper.setUpdated(branch);

        return mapToResponse(branchesRepository.save(branch));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.delete");

        Branches branch = getValidatedBranch(id, currentUser);
        branchesRepository.delete(branch);
    }

    // ─── GET USERS BY BRANCH ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByBranch(Long branchId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.show");
        getValidatedBranch(branchId, currentUser); // validate access
        return userRepository.findByBranchId(branchId)
                .stream().map(this::mapUserToResponse).toList();
    }

    // ─── TRANSFER MANAGER ─────────────────────────────────────────────────────

    @Transactional
    public BranchResponse transferManager(Long branchId, Long newManagerUserId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "branch.update");

        Branches branch = getValidatedBranch(branchId, currentUser);

        User newManager = userRepository.findById(newManagerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", newManagerUserId));

        // Guard: user harus di partner yang sama
        if (currentUser.getPartner() != null) {
            if (newManager.getPartner() == null ||
                    !newManager.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        }

        // Guard: user tidak boleh sudah jadi pengelola cabang/gudang lain
        if (newManager.getBranch() != null && !newManager.getBranch().getId().equals(branchId)) {
            throw new RuntimeException("User ini sudah menjadi pengelola cabang lain: " + newManager.getBranch().getName());
        }
        if (newManager.getWarehouse() != null) {
            throw new RuntimeException("User ini sudah menjadi pengelola gudang: " + newManager.getWarehouse().getName());
        }

        // Guard: hanya karyawan biasa yang boleh ditunjuk (bukan owner/admin/pengelola lain)
        if (UserRoleUtil.isIneligibleManagerCandidate(newManager)) {
            throw new RuntimeException("Akses Ditolak: Hanya karyawan biasa yang bisa ditunjuk sebagai pengelola cabang.");
        }

        Role managerRole = roleRepository.findBySlug("pengelola-cabang")
                .orElseThrow(() -> new RuntimeException("Role pengelola-cabang tidak ditemukan."));

        // Lepas pengelola lama dari cabang ini
        List<User> oldManagers = userRepository.findByBranchId(branchId);
        for (User old : oldManagers) {
            if (!old.getId().equals(newManagerUserId)) {
                old.setBranch(null);
                // Hapus role pengelola-cabang dari pengelola lama (kembalikan ke karyawan biasa)
                Set<Role> stripped = old.getRoles().stream()
                        .filter(r -> !"pengelola-cabang".equalsIgnoreCase(r.getSlug()))
                        .collect(Collectors.toSet());
                old.setRoles(stripped);
                userRepository.save(old);
                permissionCacheService.evict(old.getUsername());
            }
        }

        // Assign pengelola baru + role pengelola-cabang
        Set<Role> newRoles = new HashSet<>(newManager.getRoles());
        newRoles.add(managerRole);
        // Hapus role karyawan-cabang jika ada (pengelola lebih tinggi dari karyawan)
        newRoles.removeIf(r -> "karyawan-cabang".equalsIgnoreCase(r.getSlug())
                            || "staff-branch".equalsIgnoreCase(r.getSlug()));
        newManager.setRoles(newRoles);
        newManager.setBranch(branch);
        newManager.setWarehouse(null);
        userRepository.save(newManager);
        permissionCacheService.evict(newManager.getUsername());

        branch.setUpdatedBy(currentUser);
        branch.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(branchesRepository.save(branch));
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setFullname(user.getFullname());
        res.setEmail(user.getEmail());
        res.setAvatar(user.getAvatar());
        res.setCreatedAt(user.getCreatedAt());
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

    // ─── 🔄 PRIVATE UTILS & MAPPERS ────────────────────────────────────────────

    private BranchResponse mapToResponse(Branches branch) {
        if (branch == null) return null;

        BranchResponse response = new BranchResponse();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setAddress(branch.getAddress());
        response.setIsActive(branch.getIsActive());
        response.setCreatedAt(branch.getCreatedAt());
        response.setUpdatedAt(branch.getUpdatedAt());

        if (branch.getPartners() != null) {
            PartnerSimpleDto partnerDto = new PartnerSimpleDto();
            partnerDto.setId(branch.getPartners().getId());
            partnerDto.setName(branch.getPartners().getName());
            response.setPartner(partnerDto);
        }

        response.setCreatedBy(mapUserToDto(branch.getCreatedBy()));
        response.setUpdatedBy(mapUserToDto(branch.getUpdatedBy()));

        return response;
    }

    private UserSimpleDto mapUserToDto(User user) {
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}