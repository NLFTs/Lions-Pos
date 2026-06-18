# Mulai Menggunakan

Setelah instalasi selesai, ikuti langkah-langkah berikut untuk mulai menggunakan Gaptek.

## Menjalankan Aplikasi

### Development Mode

```bash
# Backend
php artisan serve

# Frontend (di terminal terpisah)
pnpm dev
```

Aplikasi akan berjalan di `http://localhost:8000` (backend) dan `http://localhost:5173` (frontend).

## Login Pertama Kali

Gunakan akun default yang dibuat oleh seeder:

| Field    | Value               |
|----------|---------------------|
| Email    | `admin@gaptek.dev`  |
| Password | `password`          |

::: danger Penting
Segera ganti password default setelah login pertama kali di halaman **Profil > Keamanan**.
:::

## Langkah Selanjutnya

Setelah berhasil login, Anda dapat:

1. **Mengatur Cabang & Gudang** — Buat lokasi bisnis Anda di menu *Pengaturan*.
2. **Menambahkan Produk** — Mulai input inventaris di menu *Produk*.
3. **Mengaktifkan POS** — Buka aplikasi kasir di menu *Point of Sale*.

::: tip
Lihat bagian **Services** untuk memahami lebih lanjut tentang fitur-fitur utama Gaptek.
:::
