package com.dak.spravel.service.common;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
import com.dak.spravel.dto.request.partner.UpdatePartnerRequest;
import com.dak.spravel.dto.response.common.PartnerResponse;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.BranchWarehousesRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.model.inventory.BranchWarehouses;
import com.dak.spravel.util.AuditHelper;
import com.dak.spravel.dto.request.partner.BranchRequest;
import com.dak.spravel.dto.request.partner.BranchWarehouseMappingRequest;
import com.dak.spravel.dto.request.partner.WarehouseRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerService {

    private final BranchesRepository branchesRepository;
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BranchesRepository branchesRepository;
    private final WarehousesRepository warehousesRepository;
    private final BranchWarehousesRepository branchWarehousesRepository;

    // --- HELPER METHODS ---

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Woi login dulu!");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User login gak ketemu di DB"));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin"));
    }

    private Partners getValidatedPartner(Long id) {
        User currentUser = getAuthenticatedUser();
        Partners partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", id));

        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya admin yang bisa akses data partner!");
        }

        return partner;
    }

    private void createInternalUser(CreatePartnerRequest.UserRequest userReq, Partners partner, String roleSlug) {
        if (userRepository.existsByUsername(userReq.getUsername())) {
            throw new IllegalArgumentException("Username " + userReq.getUsername() + " Sudah Terdaftar!");
        }


        User user = new User();
        user.setUsername(userReq.getUsername());
        user.setEmail(userReq.getEmail());
        user.setPassword(passwordEncoder.encode(userReq.getPassword()));
        user.setPartner(partner);

        Role role = roleRepository.findBySlug(roleSlug)
                .orElseThrow(() -> new RuntimeException("Role " + roleSlug + " Tidak Ada!"));
        user.setRoles(Collections.singleton(role));

        AuditHelper.setCreated(user);
        userRepository.save(user);
    }

    // --- LOGIC UTAMA ---

    // GET ALL
    public List<PartnerResponse> findAll() {
        if (!isAdmin(getAuthenticatedUser())) {
            throw new RuntimeException("Akses Ditolak: Hanya admin yang bisa akses data partner!");
        }

        return partnerRepository.findAll(Sort.by("name"))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET BY ID
    public PartnerResponse findById(Long id) {
        if (!isAdmin(getAuthenticatedUser())) {
            throw new RuntimeException("Akses Ditolak: Hanya admin yang bisa akses data partner!");
        }
        
        return mapToResponse(getValidatedPartner(id));
    }

    // CREATE
    @Transactional
    public PartnerResponse createPartner(CreatePartnerRequest request) {
        User currentUser = getAuthenticatedUser();

        log.info("[DEBUG] User: {} mencoba create partner dengan role slug: admin", currentUser.getUsername());

        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya user dengan role 'admin' yang bisa bikin Partner!");
        }

        if (partnerRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Nama Partner sudah ada, cari nama lain!");
        }

        // Save Partner
        Partners partner = new Partners();
        partner.setName(request.getName());
        partner.setPlan(request.getPlan());
        partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        partner.setIsActive(true);
        partner.setCreatedBy(currentUser);
        partner.setUpdatedBy(currentUser);
        partner.setCreatedAt(LocalDateTime.now());

        Partners savedPartner = partnerRepository.save(partner);

        // Buat Branch
        if (request.getBranch() != null) {
            Branches branch = new Branches(); 
            branch.setName(request.getBranch().getName());
            branch.setAddress(request.getBranch().getAddress());
            branch.setPartners(savedPartner);
            branch.setCreatedBy(currentUser);
            
            AuditHelper.setCreated(branch); 
            
            branchesRepository.save(branch);
        }

        // Buat User Admin Partner
        createInternalUser(request.getAdmin(), savedPartner, "admin-partners");

        if (request.getEmployees() != null && !request.getEmployees().isEmpty()) {
            for (CreatePartnerRequest.UserRequest empReq : request.getEmployees()) {
                createInternalUser(empReq, savedPartner, "employee-partners");
            }
        }

        return mapToResponse(savedPartner);
    }

    private void setupPartnerResources(Partners partner, CreatePartnerRequest request, User currentUser) {
        // 1. Buat Branch secara bulk
        List<Branches> savedBranches = new ArrayList<>();
        if (request.getBranches() != null && !request.getBranches().isEmpty()) {
            for (BranchRequest br : request.getBranches()) {
                Branches branch = new Branches();
                branch.setPartners(partner);
                branch.setName(br.getName());
                branch.setAddress(br.getAddress());
                savedBranches.add(branchesRepository.save(branch));
            }
        } else {
            Branches defaultBranch = new Branches();
            defaultBranch.setPartners(partner);
            defaultBranch.setName("Branch Utama - " + partner.getName());
            defaultBranch.setAddress("-");
            savedBranches.add(branchesRepository.save(defaultBranch));
        }

        // 2. Buat Warehouse secara bulk
        List<Warehouses> savedWarehouses = new ArrayList<>();
        if (request.getWarehouses() != null && !request.getWarehouses().isEmpty()) {
            for (WarehouseRequest wr : request.getWarehouses()) {
                Warehouses warehouse = new Warehouses();
                warehouse.setPartners(partner);
                warehouse.setName(wr.getName());
                warehouse.setAddress(wr.getAddress());
                warehouse.setIsActive(true);
                savedWarehouses.add(warehousesRepository.save(warehouse));
            }
        } else {
            Warehouses defaultWarehouse = new Warehouses();
            defaultWarehouse.setPartners(partner);
            defaultWarehouse.setName("Gudang Utama - " + partner.getName());
            defaultWarehouse.setAddress("-");
            defaultWarehouse.setIsActive(true);
            savedWarehouses.add(warehousesRepository.save(defaultWarehouse));
        }

        // 3. Mapping BranchWarehouse
        if (request.getBranchWarehouses() != null && !request.getBranchWarehouses().isEmpty()) {
            for (BranchWarehouseMappingRequest m : request.getBranchWarehouses()) {
                if (m.getBranchIndex() < 0 || m.getBranchIndex() >= savedBranches.size() ||
                    m.getWarehouseIndex() < 0 || m.getWarehouseIndex() >= savedWarehouses.size()) {
                    throw new IllegalArgumentException("Index Branch/Warehouse mapping tidak valid!");
                }

                BranchWarehouses bw = new BranchWarehouses();
                bw.setBranches(savedBranches.get(m.getBranchIndex()));
                bw.setWarehouses(savedWarehouses.get(m.getWarehouseIndex()));
                bw.setCreatedAt(LocalDateTime.now());
                bw.setCreatedBy(currentUser);
                branchWarehousesRepository.save(bw);
            }
        } else {
            Branches firstBranch = savedBranches.get(0);
            for (Warehouses w : savedWarehouses) {
                BranchWarehouses bw = new BranchWarehouses();
                bw.setBranches(firstBranch);
                bw.setWarehouses(w);
                bw.setCreatedAt(LocalDateTime.now());
                bw.setCreatedBy(currentUser);
                branchWarehousesRepository.save(bw);
            }
        }
    }

    // UPDATE
    @Transactional
    public PartnerResponse update(Long id, UpdatePartnerRequest request) {
        Partners partner = getValidatedPartner(id);

        if (!isAdmin(getAuthenticatedUser())){
            throw new RuntimeException("Akses Ditolak: Hanya admin yang bisa update data partner!");
        }

        if (request.getName() != null) {
            if (!request.getName().equals(partner.getName()) &&
                    partnerRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Nama Partner sudah ada!");
            }
            partner.setName(request.getName());
            partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        }

        if (request.getPlan() != null) partner.setPlan(request.getPlan());
        if (request.getIsActive() != null) partner.setIsActive(request.getIsActive());

        partner.setUpdatedBy(getAuthenticatedUser());
        partner.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(partnerRepository.save(partner));
    }

    // SOFT DELETE
    @Transactional
    public PartnerResponse softDelete(Long id) {
        Partners partner = getValidatedPartner(id);

        if (!isAdmin(getAuthenticatedUser())){
            throw new RuntimeException("Akses Ditolak: Hanya Super Admin yang bisa soft delete data partner!");
        }

        partner.setIsActive(false);
        partner.setUpdatedAt(LocalDateTime.now());
        partner.setUpdatedBy(getAuthenticatedUser());

        return mapToResponse(partnerRepository.save(partner));
    }

    // RESTORE
    @Transactional
    public PartnerResponse restore(Long id) {
        Partners partner = getValidatedPartner(id);
        
        if (!isAdmin(getAuthenticatedUser())){
            throw new RuntimeException("Akses Ditolak: Hanya Super Admin yang bisa restore data partner!");
        }
        
        partner.setIsActive(true);  
        partner.setUpdatedAt(LocalDateTime.now());
        partner.setUpdatedBy(getAuthenticatedUser());

        return mapToResponse(partnerRepository.save(partner));
    }

    // HARD DELETE
    @Transactional
    public void hardDelete(Long id) {
        Partners partner = getValidatedPartner(id);
        
        if (!isAdmin(getAuthenticatedUser())){
            throw new RuntimeException("Akses Ditolak: Hanya Super Admin yang bisa hard delete data partner!");
        }
        
        partnerRepository.delete(partner);
    }

    private PartnerResponse mapToResponse(Partners partner) {
        PartnerResponse response = new PartnerResponse();
        response.setId(partner.getId());
        response.setName(partner.getName());
        response.setSlug(partner.getSlug());
        response.setPlan(partner.getPlan() != null ? partner.getPlan().name() : null);
        response.setIsActive(partner.getIsActive());
        response.setCreatedAt(partner.getCreatedAt());
        response.setUpdatedAt(partner.getUpdatedAt());
        response.setDeletedAt(partner.getDeletedAt());

        // Mapping User ke UserSimpleDto (CreatedBy)
        if (partner.getCreatedBy() != null) {
            UserSimpleDto userDto = new UserSimpleDto();
            userDto.setId(partner.getCreatedBy().getId());
            userDto.setUsername(partner.getCreatedBy().getUsername());
            // Set field lain di UserSimpleDto lu
            response.setCreatedBy(userDto);
        }

        if (partner.getUpdatedBy() != null) {
            UserSimpleDto userDto = new UserSimpleDto();
            userDto.setId(partner.getUpdatedBy().getId());
            userDto.setUsername(partner.getUpdatedBy().getUsername());
            // Set field lain di UserSimpleDto lu
            response.setUpdatedBy(userDto);
        }

        if (partner.getDeletedBy() != null) {
            UserSimpleDto userDto = new UserSimpleDto();
            userDto.setId(partner.getDeletedBy().getId());
            userDto.setUsername(partner.getDeletedBy().getUsername());
            // Set field lain di UserSimpleDto lu
            response.setDeletedBy(userDto);
        }

        // Mapping untuk field partner_id (PartnerSimpleDto)
        // Biasanya ini diisi info ringkas partner itu sendiri
        PartnerSimpleDto simpleDto = new PartnerSimpleDto();
        simpleDto.setId(partner.getId());
        simpleDto.setName(partner.getName());

        return response;
    }
}