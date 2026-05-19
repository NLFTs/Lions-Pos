package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.WarehousesRequestDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.WarehouseResponse;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    // =========================
    // AUTH HELPERS (POLA BARU)
    // =========================
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) 
            throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee-partners"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin"));
    }

    public WarehouseResponse mapToResponse(Warehouses warehouses) {
        if (warehouses == null) return null;

        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouses.getId());
        response.setName(warehouses.getName());
        response.setAddress(warehouses.getAddress());
        response.setIsActive(warehouses.getIsActive());

        if (warehouses.getPartners() != null) {
            PartnerSimpleDto pDto = new PartnerSimpleDto();
            pDto.setId(warehouses.getPartners().getId());
            pDto.setName(warehouses.getPartners().getName());
            response.setPartner(pDto);
        }

        response.setCreatedBy(mapUserToDto(warehouses.getCreatedBy()));
        response.setUpdatedBy(mapUserToDto(warehouses.getUpdatedBy()));
        response.setCreatedAt(warehouses.getCreatedAt());
        response.setUpdatedAt(warehouses.getUpdatedAt());
        return response;
    }

    private UserSimpleDto mapUserToDto(User user) {
        if (user == null)
            return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<WarehouseResponse> findAllAdmin() {
        getAuthenticatedSuperAdmin();
        
        List<Warehouses> warehouses = warehousesRepository.findAll();

        return warehouses.stream().map(this::mapToResponse).toList();
    }

    public Page<WarehouseResponse> findPageAdmin(int page, int size) {
        getAuthenticatedSuperAdmin();

        return warehousesRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<WarehouseResponse> findAllByPartner() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return warehousesRepository.findByPartnersIdAndDeletedAtIsNull(currentUser.getPartner().getId())
                .stream().map(this::mapToResponse).toList();
    }

    public Page<WarehouseResponse> findPageByPartner(int page, int size) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return warehousesRepository.findByPartnersIdAndDeletedAtIsNull(currentUser.getPartner().getId(), PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Transactional
    public WarehouseResponse create(WarehousesRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Partners partners = currentUser.getPartner();

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner yang diizinkan.");
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        Warehouses warehouse = new Warehouses();
        warehouse.setPartners(partners);
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setIsActive(true);

        if (warehousesRepository.existsByNameAndPartnersIdAndDeletedAtIsNull(warehouse.getName(), currentUser.getPartner().getId())) {
            throw new IllegalArgumentException("Warehouse sudah terdaftar.");
        }

        warehouse.setPartners(currentUser.getPartner());
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setCreatedBy(currentUser);

        return mapToResponse(warehousesRepository.save(warehouse));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Warehouses warehouse = warehousesRepository.findById(id)
                .filter(w -> w.getPartners().getId().equals(currentUser.getPartner().getId()))
                .orElseThrow(() -> new RuntimeException("Akses Ditolak: Warehouse tidak ditemukan"));

        warehouse.setDeletedAt(LocalDateTime.now());
        warehouse.setDeletedBy(currentUser);
        warehousesRepository.save(warehouse);
    }
}