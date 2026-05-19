package com.dak.spravel.service.common;

import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
import com.dak.spravel.dto.request.partner.UpdatePartnerRequest;
import com.dak.spravel.dto.response.common.PartnerResponse;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();

        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equalsIgnoreCase("admin-partners") ||
                                role.getSlug().equalsIgnoreCase("employee")
                );

        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("admin"));

        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException(
                    "Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan."
            );
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getSlug().equals("admin") ||
                role.getSlug().equals("admin")
        );
    }

    private boolean isAdminPartnerAndEmployee(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getSlug().equals("employee") ||
                role.getSlug().equals("admin-partners")
        );
    }

    private Partners getValidatedPartner(Long id, User currentUser) {

        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: hanya admin yang boleh akses Partner.");
        }

        return partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partners", id));
    }


    public List<PartnerResponse> findAll() {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh akses Partner Master.");
        }

        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: hanya admin yang boleh lihat semua Partner.");
        }

        return partnerRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<PartnerResponse> findAll(int page, int size) {

        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh akses data paginated.");
        }

        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: hanya admin yang boleh akses data ini.");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

        return partnerRepository.findAll(pageRequest)
                .map(this::mapToResponse);
    }

    public PartnerResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh akses Partner.");
        }

        Partners partner = getValidatedPartner(id, currentUser);

        return mapToResponse(partner);
    }

    @Transactional
    public PartnerResponse create(CreatePartnerRequest request) {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh create Partner.");
        }

        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: hanya admin yang boleh create Partner.");
        }

        // Validasi admin user request wajib ada
        if (request.getAdmin() == null) {
            throw new IllegalArgumentException("Data admin wajib diisi.");
        }

        // Validasi duplikasi username
        if (userRepository.findByUsername(request.getAdmin().getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getAdmin().getUsername() + "' sudah digunakan.");
        }

        // Simpan Partner
        Partners partner = new Partners();
        partner.setName(request.getName());
        partner.setPlan(request.getPlan());
        partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        AuditHelper.setCreated(partner);
        Partners savedPartner = partnerRepository.save(partner);

        // Ambil role admin-partners
        Role adminPartnerRole = roleRepository.findBySlug("admin-partners")
                .orElseThrow(() -> new RuntimeException("Role 'admin-partners' tidak ditemukan di database. Pastikan seeder sudah berjalan."));

        // Buat Admin User untuk partner ini
        User adminUser = new User();
        adminUser.setUsername(request.getAdmin().getUsername());
        adminUser.setEmail(request.getAdmin().getEmail());
        adminUser.setPassword(passwordEncoder.encode(request.getAdmin().getPassword()));
        adminUser.setPartner(savedPartner);
        Set<Role> roles = new HashSet<>();
        roles.add(adminPartnerRole);
        adminUser.setRoles(roles);
        userRepository.save(adminUser);

        return mapToResponse(savedPartner);
    }

    @Transactional
    public PartnerResponse update(Long id, UpdatePartnerRequest request) {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh update Partner.");
        }

        Partners partner = getValidatedPartner(id, currentUser);

        if (request.getName() != null) {
            partner.setName(request.getName());
            partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        }

        if (request.getPlan() != null) {
            partner.setPlan(request.getPlan());
        }

        if (request.getIsActive() != null) {
            partner.setIsActive(request.getIsActive());
        }

        partner.setUpdatedBy(currentUser);

        AuditHelper.setUpdated(partner);

        return mapToResponse(partnerRepository.save(partner));
    }

    @Transactional
    public PartnerResponse softDelete(Long id) {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh delete Partner.");
        }

        Partners partner = getValidatedPartner(id, currentUser);

        partner.setIsActive(false);
        partner.setDeletedBy(currentUser);

        return mapToResponse(partnerRepository.save(partner));
    }

    @Transactional
    public PartnerResponse restore(Long id) {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh restore Partner.");
        }

        Partners partner = getValidatedPartner(id, currentUser);

        partner.setIsActive(true);
        partner.setUpdatedBy(currentUser);

        return mapToResponse(partnerRepository.save(partner));
    }

    @Transactional
    public void hardDelete(Long id) {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Ditolak: role ini tidak boleh hard delete Partner.");
        }

        Partners partner = getValidatedPartner(id, currentUser);

        partnerRepository.delete(partner);
    }

    public PartnerResponse mapToResponse(Partners partner) {
        if (partner == null) return null;

        PartnerResponse response = new PartnerResponse();
        response.setId(partner.getId());
        response.setName(partner.getName());
        response.setSlug(partner.getSlug());
        response.setPlan(partner.getPlan() != null ? partner.getPlan().name() : null);
        response.setIsActive(partner.getIsActive());
        response.setCreatedAt(partner.getCreatedAt());
        response.setUpdatedAt(partner.getUpdatedAt());
        response.setDeletedAt(partner.getDeletedAt());

        response.setCreatedBy(mapUser(partner.getCreatedBy()));
        response.setUpdatedBy(mapUser(partner.getUpdatedBy()));
        response.setDeletedBy(mapUser(partner.getDeletedBy()));

        return response;
    }

    private UserSimpleDto mapUser(User user) {
        if (user == null) return null;

        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}