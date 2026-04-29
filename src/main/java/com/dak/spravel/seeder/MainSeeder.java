package com.dak.spravel.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Orchestrates all seeders on ApplicationReadyEvent.
 */
@Component
@DependsOn("entityManagerFactory")
@RequiredArgsConstructor
public class MainSeeder {

    private final CategorySeeder categorySeeder;
    private final UserSeeder userSeeder;
    private final PermissionSeeder permissionSeeder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedAfterMigrations() {
        try {
            // categorySeeder.run();
            userSeeder.run();         // must run before permissionSeeder
            permissionSeeder.run();   // assigns admin role to su
        } catch (Exception e) {
            throw new RuntimeException("Error seeding data", e);
        }
    }
}
