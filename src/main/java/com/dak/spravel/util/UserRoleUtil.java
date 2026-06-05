package com.dak.spravel.util;

import com.dak.spravel.model.auth.User;

/**
 * Utilitas pengecekan role user untuk guard transfer pengelola & penempatan staff.
 */
public final class UserRoleUtil {

    private UserRoleUtil() {}

    public static boolean hasPrivilegedRole(User user) {
        return user.getRoles().stream().anyMatch(r -> {
            String slug = r.getSlug().toLowerCase();
            return slug.equals("owner")
                    || slug.equals("admin-partner")
                    || slug.equals("admin-partners")
                    || slug.equals("admin")
                    || slug.equals("super-admin")
                    || slug.equals("superadmin");
        });
    }

    public static boolean hasManagerRole(User user) {
        return user.getRoles().stream().anyMatch(r -> {
            String slug = r.getSlug().toLowerCase();
            return slug.equals("pengelola-cabang")
                    || slug.equals("branch-manager")
                    || slug.equals("pengelola-gudang")
                    || slug.equals("warehouse-manager")
                    || slug.contains("pengelola-cabang")
                    || slug.contains("pengelola-gudang");
        });
    }

    /** User yang tidak boleh ditunjuk jadi pengelola cabang/gudang baru. */
    public static boolean isIneligibleManagerCandidate(User user) {
        return hasPrivilegedRole(user) || hasManagerRole(user);
    }
}
