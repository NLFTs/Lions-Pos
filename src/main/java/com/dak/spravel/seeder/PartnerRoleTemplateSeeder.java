package com.dak.spravel.seeder;

import com.dak.spravel.model.auth.Permission;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.PermissionRepository;
import com.dak.spravel.repository.auth.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Menyediakan 3 role template siap pakai untuk setiap partner:
 *
 *  1. admin-partner   — Admin internal partner, akses penuh ke semua modul mitranya
 *  2. pengelola-gudang — Kelola stok, PO masuk, transfer request, stock opname
 *  3. pengelola-cabang — Seperti gudang + POS lengkap + laporan + riwayat order
 *
 * Aturan: jika permission .index aktif maka .show juga harus aktif.
 *
 * Dipanggil:
 *  - Saat startup (MainSeeder) untuk semua partner yang belum punya template role
 *  - Saat partner baru dibuat (PartnerService.create)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartnerRoleTemplateSeeder {

    private final RoleRepository       roleRepository;
    private final PermissionRepository permissionRepository;

    // ─── Definisi template role ───────────────────────────────────────────────

    /**
     * Admin Partner — akses penuh ke semua modul mitranya
     * (kecuali modul system-level: role global, permission global, module, partner)
     */
    private static final String[] ADMIN_PARTNER_PERMS = {
        // User management
        "user.index", "user.show", "user.store", "user.update", "user.delete",
        // Role (per-partner)
        "role.index", "role.show",
        // Permissions
        "permission.index", "permission.show",
        // Module
        "module.index", "module.show",
        // Kategori
        "category.index", "category.show", "category.store", "category.update", "category.delete",
        // Produk
        "produk.index", "produk.show", "produk.store", "produk.update", "produk.delete",
        "product_photo.index", "product_photo.store", "product_photo.update", "product_photo.delete",
        // Lokasi
        "branch.index", "branch.show", "branch.store", "branch.update", "branch.delete",
        "warehouse.index", "warehouse.show", "warehouse.store", "warehouse.update", "warehouse.delete",
        "branch_warehouse.index", "branch_warehouse.show", "branch_warehouse.store",
        "branch_warehouse.update", "branch_warehouse.delete",
        // Stok
        "stock_balance.index", "stock_balance.show", "stock_balance.store",
        "stock_balance.update", "stock_balance.transfer",
        "stock_mutation.index", "stock_mutation.show",
        "stock_opname.index", "stock_opname.show", "stock_opname.store",
        "stock_opname.update", "stock_opname.delete",
        // Transfer
        "transfer_request.index", "transfer_request.show", "transfer_request.store",
        "transfer_request.update", "transfer_request.delete",
        // Procurement
        "supplier.index", "supplier.show", "supplier.store", "supplier.update", "supplier.delete",
        "purchase_order.index", "purchase_order.show", "purchase_order.store",
        "purchase_order.update", "purchase_order.delete",
        "purchase_receipt.index", "purchase_receipt.show", "purchase_receipt.store",
        "purchase_receipt.update", "purchase_receipt.delete",
        // Order & POS
        "order.index", "order.show", "order.store", "order.update", "order.delete",
        "order_item.index", "order_item.show", "order_item.store",
        "order_item.update", "order_item.delete",
        "pos.index",
        // Voucher
        "voucher.index", "voucher.show", "voucher.store", "voucher.update", "voucher.delete",
        // Dashboard & Laporan
        "dashboard.index",
        "report.index",
        // Shift 
        "shift.index", "shift.open", "shift.close",
        // Notifikasi
        "notification.index",
    };

    /**
     * Pengelola Cabang:
     * - Semua yang dimiliki Pengelola Gudang
     * - Tambahan: POS lengkap, riwayat order, laporan lengkap
     */
    private static final String[] PENGELOLA_CABANG_PERMS = {
        // Produk & Kategori — read-only
        "produk.index", "produk.show",
        "category.index", "category.show",
        // Stok
        "stock_balance.index", "stock_balance.show",
        "stock_balance.store", "stock_balance.update", "stock_balance.transfer",
        "stock_mutation.index", "stock_mutation.show",
        // Transfer Request — full
        "transfer_request.index", "transfer_request.show",
        "transfer_request.store", "transfer_request.update",
        // Stock Opname — bisa approve & update (agar stok masuk ke kasir)
        "stock_opname.index", "stock_opname.show", "stock_opname.store",
        "stock_opname.update",
        // Purchase Order — read-only
        "purchase_order.index", "purchase_order.show",
        // Purchase Receipt — bisa terima barang
        "purchase_receipt.index", "purchase_receipt.show", "purchase_receipt.store",
        // Supplier — read-only
        "supplier.index", "supplier.show",
        // POS & Order — FULL (tambahan dari gudang)
        "pos.index",
        "order.index", "order.show", "order.store", "order.update",
        "order_item.index", "order_item.show", "order_item.store",
        // Voucher — read-only (bisa pakai di kasir)
        "voucher.index", "voucher.show",
        // Dashboard & Laporan — FULL (tambahan dari gudang)
        "dashboard.index",
        "report.index",
        // Notifikasi
        "notification.index",
    };

    /**
     * Karyawan Cabang:
     * - Bisa akses POS & kasir (karena cabang = toko/outlet)
     * - Lihat stok cabangnya (read-only)
     * - Lihat produk & kategori (read-only)
     * - Buat order / transaksi
     * - Lihat laporan (read-only)
     * - TIDAK bisa kelola gudang, PO, supplier, transfer stok
     */
    private static final String[] KARYAWAN_CABANG_PERMS = {
        // Produk & Kategori — read-only
        "produk.index", "produk.show",
        "category.index", "category.show",
        // Stok — read-only
        "stock_balance.index", "stock_balance.show",
        "stock_mutation.index", "stock_mutation.show",
        // POS — full (karyawan cabang bisa jadi kasir)
        "pos.index",
        // Branch — read-only
        "branch.index", "branch.show",
        // Warehouse — read-only
        "warehouse.index", "warehouse.show",
        // Branch Warehouse — read-only
        "branch_warehouse.index", "branch_warehouse.show",
        // Order — bisa buat & lihat
        "order.index", "order.show", "order.store", "order.update",
        "order_item.index", "order_item.show", "order_item.store",
        // Voucher — read-only
        "voucher.index", "voucher.show",
        // Dashboard
        "dashboard.index",
        // Notifikasi
        "notification.index",
    };

    // ─── Template definitions ─────────────────────────────────────────────────

    record RoleTemplate(String slug, String name, String[] perms) {}

    public static final List<RoleTemplate> TEMPLATES = List.of(
        new RoleTemplate("owner",              "Owner / Pemilik Mitra", ADMIN_PARTNER_PERMS),
        new RoleTemplate("pengelola-cabang",   "Pengelola Cabang",      PENGELOLA_CABANG_PERMS),
        new RoleTemplate("karyawan-cabang",    "Karyawan Cabang",       KARYAWAN_CABANG_PERMS)
    );

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Pastikan 3 role template global (owner, pengelola-gudang, dll) ada di database.
     * Tidak bergantung pada partner — aman dipanggil berkali-kali (idempotent).
     */
    @Transactional
    public void ensureGlobalTemplates() {
        int created = 0;
        for (RoleTemplate template : TEMPLATES) {
            if (roleRepository.existsBySlug(template.slug())) {
                continue;
            }

            Role role = new Role();
            role.setSlug(template.slug());
            role.setName(template.name());
            role.setType(Role.Type.EXTERNAL);
            role.setCreatedAt(LocalDateTime.now());
            role.setPermissions(resolvePermissions(template.perms()));
            roleRepository.save(role);
            created++;
            log.info("[RoleTemplate] Role global '{}' dibuat dengan {} permissions.",
                template.slug(), role.getPermissions().size());
        }
        if (created > 0) {
            log.info("[RoleTemplate] ✅ {} role template global baru dibuat.", created);
        }
    }

    /**
     * Alias idempotent — dipanggil saat partner baru dibuat lewat API.
     */
    @Transactional
    public void seedForPartner(Partners partner) {
        ensureGlobalTemplates();
        log.debug("[RoleTemplate] Template roles dipastikan untuk partner '{}'.", partner.getName());
    }

    /**
     * Dipanggil saat startup oleh MainSeeder.
     */
    @Transactional
    public void run() {
        ensureGlobalTemplates();
        log.info("[RoleTemplate] ✅ Template roles global siap.");
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Set<Permission> resolvePermissions(String[] slugs) {
        Set<Permission> result = new HashSet<>();
        for (String slug : slugs) {
            permissionRepository.findBySlug(slug).ifPresentOrElse(
                result::add,
                () -> log.warn("[RoleTemplate] Permission '{}' tidak ditemukan di database, skip.", slug)
            );
        }
        return result;
    }
}
