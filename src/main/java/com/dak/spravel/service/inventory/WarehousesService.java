package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.WarehousesRequestDTO;
import com.dak.spravel.dto.response.UserResponse;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.WarehouseResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import com.dak.spravel.util.UserRoleUtil;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.service.auth.PermissionCacheService;

import lombok.RequiredArgsConstructor;

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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehousesService {

    private final WarehousesRepository warehousesRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;
    private final PermissionCacheService permissionCacheService;

    // ─── PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // KUNCI DINAMIS: Cek permission langsung dari database tanpa hardcode nama role kaku
    private void checkPermission(User user, String permissionSlug) {
        // Raja Super Admin (partner null) bypass seluruh jenis gate permission
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
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private Warehouses getValidatedWarehouse(Long id, User currentUser) {
        Warehouses w = warehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));

        // Super Admin global bebas bypass pengecekan tenant ID
        if (currentUser.getPartner() == null) {
            return w;
        }

        if (w.getPartners() == null || !w.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda");
        }

        return w;
    }

    // ─── MAPPER SECTION ────────────────────────────────────────────────────

    private WarehouseResponse mapToResponse(Warehouses w) {
        if (w == null) return null;

        WarehouseResponse res = new WarehouseResponse();
        res.setId(w.getId());
        res.setName(w.getName());
        res.setAddress(w.getAddress());
        res.setIsActive(w.getIsActive());
        res.setCreatedAt(w.getCreatedAt());
        res.setUpdatedAt(w.getUpdatedAt());

        if (w.getPartners() != null) {
            PartnerSimpleDto p = new PartnerSimpleDto();
            p.setId(w.getPartners().getId());
            p.setName(w.getPartners().getName());
            res.setPartner(p);
        }

        if (w.getCreatedBy() != null) {
            UserSimpleDto u = new UserSimpleDto();
            u.setId(w.getCreatedBy().getId());
            u.setUsername(w.getCreatedBy().getUsername());
            res.setCreatedBy(u);
        }

        if (w.getUpdatedBy() != null) {
            UserSimpleDto u = new UserSimpleDto();
            u.setId(w.getUpdatedBy().getId());
            u.setUsername(w.getUpdatedBy().getUsername());
            res.setUpdatedBy(u);
        }

        return res;
    }

    // ─── MAIN METHODS (SUDAH DISERAGAMKAN POLANYA) ──────────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<WarehouseResponse> findAllAdmin() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return warehousesRepository.findAll(Sort.by("id").descending())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<WarehouseResponse> findPageAdmin(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return warehousesRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        ).map(this::mapToResponse);
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)

    public List<WarehouseResponse> findAllByPartner() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "warehouse.index"); 

        if (currentUser.getPartner() == null) {
            return warehousesRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        return warehousesRepository
                .findByPartnersIdAndDeletedAtIsNull(currentUser.getPartner().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<WarehouseResponse> findPageByPartner(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "warehouse.index");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (currentUser.getPartner() == null) {
            return warehousesRepository.findAll(pageable).map(this::mapToResponse);
        }

        return warehousesRepository
                .findByPartnersIdAndDeletedAtIsNull(currentUser.getPartner().getId(), pageable)
                .map(this::mapToResponse);
    }

    // ─── CREATE WAREHOUSE ─────────────────────────────────────────────────────

    @Transactional
    public WarehouseResponse create(WarehousesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "warehouse.store"); 

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin tidak diperbolehkan membuat gudang tanpa scope partner.");
        }

        if (warehousesRepository.existsByNameAndPartnersIdAndDeletedAtIsNull(
                request.getName(),
                currentUser.getPartner().getId()
        )) {
            throw new RuntimeException("Warehouse dengan nama '" + request.getName() + "' sudah terdaftar.");
        }

        Warehouses w = new Warehouses();
        w.setName(request.getName());
        w.setAddress(request.getAddress());
        w.setPartners(currentUser.getPartner());
        w.setIsActive(true);
        w.setCreatedAt(LocalDateTime.now());
        w.setCreatedBy(currentUser);

        Warehouses saved = warehousesRepository.save(w);

        // ── Buat Akun User Pengelola Gudang Otomatis ──────────────────────────
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String username = request.getUsername().trim();
            if (userRepository.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username '" + username + "' sudah digunakan.");
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                throw new IllegalArgumentException("Password wajib diisi dan minimal 6 karakter.");
            }

            User warehouseUser = new User();
            warehouseUser.setUsername(username);
            warehouseUser.setFullname("Gudang " + saved.getName());
            warehouseUser.setEmail(username + "@gaptek.com");
            warehouseUser.setPassword(passwordEncoder.encode(request.getPassword()));
            warehouseUser.setPartner(currentUser.getPartner());
            warehouseUser.setWarehouse(saved);

            // AUTO-ASSIGN: Role gudang diberikan otomatis berdasarkan konteks gudang
            Role pengelolaGudangRole = roleRepository.findBySlug("pengelola-gudang")
                .orElseThrow(() -> new RuntimeException("Role gudang tidak ditemukan. Pastikan seeder sudah berjalan."));
            warehouseUser.setRoles(new HashSet<>(Set.of(pengelolaGudangRole)));

            userRepository.save(warehouseUser);
        }

        // ⚙️ INITIALIZE STOCK BALANCE AVAL (+0 Qty untuk setiap produk tenant)
        List<Product> products = productRepository.findAllByPartner(currentUser.getPartner());

        for (Product p : products) {
            StockBalance sb = new StockBalance();
            sb.setProduct(p);
            sb.setLocationType("WAREHOUSE");
            sb.setLocationId(saved.getId());
            sb.setQty(0L);
            sb.setCreatedAt(LocalDateTime.now());
            sb.setCreatedBy(currentUser);
            sb.setUpdatedBy(currentUser);
            sb.setUpdatedAt(LocalDateTime.now()); // Menambahkan updatedAt yang ketinggalan

            stockBalanceRepository.save(sb);
        }

        return mapToResponse(saved);
    }

    // ─── UPDATE WAREHOUSE ─────────────────────────────────────────────────────

    @Transactional
    public WarehouseResponse update(Long id, WarehousesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "warehouse.update");

        Warehouses w = getValidatedWarehouse(id, currentUser);

        if (request.getName() != null && !request.getName().isBlank()) {
            w.setName(request.getName());
        }

        if (request.getAddress() != null) {
            w.setAddress(request.getAddress());
        }

        w.setUpdatedAt(LocalDateTime.now());
        w.setUpdatedBy(currentUser);

        return mapToResponse(warehousesRepository.save(w));
    }

    // ─── DELETE WAREHOUSE ─────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "warehouse.delete");

        Warehouses w = getValidatedWarehouse(id, currentUser);

        w.setDeletedAt(LocalDateTime.now());
        w.setDeletedBy(currentUser);

        warehousesRepository.save(w);
    }

    // ─── GET USERS BY WAREHOUSE ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByWarehouse(Long warehouseId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "warehouse.index");
        getValidatedWarehouse(warehouseId, currentUser); // validate access
        return userRepository.findByWarehouseId(warehouseId)
                .stream().map(this::mapUserToResponse).toList();
    }

    // ─── TRANSFER MANAGER ─────────────────────────────────────────────────────

    @Transactional
    public WarehouseResponse transferManager(Long warehouseId, Long newManagerUserId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "warehouse.update");

        Warehouses warehouse = getValidatedWarehouse(warehouseId, currentUser);

        User newManager = userRepository.findById(newManagerUserId)
                .orElseThrow(() -> new com.dak.spravel.handler.ResourceNotFoundException("User", newManagerUserId));

        // Guard: user harus di partner yang sama
        if (currentUser.getPartner() != null) {
            if (newManager.getPartner() == null ||
                    !newManager.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        }

        // Guard: user tidak boleh sudah jadi pengelola gudang/cabang lain
        if (newManager.getWarehouse() != null && !newManager.getWarehouse().getId().equals(warehouseId)) {
            throw new RuntimeException("User ini sudah menjadi pengelola gudang lain: " + newManager.getWarehouse().getName());
        }
        if (newManager.getBranch() != null) {
            throw new RuntimeException("User ini sudah menjadi pengelola cabang: " + newManager.getBranch().getName());
        }

        // Guard: hanya karyawan biasa yang boleh ditunjuk (bukan owner/admin/pengelola lain)
        if (UserRoleUtil.isIneligibleManagerCandidate(newManager)) {
            throw new RuntimeException("Akses Ditolak: Hanya karyawan biasa yang bisa ditunjuk sebagai pengelola gudang.");
        }

        Role managerRole = roleRepository.findBySlug("pengelola-gudang")
                .orElseThrow(() -> new RuntimeException("Role pengelola-gudang tidak ditemukan."));

        // Lepas pengelola lama dari gudang ini
        List<User> oldManagers = userRepository.findByWarehouseId(warehouseId);
        for (User old : oldManagers) {
            if (!old.getId().equals(newManagerUserId)) {
                old.setWarehouse(null);
                Set<Role> stripped = old.getRoles().stream()
                        .filter(r -> !"pengelola-gudang".equalsIgnoreCase(r.getSlug()))
                        .collect(Collectors.toSet());
                old.setRoles(stripped);
                userRepository.save(old);
                permissionCacheService.evict(old.getUsername());
            }
        }

        // Assign pengelola baru + role pengelola-gudang
        Set<Role> newRoles = new HashSet<>(newManager.getRoles());
        newRoles.add(managerRole);
        // Hapus role karyawan-gudang jika ada (pengelola lebih tinggi dari karyawan)
        newRoles.removeIf(r -> "karyawan-gudang".equalsIgnoreCase(r.getSlug())
                            || "staff-warehouse".equalsIgnoreCase(r.getSlug()));
        newManager.setRoles(newRoles);
        newManager.setWarehouse(warehouse);
        newManager.setBranch(null);
        userRepository.save(newManager);
        
        // Evict cache agar permission baru langsung aktif
        permissionCacheService.evict(newManager.getUsername());

        warehouse.setUpdatedAt(LocalDateTime.now());
        warehouse.setUpdatedBy(currentUser);
        return mapToResponse(warehousesRepository.save(warehouse));
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
}