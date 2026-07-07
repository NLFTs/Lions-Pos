package com.fts.twin.util;

import com.fts.twin.model.auth.User;

/**
 * Utilitas pengecekan role user untuk guard transfer pengelola & penempatan staff.
 *
 * Hierarki role dalam mitra:
 *   1. admin-partner      — Admin Mitra, akses penuh ke semua modul
 *   2. pengelola-cabang   — Pengelola utama cabang (bisa POS + kelola stok)
 *   3. pengelola-gudang   — Pengelola utama gudang (operasional stok, tidak ada POS)
 *   4. karyawan-cabang    — Karyawan di cabang, bisa POS & kasir, read-only stok
 *   5. karyawan-gudang    — Karyawan di gudang, bantu operasional gudang, tidak ada POS
 */
public final class UserRoleUtil {

    private UserRoleUtil() {}

    /** Role tingkat admin/owner — tidak bisa ditunjuk sebagai pengelola atau karyawan. */
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

    /** Apakah user sudah menjadi pengelola cabang atau gudang. */
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

    /** Apakah user punya role karyawan cabang. */
    public static boolean hasBranchStaffRole(User user) {
        return user.getRoles().stream().anyMatch(r -> {
            String slug = r.getSlug().toLowerCase();
            return slug.equals("karyawan-cabang") || slug.equals("staff-branch");
        });
    }

    /** Apakah user punya role karyawan gudang. */
    public static boolean hasWarehouseStaffRole(User user) {
        return user.getRoles().stream().anyMatch(r -> {
            String slug = r.getSlug().toLowerCase();
            return slug.equals("karyawan-gudang") || slug.equals("staff-warehouse");
        });
    }

    /** Apakah user sudah punya role kasir. */
    public static boolean hasKasirRole(User user) {
        return user.getRoles().stream().anyMatch(r ->
            r.getSlug().toLowerCase().equals("kasir")
        );
    }

    /**
     * User yang tidak boleh ditunjuk jadi pengelola cabang/gudang baru.
     * Admin/owner tidak bisa jadi pengelola. Pengelola yang sudah aktif juga tidak bisa.
     */
    public static boolean isIneligibleManagerCandidate(User user) {
        return hasPrivilegedRole(user) || hasManagerRole(user);
    }

    /**
     * User yang tidak bisa ditambahkan sebagai karyawan biasa.
     * Admin/owner tidak bisa jadi karyawan cabang/gudang.
     */
    public static boolean isIneligibleStaffCandidate(User user) {
        return hasPrivilegedRole(user);
    }
}
