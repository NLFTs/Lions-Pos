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
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * Seeds modules, permissions, and default admin/editor roles.
 */
@Slf4j
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
        {"partner",    "Partner",    "Manage blog partners"},
        {"branch",     "Branch",     "Manage partner branches"},
        {"warehouse",  "Warehouse",  "Manage partner warehouses"},
        {"branch_warehouse", "Branch Warehouse", "Manage branch warehouses"},
        {"stock_balance",      "Stock Balance",      "Manage stock balances"},
        {"stock_mutation", "Stock Mutation", "Manage stock mutations"},
        {"category", "Category", "Manage product categories"},
        {"produk",    "Product",    "Manage blog products"},
        {"product_photo", "Product Photo", "Manage blog product photo"},
        {"role",       "Role",       "Manage user roles"},
        {"permission", "Permission", "Manage system permissions"},
        {"module",     "Module",     "Manage permission modules"},
        {"user",       "User",       "Manage users"},
        {"log",        "Log",        "View audit logs"},
        {"dashboard",  "Dashboard",  "View dashboard metrics"},
        {"pos",        "Point of Sale", "Access cashier system"},
        {"report",     "Reports",    "View business reports"},
        {"transfer_request", "Transfer Request", "Manage stock transfers"},
        {"stock_opname", "Stock Opname", "Manage stock opname"},
        {"purchase_order", "Purchase Order", "Manage purchase orders"},
    };

    // slug, name, moduleSlug
    private static final String[][] ALL_PERMISSIONS = {

        {"partner.index",      "View All Partners",          "partner"},
        {"partner.show",       "View Partner Detail",        "partner"},
        {"partner.store",      "Create Partner",             "partner"},
        {"partner.update",     "Update Partner",             "partner"},
        {"partner.delete",     "Delete Partner",             "partner"},

        {"category.index",  "View All Categories",     "category"},
        {"category.show",   "View Category Detail",    "category"},
        {"category.store",  "Create Category",         "category"},
        {"category.update", "Update Category",         "category"},
        {"category.delete", "Delete Category",         "category"}, 

        {"produk.index",      "View All Products",          "produk"},
        {"produk.show",       "View Product Detail",        "produk"},
        {"produk.store",      "Create Product",             "produk"},
        {"produk.update",     "Update Product",             "produk"},
        {"produk.delete",     "Delete Product",             "produk"},
        
        {"product_photo.index",      "View All Products",          "product_photo"},
        {"product_photo.store",      "Create Product",             "product_photo"},
        {"product_photo.update",     "Update Product",             "product_photo"},
        {"product_photo.delete",     "Delete Product",             "product_photo"},
        
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

        {"warehouse.index",  "View All Warehouses",   "warehouse"},
        {"warehouse.show",   "View Warehouse Detail", "warehouse"},
        {"warehouse.store",  "Create Warehouse",      "warehouse"},
        {"warehouse.update", "Update Warehouse",      "warehouse"},
        {"warehouse.delete", "Delete Warehouse",      "warehouse"},
        
        {"branch.index",  "View All Branches",   "branch"},
        {"branch.show",   "View Branch Detail", "branch"},
        {"branch.store",  "Create Branch",      "branch"},
        {"branch.update", "Update Branch",      "branch"},
        {"branch.delete", "Delete Branch",      "branch"},

        {"branch_warehouse.index",  "View All Branch Warehouses",   "branch_warehouse"},
        {"branch_warehouse.show",   "View Branch Warehouse Detail", "branch_warehouse"},
        {"branch_warehouse.store",  "Create Branch Warehouse",      "branch_warehouse"},
        {"branch_warehouse.update", "Update Branch Warehouse",      "branch_warehouse"},
        {"branch_warehouse.delete", "Delete Branch Warehouse",      "branch_warehouse"},

        {"stock_balance.index",  "View All Stock Balances",   "stock_balance"},
        {"stock_balance.store",  "Create Stock Balance",      "stock_balance"},
        {"stock_balance.show",   "View Stock Balance Detail", "stock_balance"},
        {"stock_balance.update", "Update Stock Balance",      "stock_balance"},

        {"stock_mutation.index",  "View All Stock Mutations",   "stock_mutation"},
        {"stock_mutation.show",   "View Stock Mutation Detail", "stock_mutation"},

        {"log.index",  "View All Logs",  "log"},
        {"log.show",   "View Log Detail", "log"},

        {"dashboard.index", "View Dashboard", "dashboard"},
        {"pos.index",       "Access POS",       "pos"},
        {"report.index",    "View Reports",    "report"},

        {"transfer_request.index", "View All Transfer Requests", "transfer_request"},
        {"transfer_request.show",  "View Transfer Request Detail", "transfer_request"},
        {"transfer_request.store", "Create Transfer Request", "transfer_request"},
        {"transfer_request.update", "Update Transfer Request Status", "transfer_request"},
        {"transfer_request.delete", "Delete Transfer Request", "transfer_request"},

        {"stock_opname.index", "View All Stock Opname", "stock_opname"},
        {"stock_opname.show",  "View Stock Opname Detail", "stock_opname"},
        {"stock_opname.store", "Create Stock Opname", "stock_opname"},
        {"stock_opname.update", "Update Stock Opname", "stock_opname"},
        {"stock_opname.delete", "Delete Stock Opname", "stock_opname"},

        {"purchase_order.index", "View All Purchase Orders", "purchase_order"},
        {"purchase_order.show",  "View Purchase Order Detail", "purchase_order"},
        {"purchase_order.store", "Create Purchase Order", "purchase_order"},
        {"purchase_order.update", "Update Purchase Order", "purchase_order"},
        {"purchase_order.delete", "Delete Purchase Order", "purchase_order"},
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
        Role savedAdmin = roleRepository.save(adminRole);
        log.info("[SEEDER] Admin role '{}' now has {} permissions", savedAdmin.getSlug(), savedAdmin.getPermissions().size());

        // 4. Update roles with specific module access
        Role adminPartnersRole = roleRepository.findBySlug("admin-partners").orElseGet(() -> {
            Role r = new Role();
            r.setSlug("admin-partners");
            r.setName("Admin Partners");
            return r;
        });
        adminPartnersRole.getPermissions().clear();

        Role employeePartnersRole = roleRepository.findBySlug("employee-partners").orElseGet(() -> {
            Role r = new Role();
            r.setSlug("employee-partners");
            r.setName("Employee Partners");
            return r;
        });
        employeePartnersRole.getPermissions().clear();

        Set<Permission> adminPartnersPerms = new HashSet<>();
        Set<Permission> employeePartnersPerms = new HashSet<>();

        for (Permission p : allPerms) {
            String moduleSlug = p.getModule().getSlug();
            
            // Modules allowed for Admin Partner
            if (moduleSlug.equals("user") || 
                moduleSlug.equals("category") ||
                moduleSlug.equals("produk") ||
                moduleSlug.equals("warehouse") ||
                moduleSlug.equals("branch") ||
                moduleSlug.equals("product_photo") ||
                moduleSlug.equals("stock_balance") ||
                moduleSlug.equals("stock_mutation") ||
                moduleSlug.equals("transfer_request") ||
                moduleSlug.equals("stock_opname") ||
                moduleSlug.equals("purchase_order") ||
                moduleSlug.equals("dashboard") ||
                moduleSlug.equals("pos") ||
                moduleSlug.equals("report")) {
                adminPartnersPerms.add(p);
            }

            // Modules allowed for Employee
            if (moduleSlug.equals("category") ||
                moduleSlug.equals("produk") ||
                moduleSlug.equals("warehouse") ||
                moduleSlug.equals("branch") ||
                moduleSlug.equals("product_photo") ||
                moduleSlug.equals("stock_balance") ||
                moduleSlug.equals("stock_mutation") ||
                moduleSlug.equals("transfer_request") ||
                moduleSlug.equals("stock_opname") ||
                moduleSlug.equals("purchase_order") ||
                moduleSlug.equals("dashboard") ||
                moduleSlug.equals("pos") ||
                moduleSlug.equals("report")) {
                employeePartnersPerms.add(p);
            }
        }
        
        adminPartnersRole.setPermissions(adminPartnersPerms);
        roleRepository.save(adminPartnersRole);

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
