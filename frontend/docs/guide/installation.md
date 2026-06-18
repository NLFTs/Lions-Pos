# Instalasi

Panduan ini akan membantu Anda menginstal dan mengkonfigurasi Gaptek di lingkungan lokal maupun server.

## Kebutuhan Sistem

| Kebutuhan | Versi Minimum |
|-----------|--------------|
| Node.js   | 18.x         |
| PHP       | 8.1+         |
| MySQL     | 8.0+         |
| Composer  | 2.x          |

## Clone Repository

```bash
git clone https://github.com/your-org/gaptek.git
cd gaptek
```

## Instalasi Dependensi

### Backend (Laravel)

```bash
composer install
cp .env.example .env
php artisan key:generate
```

### Frontend (Vue / Vite)

```bash
pnpm install
```

## Konfigurasi Database

Edit file `.env` dan sesuaikan koneksi database:

```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=gaptek
DB_USERNAME=root
DB_PASSWORD=your_password
```

Kemudian jalankan migrasi:

```bash
php artisan migrate --seed
```

::: warning Perhatian
Pastikan database sudah dibuat sebelum menjalankan migrasi.
:::
