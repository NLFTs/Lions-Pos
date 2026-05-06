package com.dak.spravel.seeder;

import com.dak.spravel.model.auth.Module;
import com.dak.spravel.model.auth.Permission;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.repository.auth.ModuleRepository;
import com.dak.spravel.repository.auth.PermissionRepository;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * Seeds modules, permissions, and default admin/editor roles.
 */
@Component
@RequiredArgsConstructor
public class PermissionSeeder {
    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Value("${app.enable.seeder:false}")
    private boolean enableSeeder;

    // slug, name, description
    private static final String[][] ALL_MODULES = {
        {"post",       "Post",       "Manage blog posts"},
        {"category",   "Category",   "Manage post categories"},
        {"role",       "Role",       "Manage user roles"},
        {"permission", "Permission", "Manage system permissions"},
        {"module",     "Module",     "Manage permission modules"},
        {"user",       "User",       "Manage users"},
        {"log",        "Log",        "View audit logs"},
    };

    // slug, name, moduleSlug
    private static final String[][] ALL_PERMISSIONS = {
        {"post.index",      "View All Posts",          "post"},
        {"post.show",       "View Post Detail",        "post"},
        {"post.store",      "Create Post",             "post"},
        {"post.update",     "Update Post",             "post"},
        {"post.delete",     "Delete Post",             "post"},

        {"category.index",  "View All Categories",     "category"},
        {"category.show",   "View Category Detail",    "category"},
        {"category.store",  "Create Category",         "category"},
        {"category.update", "Update Category",         "category"},
        {"category.delete", "Delete Category",         "category"},

        {"role.index",      "View All Roles",          "role"},
        {"role.show",       "View Role Detail",        "role"},
        {"role.store",      "Create Role",             "role"},
        {"role.update",     "Update Role",             "role"},
        {"role.delete",     "Delete Role",             "role"},

        {"permission.index",  "View All Permissions",   "permission"},
        {"permission.show",   "View Permission Detail", "permission"},
        {"permission.store",  "Create Permission",      "permission"},
        {"permission.update", "Update Permission",      "permission"},
        {"permission.delete", "Delete Permission",      "permission"},


        {"module.index",  "View All Modules",   "module"},
        {"module.show",   "View Module Detail", "module"},
        {"module.store",  "Create Module",      "module"},
        {"module.update", "Update Module",      "module"},
        {"module.delete", "Delete Module",      "module"},

        {"user.index",  "View All Users",   "user"},
        {"user.show",   "View User Detail", "user"},
        {"user.store",  "Create User",      "user"},
        {"user.update", "Update User",      "user"},
        {"user.delete", "Delete User",      "user"},

        {"log.index",  "View All Logs",  "log"},
        {"log.show",   "View Log Detail", "log"},
    };

    @Transactional
    public void run() {
        if (!enableSeeder) return;

        // 1. Create modules
        for (String[] mod : ALL_MODULES) {
            if (!moduleRepository.existsBySlug(mod[0])) {
                Module m = new Module();
                m.setSlug(mod[0]);
                m.setName(mod[1]);
                m.setDescription(mod[2]);
                moduleRepository.save(m);
            }
        }

        // 2. Create permissions linked to their module
        for (String[] perm : ALL_PERMISSIONS) {
            if (!permissionRepository.existsBySlug(perm[0])) {
                Module module = moduleRepository.findBySlug(perm[2])
                        .orElseThrow(() -> new RuntimeException("Module not found: " + perm[2]));
                Permission p = new Permission();
                p.setSlug(perm[0]);
                p.setName(perm[1]);
                p.setModule(module);
                permissionRepository.save(p);
            }
        }

        List<Permission> allPerms = permissionRepository.findAll();
        Set<Permission> allPermsSet = new HashSet<>(allPerms);

        // 3. Create "admin" role with ALL permissions
        Role adminRole = roleRepository.findBySlug("admin").orElseGet(() -> {
            Role r = new Role();
            r.setSlug("admin");
            r.setName("Administrator");
            return r;
        });
        adminRole.setPermissions(allPermsSet);
        roleRepository.save(adminRole);

        // 4. Create "editor" role with post.* + category read
        Set<Permission> editorPerms = new HashSet<>();
        for (Permission p : allPerms) {
            String moduleSlug = p.getModule().getSlug();
            if (moduleSlug.equals("post") ||
                p.getSlug().equals("category.index") ||
                p.getSlug().equals("category.show")) {
                editorPerms.add(p);
            }
        }
        Role editorRole = roleRepository.findBySlug("editor").orElseGet(() -> {
            Role r = new Role();
            r.setSlug("editor");
            r.setName("Editor");
            return r;
        });
        editorRole.setPermissions(editorPerms);
        roleRepository.save(editorRole);

        // 5. Assign "admin" role to user "su"
        userRepository.findByUsername("su").ifPresent(su -> {
            Role managed = roleRepository.findBySlug("admin").orElseThrow();
            if (su.getRoles().stream().noneMatch(r -> "admin".equals(r.getSlug()))) {
                su.getRoles().add(managed);
                userRepository.save(su);
            }
        });
    }
}
