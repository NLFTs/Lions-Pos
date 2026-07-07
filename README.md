[![Gaptek](/docs/amazing.png)](https://gaptek.dev)

# Gaptek — Sistem Manajemen Bisnis

<p>
  <a href="./LICENSE"><img src="https://img.shields.io/github/license/your-org/gaptek?style=flat" alt="Lisensi"></a>
  <a href="#"><img src="https://img.shields.io/badge/Java-21-orange?style=flat" alt="Java 21"></a>
  <a href="#"><img src="https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?style=flat" alt="Spring Boot 3.4.5"></a>
  <a href="#"><img src="https://img.shields.io/badge/Vue.js-3.x-42b883?style=flat" alt="Vue 3"></a>
  <a href="#"><img src="https://img.shields.io/badge/Vite-8.x-646cff?style=flat" alt="Vite"></a>
  <a href="#"><img src="https://img.shields.io/badge/PostgreSQL-ready-336791?style=flat" alt="PostgreSQL"></a>
</p>

**Gaptek** adalah platform manajemen bisnis berbasis web yang menggabungkan backend Spring Boot dengan frontend Vue.js. Sistem ini mencakup manajemen penjualan (kasir/POS), inventaris gudang, pengadaan barang, manajemen pengguna dengan kontrol akses berbasis peran, serta berbagai laporan operasional.

---

## Daftar Isi

- [Fitur Utama](#fitur-utama)
- [Tech Stack](#tech-stack)
- [Struktur Proyek](#struktur-proyek)
- [Prasyarat](#prasyarat)
- [Memulai Pengembangan](#memulai-pengembangan)
- [Konfigurasi Environment](#konfigurasi-environment)
- [Perintah Maven](#perintah-maven)
- [Build Production](#build-production)
- [Dokumentasi API](#dokumentasi-api)
- [Kontribusi](#kontribusi)
- [Lisensi](#lisensi)

---

## Fitur Utama

### Kasir & Penjualan
- Antarmuka kasir (POS) dengan manajemen shift
- Pemrosesan pesanan dan item pesanan
- Pembayaran dan cetak struk
- Voucher diskon

### Manajemen Inventaris
- Manajemen gudang dan cabang
- Saldo stok dan mutasi stok
- Stock opname (perhitungan fisik stok)
- Transfer stok antar gudang/cabang

### Pengadaan (Procurement)
- Manajemen supplier
- Purchase order dan penerimaan barang

### Katalog Produk
- Manajemen produk dan kategori
- Upload foto produk

### Otentikasi & Otorisasi
- Login berbasis JWT
- Manajemen pengguna, peran (role), dan izin (permission)
- Kontrol akses berbasis modul
- Cache permission dengan Caffeine

### Sistem
- Log HTTP request
- Notifikasi
- Seeder data awal (user, permission, role template)

---

## Tech Stack

### Backend
| Teknologi | Versi |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.5 |
| Spring Security + JWT (jjwt) | 0.12.6 |
| Spring Data JPA | (managed by Boot) |
| PostgreSQL Driver | (managed by Boot) |
| Springdoc OpenAPI (Swagger UI) | 2.8.8 |
| Caffeine Cache | (managed by Boot) |
| Lombok | 1.18.38 |
| Thymeleaf | (managed by Boot) |

### Frontend
| Teknologi | Versi |
|---|---|
| Vue.js | 3.x |
| Vite + vite-ssg | 8.x |
| Vue Router | 5.x |
| Pinia | 3.x |
| Tailwind CSS | 3.x |
| Reka UI / Radix Vue | latest |
| Axios | 1.x |
| Chart.js + vue-chartjs | 4.x |
| GSAP + Lenis | latest |
| jsPDF + jspdf-autotable | latest |
| Iconify Vue | 5.x |

### Tooling
| Alat | Keterangan |
|---|---|
| Maven Wrapper (`./mvnw`) | Build & lifecycle management backend |
| pnpm | Package manager frontend |
| VitePress | Dokumentasi frontend |

---

## Struktur Proyek

```text
Gaptek/
├── src/
│   ├── main/
│   │   ├── java/com/dak/spravel/
│   │   │   ├── config/          # Konfigurasi Spring (Security, Cache, Swagger, dll.)
│   │   │   ├── controller/      # REST API controllers (auth, catalog, inventory, order, procurement, system)
│   │   │   ├── dto/             # Data Transfer Objects (request & response)
│   │   │   ├── handler/         # Exception handler & custom auth entry point
│   │   │   ├── middleware/       # JWT filter, HTTP log filter, user context filter
│   │   │   ├── model/           # Entity JPA (auth, catalog, inventory, order, procurement, system)
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── seeder/          # Data seeder awal (user, permission, role)
│   │   │   ├── service/         # Business logic layer
│   │   │   └── util/            # Utility (JWT, response builder, security utils, dll.)
│   │   └── resources/
│   │       ├── static/          # Output build frontend (di-generate otomatis)
│   │       └── logback.xml      # Konfigurasi logging
│   └── test/
├── frontend/
│   ├── src/
│   │   ├── components/          # Komponen UI (dashboard, section, shadcn-style)
│   │   ├── composables/         # Composable reusable (confirm, toast, permission, dll.)
│   │   ├── hooks/               # Custom hooks (GSAP)
│   │   ├── lib/                 # Utilitas (api client, appMode, mock, gsap, lenis)
│   │   ├── pages/               # Halaman aplikasi (kasir, produk, inventaris, laporan, dll.)
│   │   ├── router/              # Konfigurasi Vue Router
│   │   └── stores/              # Pinia stores (auth, theme, toast, confirm)
│   ├── docs/                    # Dokumentasi frontend (VitePress)
│   ├── public/                  # Aset statis publik
│   ├── vite.config.js
│   ├── tailwind.config.js
│   └── package.json
├── api/
│   └── docs/                    # Dokumentasi OpenAPI (JSON) per modul
├── uploads/
│   ├── avatar/                  # Upload foto profil pengguna
│   └── products/                # Upload foto produk
├── pom.xml                      # Konfigurasi Maven (backend + build frontend)
├── mvnw / mvnw.cmd              # Maven Wrapper
└── spravel / spravel.bat        # Script utilitas (Windows & Unix)
```

---

## Prasyarat

Pastikan alat berikut sudah terpasang sebelum memulai:

- **Java 21** atau lebih baru
- **Maven** (atau gunakan `./mvnw` yang sudah tersedia)
- **Node.js** (direkomendasikan versi LTS terbaru)
- **pnpm** — `npm install -g pnpm`
- **PostgreSQL** — database yang sedang berjalan

---

## Memulai Pengembangan

### 1. Clone repositori

```bash
git clone https://github.com/your-org/gaptek.git
cd gaptek
```

### 2. Konfigurasi database

Buat database PostgreSQL dan sesuaikan konfigurasi di `src/main/resources/application.properties` (atau `application.yml`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gaptek
spring.datasource.username=postgres
spring.datasource.password=yourpassword
```

### 3. Jalankan backend

```bash
./mvnw spring-boot:run
```

Backend akan berjalan di:

```
http://localhost:8090
```

### 4. Jalankan frontend (mode development)

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend dev server akan berjalan di:

```
http://localhost:5173
```

> **Catatan:** Secara default frontend menggunakan `VITE_APP_MODE=mock`, sehingga bisa berjalan tanpa backend aktif. Ubah ke `api` untuk terhubung ke backend nyata.

---

## Konfigurasi Environment

Salin file contoh dan sesuaikan:

```bash
cp frontend/.env.example frontend/.env.local
```

| Variabel | Default | Keterangan |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8090` | URL backend API |
| `VITE_DEV_PORT` | `5173` | Port dev server Vite |
| `VITE_APP_TITLE` | `GAPTEK` | Judul aplikasi di browser |
| `VITE_APP_MODE` | `mock` | Mode data: `api`, `mock`, atau `empty` |

---

## Perintah Maven

Proyek ini menggunakan profil Maven kustom sebagai shortcut:

| Perintah | Keterangan |
|---|---|
| `./mvnw spring-boot:run` | Jalankan backend (development) |
| `./mvnw package` | Build JAR (termasuk build frontend) |
| `./mvnw package -P skip-frontend` | Build JAR tanpa build frontend |
| `./mvnw test` | Jalankan unit test backend |
| `./mvnw clean` | Bersihkan hasil build |

---

## Build Production

Build lengkap (frontend + backend dalam satu JAR):

```bash
./mvnw package
```

Jalankan JAR hasil build:

```bash
java -jar target/spravel-0.0.1-SNAPSHOT.jar
```

Aplikasi akan tersedia di `http://localhost:8090`.

---

## Dokumentasi API

Swagger UI tersedia saat aplikasi berjalan:

```
http://localhost:8090/swagger-ui.html
```

Dokumentasi OpenAPI per modul (format JSON) tersedia di folder `api/docs/`:

- `api/docs/auth.json`
- `api/docs/products.json`
- `api/docs/suppliers.json`
- `api/docs/warehouses.json`
- `api/docs/purchase-orders.json`

---

## Kontribusi

Kontribusi sangat disambut. Silakan baca [CONTRIBUTING.md](CONTRIBUTING.md) untuk panduan lengkapnya.

Langkah singkat:

1. Fork repositori ini
2. Buat branch fitur: `git checkout -b fitur/nama-fitur`
3. Commit perubahan: `git commit -m "feat: tambah fitur X"`
4. Push ke branch: `git push origin fitur/nama-fitur`
5. Buka Pull Request

---

## Lisensi

Dirilis di bawah lisensi [GNU AFFERO GENERAL PUBLIC](LICENSE).
