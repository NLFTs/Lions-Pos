package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.WarehousesRequestDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.WarehouseResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.catalog.ProductRepository;

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

@Service
@RequiredArgsConstructor
public class WarehousesService {

    private final WarehousesRepository warehousesRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;

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

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private Warehouses getValidatedWarehouse(Long id, User currentUser) {
        Warehouses w = warehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));

        // 👑 Super Admin global bebas bypass pengecekan tenant ID
        if (currentUser.getPartner() == null) {
            return w;
        }

        if (w.getPartners() == null || !w.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda");
        }

        return w;
    }

    // ─── 🔄 MAPPER SECTION ────────────────────────────────────────────────────

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

    // ─── 🚀 MAIN METHODS (SUDAH DISERAGAMKAN POLANYA) ──────────────────────────

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
        checkPermission(currentUser, "warehouse.index"); // 💡 Cek via permission index

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
        checkPermission(currentUser, "warehouse.store"); // 💡 Siapapun boleh buat asal diberi izin Owner

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

            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                throw new IllegalArgumentException("Wajib pilih minimal satu role untuk user gudang.");
            }

            User warehouseUser = new User();
            warehouseUser.setUsername(username);
            warehouseUser.setFullname("Gudang " + saved.getName());
            warehouseUser.setEmail(username + "@gaptek.com");
            warehouseUser.setPassword(passwordEncoder.encode(request.getPassword()));
            warehouseUser.setPartner(currentUser.getPartner());
            warehouseUser.setWarehouse(saved);
            warehouseUser.setRoles(resolveRoles(request.getRoleIds(), currentUser.getPartner()));

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

            stockBalanceRepository.save(sb);
        }

        return mapToResponse(saved);
    }

    // ─── HELPER: Resolve roles dengan validasi kepemilikan tenant ────────────

    private Set<Role> resolveRoles(List<Long> roleIds, Partners targetPartner) {
        List<Role> roles = roleRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {
            throw new RuntimeException("Satu atau lebih Role yang dipilih tidak ditemukan.");
        }

        for (Role role : roles) {
            if (role.getPartner() != null && !role.getPartner().getId().equals(targetPartner.getId())) {
                throw new RuntimeException("Akses Ditolak: Role '" + role.getName() + "' bukan milik partner Anda.");
            }
        }

        return new HashSet<>(roles);
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
}