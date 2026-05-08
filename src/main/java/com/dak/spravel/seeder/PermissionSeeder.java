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
        {"partner",    "Partner",    "Manage blog partners"},
        {"category_product", "Category Product", "Manage product categories"},
        {"product",    "Product",    "Manage blog products"},
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

        {"partner.index",      "View All Partners",          "partner"},
        {"partner.show",       "View Partner Detail",        "partner"},
        {"partner.store",      "Create Partner",             "partner"},
        {"partner.update",     "Update Partner",             "partner"},
        {"partner.delete",     "Delete Partner",             "partner"},

        {"category_product.index",  "View All Categories",     "category_product"},
        {"category_product.show",   "View Category Detail",    "category_product"},
        {"category_product.store",  "Create Category",         "category_product"},
        {"category_product.update", "Update Category",         "category_product"},
        {"category_product.delete", "Delete Category",         "category_product"}, 

<<<<<<< HEAD
=======
        {"product.index",      "View All Products",          "product"},
        {"product.show",       "View Product Detail",        "product"},
        {"product.store",      "Create Product",             "product"},
        {"product.update",     "Update Product",             "product"},
        {"product.delete",     "Delete Product",             "product"},
        

>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
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
        Set<Permission> adminPartnersPerms = new HashSet<>();
        for (Permission p : allPerms) {
            String moduleSlug = p.getModule().getSlug();
            if (moduleSlug.equals("user") || 
                p.getSlug().equals("user.index") ||
                p.getSlug().equals("user.show") || 
                p.getSlug().equals("user.store") ||
                p.getSlug().equals("user.update") ||
                p.getSlug().equals("user.delete")){
                adminPartnersPerms.add(p);
            }

            if (moduleSlug.equals("post") ||
                p.getSlug().equals("category.index") ||
                p.getSlug().equals("category.show"))  {
                adminPartnersPerms.add(p);
            }

            if (moduleSlug.equals("category_product") ||
                p.getSlug().equals("category_product.index") ||
                p.getSlug().equals("category_product.show") || 
                p.getSlug().equals("category_product.store") ||
                p.getSlug().equals("category_product.update") ||
                p.getSlug().equals("category_product.delete"))  {
                adminPartnersPerms.add(p);
            }

            if (moduleSlug.equals("product") ||
                p.getSlug().equals("product.index") ||
                p.getSlug().equals("product.show") || 
                p.getSlug().equals("product.store") ||
                p.getSlug().equals("product.update") ||
                p.getSlug().equals("product.delete")) {
                adminPartnersPerms.add(p);
            }


        }
        Role adminPartnersRole = roleRepository.findBySlug("admin-partners").orElseGet(() -> {
            Role r = new Role();
            r.setSlug("admin-partners");
            r.setName("Admin Partners");
            return r;
        });
        adminPartnersRole.setPermissions(adminPartnersPerms);
        roleRepository.save(adminPartnersRole);

        Set<Permission> employeePartnersPerms = new HashSet<>();
        for (Permission p : allPerms) {
            String moduleSlug = p.getModule().getSlug();
            

            if (moduleSlug.equals("post") ||
                p.getSlug().equals("category.index") ||
                p.getSlug().equals("category.show"))  {
                employeePartnersPerms.add(p);
            }

            if (moduleSlug.equals("category_product") ||
                p.getSlug().equals("category_product.index") ||
                p.getSlug().equals("category_product.show") || 
                p.getSlug().equals("category_product.store") ||
                p.getSlug().equals("category_product.update") ||
                p.getSlug().equals("category_product.delete"))  {
                employeePartnersPerms.add(p);
            }

            if (moduleSlug.equals("product") ||
                p.getSlug().equals("product.index") ||
                p.getSlug().equals("product.show") || 
                p.getSlug().equals("product.store") ||
                p.getSlug().equals("product.update") ||
                p.getSlug().equals("product.delete")) {
                employeePartnersPerms.add(p);
            }


        }
        Role employeePartnersRole = roleRepository.findBySlug("employee-partners").orElseGet(() -> {
            Role r = new Role();
            r.setSlug("employee-partners");
            r.setName("Employee Partners");
            return r;
        });
        employeePartnersRole.setPermissions(employeePartnersPerms);
        roleRepository.save(employeePartnersRole);

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
