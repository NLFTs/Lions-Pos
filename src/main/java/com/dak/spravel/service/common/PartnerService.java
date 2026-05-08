package com.dak.spravel.service.common;

<<<<<<< HEAD
=======
import java.time.LocalDateTime;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Sort;
<<<<<<< HEAD
=======
import org.springframework.data.repository.query.parser.Part;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
<<<<<<< HEAD
=======
import com.dak.spravel.dto.request.partner.UpdatePartnerRequest;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.util.AuditHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerService {

<<<<<<< HEAD
    private final PartnerRepository partnerRepository; 
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // --- LOGIC UTAMA ---

    @Transactional
    public Partners createPartner(CreatePartnerRequest request) {   
        User currentUser = getAuthenticatedUser();

        log.info("[DEBUG] User: {} mencoba create partner dengan role slug: admin", currentUser.getUsername());

        // KARENA SLUG LO 'admin', MAKA CEKNYA KE 'admin'
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Cuma user dengan role 'admin' yang bisa bikin Partner!");
        }

        if (partnerRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Nama Partner sudah ada, cari nama lain Mip!");
=======
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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
    public List<Partners> findAll() {
        getAuthenticatedUser();
        return partnerRepository.findByDeletedAtIsNull(Sort.by("name").ascending());
    }

    // GET BY ID
    public Partners findById(Long id) {
        return getValidatedPartner(id);
    }

    // CREATE
    @Transactional
    public Partners createPartner(CreatePartnerRequest request) {
        User currentUser = getAuthenticatedUser();

        log.info("[DEBUG] User: {} mencoba create partner dengan role slug: admin", currentUser.getUsername());

        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Hanya user dengan role 'admin' yang bisa bikin Partner!");
        }

        if (partnerRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Nama Partner sudah ada, cari nama lain!");
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        }

        // 1. Save Partner
        Partners partner = new Partners();
        partner.setName(request.getName());
        partner.setPlan(request.getPlan());
        partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        partner.setIsActive(true);
<<<<<<< HEAD
        
        AuditHelper.setCreated(partner); 
        Partners savedPartner = partnerRepository.save(partner);
=======
        partner.setCreatedBy(currentUser);

        AuditHelper.setCreated(partner);
        Partners savedPartner = partnerRepository.save(partner);

        // 2. Buat User Admin Partner
        createInternalUser(request.getAdmin(), savedPartner, "admin-partners");

        // 3. Buat User Karyawan (opsional)
        if (request.getEmployees() != null && !request.getEmployees().isEmpty()) {
            for (CreatePartnerRequest.UserRequest empReq : request.getEmployees()) {
                createInternalUser(empReq, savedPartner, "employee-partners");
            }
        }

        return savedPartner;
    }

    // UPDATE
    @Transactional
    public Partners update(Long id, UpdatePartnerRequest request) {
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
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635

        // 2. Buat User Admin Partner (Slug: admin-partners)
        createInternalUser(request.getAdmin(), savedPartner, "admin-partners");

        // 3. Buat User Karyawan (Slug: employee-partners)
        if (request.getEmployees() != null && !request.getEmployees().isEmpty()) {
            for (CreatePartnerRequest.UserRequest empReq : request.getEmployees()) {
                createInternalUser(empReq, savedPartner, "employee-partners");
            }
        }

        return savedPartner;
    }

<<<<<<< HEAD
    // --- HELPER METHODS ---

    private void createInternalUser(CreatePartnerRequest.UserRequest userReq, Partners partner, String roleSlug) {
        if (userRepository.existsByUsername(userReq.getUsername())) {
            throw new IllegalArgumentException("Username " + userReq.getUsername() + " udah kepake!");
        }

        User user = new User();
        user.setUsername(userReq.getUsername());
        user.setEmail(userReq.getEmail());
        user.setPassword(passwordEncoder.encode(userReq.getPassword()));
        user.setPartner(partner);

        Role role = roleRepository.findBySlug(roleSlug)
                .orElseThrow(() -> new RuntimeException("Role " + roleSlug + " gak ada di DB, cek Seeder lo!"));
        user.setRoles(Collections.singleton(role));

        AuditHelper.setCreated(user);

        userRepository.save(user);
    }

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

        if (!isAdmin(currentUser) && !partner.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bukan data lo, gak boleh akses!");
        }

        return partner;
=======
    // SOFT DELETE
    @Transactional
    public Partners softDelete(Long id) {
        Partners partner = getValidatedPartner(id);

        if (!isAdmin(getAuthenticatedUser())){
            throw new RuntimeException("Akses Ditolak: Hanya Super Admin yang bisa soft delete data partner!");
        }

        partner.setIsActive(false);
        partner.setDeletedAt(LocalDateTime.now());
        partner.setUpdatedBy(getAuthenticatedUser());

        AuditHelper.setUpdated(partner);
        return partnerRepository.save(partner);
    }

    // RESTORE
    @Transactional
    public Partners restore(Long id) {
        Partners partner = getValidatedPartner(id);
        
        if (!isAdmin(getAuthenticatedUser())){
            throw new RuntimeException("Akses Ditolak: Hanya Super Admin yang bisa restore data partner!");
        }
        
        partner.setIsActive(true);
        partner.setUpdatedAt(LocalDateTime.now());
        partner.setUpdatedBy(getAuthenticatedUser());
        AuditHelper.setUpdated(partner);
        return partnerRepository.save(partner);
    }

    // HARD DELETE
    @Transactional
    public void hardDelete(Long id) {
        Partners partner = getValidatedPartner(id);
        
        if (!isAdmin(getAuthenticatedUser())){
            throw new RuntimeException("Akses Ditolak: Hanya Super Admin yang bisa hard delete data partner!");
        }
        
        partnerRepository.delete(partner);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }
}