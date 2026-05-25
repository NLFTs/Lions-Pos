package com.dak.spravel.seeder;

import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.service.auth.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Migrasi role slug lama ke slug baru:
 *   admin-partners → owner
 *   employee-partners → employee
 *
 * Dijalankan setelah PermissionSeeder agar role baru sudah ada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleMigrationSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionCacheService permissionCacheService;

    @Value("${app.enable.seeder:false}")
    private boolean enableSeeder;

    @Transactional
    public void run() {
        if (!enableSeeder) return;

        migrateRoleSlug("admin-partners", "owner");
        migrateRoleSlug("employee-partners", "employee");

        // Evict semua cache agar permission baru langsung berlaku
        permissionCacheService.evictAll();
        log.info("[RoleMigrationSeeder] ✅ Migrasi role selesai, cache di-evict.");
    }

    /**
     * Untuk setiap user yang punya role oldSlug, tambahkan role newSlug
     * (jika belum punya) dan hapus role oldSlug.
     */
    private void migrateRoleSlug(String oldSlug, String newSlug) {
        Optional<Role> newRoleOpt = roleRepository.findAll().stream()
                .filter(r -> r.getSlug().equals(newSlug) && r.getPartner() == null)
                .findFirst();

        if (newRoleOpt.isEmpty()) {
            log.warn("[RoleMigrationSeeder] Role '{}' tidak ditemukan, skip migrasi dari '{}'.", newSlug, oldSlug);
            return;
        }

        Role newRole = newRoleOpt.get();

        List<User> usersWithOldRole = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getSlug().equals(oldSlug)))
                .toList();

        if (usersWithOldRole.isEmpty()) {
            log.info("[RoleMigrationSeeder] Tidak ada user dengan role '{}', skip.", oldSlug);
            return;
        }

        for (User user : usersWithOldRole) {
            boolean alreadyHasNew = user.getRoles().stream()
                    .anyMatch(r -> r.getSlug().equals(newSlug));

            // Hapus role lama
            user.getRoles().removeIf(r -> r.getSlug().equals(oldSlug));

            // Tambah role baru jika belum ada
            if (!alreadyHasNew) {
                user.getRoles().add(newRole);
            }

            userRepository.save(user);
            log.info("[RoleMigrationSeeder] User '{}': '{}' → '{}'", user.getUsername(), oldSlug, newSlug);
        }
    }
}
