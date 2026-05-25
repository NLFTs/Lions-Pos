package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.WarehousesRequestDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.WarehouseResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.catalog.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehousesService {

    private final WarehousesRepository warehousesRepository;
    private final UserRepository userRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;

    // =========================
    // AUTH HELPERS
    // =========================

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedOwner() {
        User user = getAuthenticatedUser();

        boolean isOwner = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("owner"));

        if (!isOwner) {
            throw new RuntimeException("Akses Ditolak: hanya Owner yang diizinkan");
        }

        if (user.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner");
        }

        return user;
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("admin"));

        if (!isAdmin) {
            throw new RuntimeException("Akses Ditolak: Super Admin saja");
        }

        return user;
    }

    // =========================
    // VALIDATION HELPERS
    // =========================

    private Warehouses getValidatedWarehouse(Long id, User user) {
        Warehouses w = warehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));

        if (w.getPartners() == null ||
                !w.getPartners().getId().equals(user.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Warehouse bukan milik partner Anda");
        }

        return w;
    }

    private boolean isOwner(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("owner"));
    }

    // =========================
    // MAPPER
    // =========================

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

    // =========================
    // SUPER ADMIN
    // =========================

    public List<WarehouseResponse> findAllAdmin() {
        getAuthenticatedSuperAdmin();

        return warehousesRepository.findAll(Sort.by("id").descending())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<WarehouseResponse> findPageAdmin(int page, int size) {
        getAuthenticatedSuperAdmin();

        return warehousesRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        ).map(this::mapToResponse);
    }

    // =========================
    // OWNER / PARTNER
    // =========================

    public List<WarehouseResponse> findAllByPartner() {
        User user = getAuthenticatedOwner();

        return warehousesRepository
                .findByPartnersIdAndDeletedAtIsNull(user.getPartner().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<WarehouseResponse> findPageByPartner(int page, int size) {
        User user = getAuthenticatedOwner();

        return warehousesRepository
                .findByPartnersIdAndDeletedAtIsNull(
                        user.getPartner().getId(),
                        PageRequest.of(page, size, Sort.by("id").descending())
                )
                .map(this::mapToResponse);
    }

    // =========================
    // CREATE
    // =========================

    @Transactional
    public WarehouseResponse create(WarehousesRequestDTO request) {
        User user = getAuthenticatedOwner();

        if (!isOwner(user)) {
            throw new RuntimeException("Hanya Owner yang boleh membuat Warehouse");
        }

        if (warehousesRepository.existsByNameAndPartnersIdAndDeletedAtIsNull(
                request.getName(),
                user.getPartner().getId()
        )) {
            throw new RuntimeException("Warehouse sudah terdaftar");
        }

        Warehouses w = new Warehouses();
        w.setName(request.getName());
        w.setAddress(request.getAddress());
        w.setPartners(user.getPartner());
        w.setIsActive(true);
        w.setCreatedAt(LocalDateTime.now());
        w.setCreatedBy(user);

        Warehouses saved = warehousesRepository.save(w);

        // INIT STOCK BALANCE
        List<Product> products = productRepository.findAllByPartner(user.getPartner());

        for (Product p : products) {
            StockBalance sb = new StockBalance();
            sb.setProduct(p);
            sb.setLocationType("WAREHOUSE");
            sb.setLocationId(saved.getId());
            sb.setQty(0L);
            sb.setCreatedAt(LocalDateTime.now());
            sb.setCreatedBy(user);
            sb.setUpdatedBy(user);

            stockBalanceRepository.save(sb);
        }

        return mapToResponse(saved);
    }



    @Transactional
    public WarehouseResponse update(Long id, WarehousesRequestDTO request) {
        User user = getAuthenticatedOwner();

        if (!isOwner(user)) {
            throw new RuntimeException("Hanya Owner yang boleh update Warehouse");
        }

        Warehouses w = getValidatedWarehouse(id, user);

        if (request.getName() != null && !request.getName().isBlank()) {
            w.setName(request.getName());
        }

        if (request.getAddress() != null) {
            w.setAddress(request.getAddress());
        }

        w.setUpdatedAt(LocalDateTime.now());
        w.setUpdatedBy(user);

        return mapToResponse(warehousesRepository.save(w));
    }


    @Transactional
    public void delete(Long id) {
        User user = getAuthenticatedOwner();

        if (!isOwner(user)) {
            throw new RuntimeException("Hanya Owner yang boleh menghapus Warehouse");
        }

        Warehouses w = getValidatedWarehouse(id, user);

        w.setDeletedAt(LocalDateTime.now());
        w.setDeletedBy(user);

        warehousesRepository.save(w);
    }
}