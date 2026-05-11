package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.WarehousesRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehousesService {

    private final WarehousesRepository warehousesRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Warehouse.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private Warehouses getValidatedWarehouse(Long id, User currentUser) {
        Warehouses warehouse = warehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));

        if (currentUser.getPartner() == null ||
                !warehouse.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses warehouse dari partner lain.");
        }

        return warehouse;
    }

    // GET ALL
    public List<Warehouses> findAll() {
        User currentUser = getAuthenticatedUser();
        return warehousesRepository.findByPartnersId(currentUser.getPartner().getId());
    }

    // GET ALL PAGINATED
    public Page<Warehouses> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        return warehousesRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    // GET BY ID
    public Warehouses findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedWarehouse(id, currentUser);
    }

    // CREATE
    public Warehouses create(WarehousesRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        Warehouses warehouse = new Warehouses();
        warehouse.setPartners(partner);
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return warehousesRepository.save(warehouse);
    }

    // UPDATE
    public Warehouses update(Long id, WarehousesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        Warehouses warehouse = getValidatedWarehouse(id, currentUser);

        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        if (request.getIsActive() != null) warehouse.setIsActive(request.getIsActive());

        return warehousesRepository.save(warehouse);
    }

    // DELETE
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Warehouses warehouse = getValidatedWarehouse(id, currentUser);
        warehousesRepository.delete(warehouse);
    }
}