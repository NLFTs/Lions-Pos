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
 *   3. PartnerRoleTemplateSeeder— seed 4 template role untuk semua partner
 */
@Component
@DependsOn("entityManagerFactory")
@RequiredArgsConstructor
public class MainSeeder {

    private final UserSeeder                userSeeder;
    private final PermissionSeeder          permissionSeeder;
    private final PartnerRoleTemplateSeeder partnerRoleTemplateSeeder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedAfterMigrations() {
        try {
            userSeeder.run();                   // 1. buat super admin user
            permissionSeeder.run();             // 2. buat roles & permissions (superadmin read-only + owner)
            partnerRoleTemplateSeeder.run();    // 3. seed 4 template role untuk semua partner
        } catch (Exception e) {
            throw new RuntimeException("Error seeding data", e);
        }
    }
}
