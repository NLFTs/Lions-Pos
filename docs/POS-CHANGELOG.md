# POS Changelog

Catatan semua perubahan dan fitur yang dikerjakan pada modul Point of Sale (POS) dan sistem order.

---

## Shift Kasir

### Backend
- Buat model `CashierShift` dengan field: `partner`, `branch`, `cashier`, `startedAt`, `endedAt`, `startingCash`, `totalRevenue`, `totalTransactions`, `status` (OPEN/CLOSED), `notes`, `closingNotes`
- Tambah field breakdown per metode pembayaran: `cashRevenue`, `transferRevenue`, `cashTransactions`, `transferTransactions`
- Buat `CashierShiftRepository` dengan query:
  - `findByCashierIdAndBranchIdAndStatus` — cek shift aktif
  - `calculateRevenue` — hitung omzet PAID dalam rentang waktu shift
  - `countTransactions` — hitung jumlah transaksi
  - `calculateRevenueByMethod` — omzet per metode (CASH/TRANSFER)
  - `countTransactionsByMethod` — jumlah transaksi per metode
- Buat `CashierShiftService` dengan method: `openShift`, `closeShift`, `getActiveShift`, `findById`, `findAll`
  - `closeShift` menghitung omzet dan breakdown tunai vs transfer secara otomatis
  - Guard multi-tenant dan branch isolation
- Buat `CashierShiftController` dengan endpoint:
  - `GET /api/v1/shifts` — daftar shift sesuai scope user
  - `GET /api/v1/shifts/{id}` — detail shift
  - `GET /api/v1/shifts/active?branchId={id}` — cek shift aktif kasir
  - `POST /api/v1/shifts/open` — buka shift
  - `PATCH /api/v1/shifts/{id}/close` — tutup shift dan hitung omzet
- Tambah permission `shift.open`, `shift.close`, `shift.index` ke `PermissionSeeder`
- Tambah module `shift` ke seeder

### Frontend
- Integrasi `ShiftModal` ke `KasirPage`:
  - Cek shift aktif otomatis saat branch berubah
  - Badge kasir aktif di header POS
  - Tombol "Buka Shift" dan "Tutup Shift" di header
- Setelah tutup shift: modal tidak langsung tertutup, tampilkan ringkasan omzet aktual dengan breakdown tunai vs transfer
- Tombol "Cetak Ringkasan" di modal penutupan — print struk 80mm ringkasan shift
- Buat `ShiftsPage.vue` — halaman riwayat shift dengan:
  - 4 summary card: total omzet, total transaksi, shift selesai, shift aktif
  - Tabel rekapitulasi omzet per kasir
  - Tabel riwayat shift lengkap (kasir, cabang, waktu, durasi, modal awal, omzet, transaksi, status)
  - Filter status dan pencarian
  - Tampilan card untuk mobile
- Tambah route `/dashboard/shifts` ke router
- Tambah menu "Riwayat Shift" di sidebar grup Transaksi

---

## Stok — Fix Transfer Payment

### Backend
- `OrdersService.create()`: stok dipotong segera saat order dibuat, terlepas dari metode pembayaran
  - Sebelumnya: stok hanya dipotong jika semua pembayaran tunai (allCashVerified)
  - Sekarang: stok selalu dipotong, flag `hasTransfer` hanya menentukan status order (DRAFT/PAID)
- `PaymentsService.verifyPayment()`: dihapus potongan stok dari sini karena sudah dipotong saat create
  - Sekarang hanya update status order ke PAID
  - Hapus inject `StockBalanceService` dan `StockMutationService` yang tidak lagi dipakai

---

## Filter Tanggal di Riwayat Order

### Frontend
- Tambah filter tanggal di `OrdersPage.vue` dengan 5 pilihan: Hari Ini (default), Kemarin, 7 Hari, 30 Hari, Custom
- Mode Custom menampilkan dua date input (dari–sampai) dengan animasi
- Counter "X transaksi ditemukan" di kanan filter bar

---

## Cetak Struk

### Frontend
- Refactor `StrukPrint.vue` menjadi komponen headless (tidak render UI)
- `doPrint()` membuka popup window 80mm, bukan `window.print()` yang cetak seluruh halaman
- Dukungan split payment di struk: tampilkan baris tunai dan transfer secara terpisah
- Dukungan `itemNote`: ditampilkan sebagai teks italic di bawah nama produk
- Expose `doPrint()` via `defineExpose` agar bisa dipanggil dari parent via ref
- `printReceipt()` di `KasirPage` sekarang cukup memanggil `struk.value?.doPrint()`
- Tambah `ref="struk"` ke komponen StrukPrint di template

---

## Custom Note Per Item

### Backend
- Tambah field `itemNote` (TEXT) ke model `OrderItems`
- Tambah field `itemNote` ke `OrderItemRequest`
- Tambah field `itemNote` ke `OrderItemResponse`
- `OrdersService.create()` set `item.setItemNote()` saat mapping item
- `OrdersService.mapToResponse()` include `itemNote` di builder

### Frontend
- Cart item diinisialisasi dengan `itemNote: ''`
- Tombol biru (ikon MessageSquare) per item di cart untuk toggle textarea catatan
- Textarea muncul saat toggle dengan placeholder contoh penggunaan F&B
- Tanda konfirmasi "Catatan akan tercetak di struk" jika note terisi
- `checkout()` mengirim `itemNote` dalam payload items

---

## Produk Favorit

### Frontend
- Fungsi `fetchTopProducts()` — agregasi frekuensi penjualan dari semua order PAID, ambil top 8
- State `topProducts`, `loadingTop`, `showFavorites`
- Tab "Favorit" di sebelah kiri filter kategori dengan ikon bintang
- Grid produk terlaris dengan badge ranking (emas, silver, bronze)
- Klik produk favorit langsung menambah ke cart
- `fetchTopProducts()` dipanggil saat `onMounted`

---

## Sales Target Tracker

### Frontend
- State `salesTarget`, `targetProgress` computed, `currentMonthRevenue`
- Target disimpan di `localStorage` (persisten antar sesi)
- Fungsi `fetchMonthRevenue()` menghitung omzet bulan berjalan dari order PAID
- Widget di atas filter kategori:
  - Jika target belum diset: tombol "Set Target Omzet Bulan Ini"
  - Jika target sudah diset: progress bar adaptif (merah/amber/biru/hijau berdasarkan persentase)
  - Tampilkan omzet saat ini vs target dengan persentase
  - Pesan khusus jika target tercapai (100%)
- Tombol "Ubah" untuk mengganti target kapan saja
- `fetchMonthRevenue()` dipanggil saat `onMounted`

---

## QR Receipt — Struk Digital Publik (Opsi A)

### Backend
- Buat `PublicReceiptResponse.java` di `dto/response/order/` dengan field: orderNumber, branchName, cashierName, buyerName, createdAt, subtotal, discountAmount, total, status, paymentMethod, paymentStatus, items
- Inner class `ItemLine`: productName, qty, unitPrice, subtotal, itemNote
- Buat `PublicReceiptController.java`:
  - `GET /api/v1/public/receipt/{orderNumber}` — tidak butuh JWT
  - Mapper `toResponse()` menggunakan builder pattern
  - Return 404 dengan pesan jelas jika order tidak ditemukan
- Tambah `/api/v1/public/**` ke whitelist `SecurityConfig` (permitAll)

### Frontend
- Update QR di `StrukPrint.vue`: encode URL `{origin}/receipt/{orderNumber}` (bukan URL internal dashboard)
- Buat `ReceiptPage.vue` — halaman publik tanpa login:
  - Fetch data dari `GET /api/v1/public/receipt/{orderNumber}`
  - Tampilkan status order dengan warna dan label yang tepat
  - Detail item dengan itemNote
  - Info pembayaran dan status verifikasi
  - Desain mobile-first dengan card receipt bergaya
  - State loading, error (404), dan sukses
- Tambah route `/receipt/:orderNumber` ke router tanpa `requiresAuth`

---

## Ringkasan File yang Dibuat atau Diubah

### File Baru
| File | Keterangan |
|------|------------|
| `model/order/CashierShift.java` | Entity shift kasir |
| `repository/order/CashierShiftRepository.java` | Query omzet dan breakdown per shift |
| `service/order/CashierShiftService.java` | Logic buka/tutup shift |
| `controller/order/CashierShiftController.java` | Endpoint shift |
| `dto/request/order/ShiftRequest.java` | Request buka/tutup shift |
| `dto/response/order/ShiftResponse.java` | Response shift dengan breakdown |
| `dto/response/order/PublicReceiptResponse.java` | DTO struk publik |
| `controller/order/PublicReceiptController.java` | Endpoint struk publik |
| `frontend/src/pages/ShiftsPage.vue` | Halaman riwayat shift |
| `frontend/src/pages/ReceiptPage.vue` | Halaman struk publik pelanggan |

### File Diubah
| File | Perubahan |
|------|-----------|
| `model/order/CashierShift.java` | Tambah 4 field breakdown pembayaran |
| `model/order/OrderItems.java` | Tambah field `itemNote` |
| `service/order/OrdersService.java` | Pindah potongan stok, tambah `itemNote` di mapping |
| `service/order/PaymentsService.java` | Hapus potongan stok di verifyPayment |
| `service/order/CashierShiftService.java` | Hitung breakdown pembayaran saat close shift |
| `repository/order/CashierShiftRepository.java` | Tambah query breakdown per metode |
| `dto/request/order/OrdersRequest.java` | Tambah `itemNote` di OrderItemRequest |
| `dto/response/order/OrderItemResponse.java` | Tambah field `itemNote` |
| `dto/response/order/ShiftResponse.java` | Tambah 4 field breakdown |
| `config/SecurityConfig.java` | Whitelist `/api/v1/public/**` |
| `seeder/PermissionSeeder.java` | Tambah module dan permission shift |
| `frontend/src/components/StrukPrint.vue` | Refactor headless, QR publik, split payment, itemNote |
| `frontend/src/components/ShiftModal.vue` | Tambah halaman ringkasan setelah close, cetak shift |
| `frontend/src/pages/KasirPage.vue` | Integrasi shift, note per item, produk favorit, sales target |
| `frontend/src/pages/OrdersPage.vue` | Tambah filter tanggal |
| `frontend/src/router/index.js` | Tambah route shifts dan receipt publik |
| `frontend/src/components/AppLayout.vue` | Tambah menu Riwayat Shift |
