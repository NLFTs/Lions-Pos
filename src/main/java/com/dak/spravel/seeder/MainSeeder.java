package com.dak.spravel.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Orchestrates all seeders on ApplicationReadyEvent.
 * Urutan eksekusi:
 *   1. UserSeeder               — buat user su (super admin)
 *   2. PermissionSeeder         — buat modules, permissions, roles, assign ke su
 *   3. RoleMigrationSeeder      — migrasi user lama: admin-partners→owner, employee-partners→employee
 *   4. PartnerRoleTemplateSeeder— seed 4 template role (admin-partner, pengelola-gudang,
 *                                  pengelola-cabang, kasir) untuk setiap partner yang ada
 *   5. PartnerSeeder            — buat PT NLFTs beserta data lengkapnya
 */
@Component
@DependsOn("entityManagerFactory")
@RequiredArgsConstructor
public class MainSeeder {

    private final UserSeeder                userSeeder;
    private final PermissionSeeder          permissionSeeder;
    private final PartnerSeeder             partnerSeeder;
    private final RoleMigrationSeeder       roleMigrationSeeder;
    private final PartnerRoleTemplateSeeder partnerRoleTemplateSeeder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedAfterMigrations() {
        try {
            userSeeder.run();                   // 1. buat super admin user
            permissionSeeder.run();             // 2. buat roles & permissions (superadmin read-only + owner)
            roleMigrationSeeder.run();          // 3. migrasi slug role lama
            partnerRoleTemplateSeeder.run();    // 4. seed 4 template role untuk semua partner
            partnerSeeder.run();                // 5. buat PT NLFTs + semua data terkait
        } catch (Exception e) {
            throw new RuntimeException("Error seeding data", e);
        }
    }
}
