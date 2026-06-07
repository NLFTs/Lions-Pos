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
import com.dak.spravel.seeder.PartnerRoleTemplateSeeder;
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
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PartnerRoleTemplateSeeder partnerRoleTemplateSeeder;

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

    // ⛔ HAK AKSES DEWA: Proteksi ketat untuk fitur khusus Super Admin Master
    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini hanya dapat dikelola oleh Super Admin Global.");
        }
    }

    // ─── 🚀 MAIN CORE METHODS ───────────────────────────────────────────────────

    public List<PartnerResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        // Hanya Super Admin Global yang boleh melihat list seluruh perusahaan di sistem
        checkSuperAdminOnly(currentUser);

        return partnerRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<PartnerResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return partnerRepository.findAll(pageable).map(this::mapToResponse);
    }

    public PartnerResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "partner.show");

        // 🛡️ Multi-Tenant Guard: Partner biasa cuma boleh ngintip data perusahaannya sendiri!
        if (currentUser.getPartner() != null && !currentUser.getPartner().getId().equals(id)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki izin melihat data partner lain.");
        }

        Partners partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partners", id));
        return mapToResponse(partner);
    }

    @Transactional
    public PartnerResponse create(CreatePartnerRequest request) {
        User currentUser = getAuthenticatedUser();
        // Cuma Super Admin Global yang bisa melahirkan tenant/partner baru di Spravel
        checkSuperAdminOnly(currentUser);

        if (request.getAdmin() == null) {
            throw new IllegalArgumentException("Data admin utama partner wajib diisi.");
        }

        if (userRepository.findByUsername(request.getAdmin().getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getAdmin().getUsername() + "' sudah digunakan.");
        }

        // 1. Simpan entitas Partner Baru
        Partners partner = new Partners();
        partner.setName(request.getName());
        partner.setPlan(request.getPlan());
        partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        partner.setCreatedAt(LocalDateTime.now());
        partner.setCreatedBy(currentUser);
        partner.setIsActive(true);
        Partners savedPartner = partnerRepository.save(partner);

        // 2. Pastikan role template global (admin-partner, pengelola-gudang, dll) tersedia
        partnerRoleTemplateSeeder.seedForPartner(savedPartner);

        // 3. Role admin utama: admin-partner (utama) atau owner (fallback dari seed lama)
        Role adminPartnerRole = roleRepository.findBySlug("admin-partner")
                .or(() -> roleRepository.findBySlug("owner"))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role admin partner tidak ditemukan. Restart aplikasi agar seeder berjalan.", 0L));

        // 4. Daftarkan akun User Admin Utama milik Partner tersebut
        User adminUser = new User();
        adminUser.setUsername(request.getAdmin().getUsername());
        adminUser.setEmail(request.getAdmin().getEmail());
        adminUser.setFullname("Admin " + savedPartner.getName());
        adminUser.setPassword(passwordEncoder.encode(request.getAdmin().getPassword()));
        adminUser.setPartner(savedPartner);
        adminUser.setIsActive(true);
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminPartnerRole);
        adminUser.setRoles(roles);
        userRepository.save(adminUser);

        return mapToResponse(savedPartner);
    }

    @Transactional
    public PartnerResponse update(Long id, UpdatePartnerRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "partner.update");

        // 🛡️ Multi-Tenant Guard: Mencegah Owner mengedit profil perusahaan milik kompetitor/partner lain
        if (currentUser.getPartner() != null && !currentUser.getPartner().getId().equals(id)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki izin mengubah data partner lain.");
        }

        Partners partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partners", id));

        if (request.getName() != null) {
            partner.setName(request.getName());
            partner.setSlug(request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        }

        if (request.getPlan() != null) {
            // Penggantian plan PRO/BASIC hanya hak milik Super Admin Global
            if (currentUser.getPartner() != null && !partner.getPlan().equals(request.getPlan())) {
                throw new RuntimeException("Akses Ditolak: Perubahan Paket Langganan hanya bisa diproses oleh Super Admin Global.");
            }
            partner.setPlan(request.getPlan());
        }

        if (request.getIsActive() != null) {
            // Pembekuan/Aktivasi tenant hanya hak milik Super Admin Global
            if (currentUser.getPartner() != null) {
                throw new RuntimeException("Akses Ditolak: Status aktifasi tenant hanya bisa diubah oleh Super Admin Global.");
            }
            partner.setIsActive(request.getIsActive());
        }

        partner.setUpdatedBy(currentUser);
        partner.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(partnerRepository.save(partner));
    }

    @Transactional
    public PartnerResponse softDelete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser); // Pembekuan total lewat softdelete wajib Super Admin

        Partners partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partners", id));

        partner.setIsActive(false);
        partner.setDeletedBy(currentUser);
        partner.setDeletedAt(LocalDateTime.now());

        return mapToResponse(partnerRepository.save(partner));
    }

    @Transactional
    public PartnerResponse restore(Long id) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        Partners partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partners", id));

        partner.setIsActive(true);
        partner.setUpdatedBy(currentUser);
        partner.setDeletedAt(null);

        return mapToResponse(partnerRepository.save(partner));
    }

    @Transactional
    public void hardDelete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        Partners partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partners", id));

        partnerRepository.delete(partner);
    }

    // ─── 🔄 PRIVATE UTILS & MAPPERS ────────────────────────────────────────────

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