package com.dak.spravel.service.common;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
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
        }

        // 1. Save Partner
        Partners partner = new Partners();
        partner.setName(request.getName());
        partner.setPlan(request.getPlan());
        partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        partner.setIsActive(true);
        
        AuditHelper.setCreated(partner); 
        Partners savedPartner = partnerRepository.save(partner);

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
    }
}