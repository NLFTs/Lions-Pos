# Spravel — Dokumentasi Sistem

> **Spravel** adalah fullstack starter kit berbasis Spring Boot (Java 21) + Vue 3 + Vite.
> Dirancang agar developer Laravel langsung familiar dengan pola kerjanya.
> Dokumentasi ini ditulis untuk dibaca oleh **programmer manusia maupun AI agent**.

---

## Cara Membaca Dokumen Ini

Setiap dokumen menggunakan kata kunci berikut dengan makna pasti:

| Kata Kunci | Makna |
|------------|-------|
| **HARUS** | Wajib dilakukan. Tidak ada pengecualian. |
| **TIDAK BOLEH** | Dilarang. Melanggar ini merusak konsistensi sistem. |
| **SEBAIKNYA** | Rekomendasi kuat, ada alasan teknis. |
| **BOLEH** | Opsional, tidak mempengaruhi sistem jika tidak dilakukan. |
| `✅ Implemented` | Sudah ada di codebase, jangan dibuat ulang. |
| `⏳ TODO` | Belum ada, perlu dibuat. |

---

## Indeks Dokumen

| File | Isi | Dibaca Saat |
|------|-----|-------------|
| `README.md` | Indeks utama + panduan baca | Selalu pertama |
| `TODO-URGENT.md` | Semua item yang belum selesai + prioritas | Mau mulai kerja / cari apa yang harus dikerjakan |
| `01-roadmap.md` | Status semua fase pengerjaan | Cek apa yang sudah/belum selesai |
| `02-architecture.md` | Alur request, layer system, perbandingan Laravel | Pahami bagaimana sistem bekerja |
| `03-structure.md` | Peta direktori backend + frontend | Cari file yang tepat |
| `04-api-standard.md` | Kontrak response API + standar Response DTO | Buat atau konsumsi endpoint |
| `05-vue-thymeleaf.md` | Integrasi Vue 3 + Vite, routing, store | Buat halaman frontend baru |
| `06-crud-convention.md` | Langkah buat resource CRUD baru | Tambah fitur/module baru |
| `07-token-system.md` | JWT auth, permission flow, token lifecycle | Modifikasi auth atau permission |
| `08-code-rules.md` | Aturan coding (comment, DI, formatting) | Tulis kode baru |
| `09-layout-standards.md` | Standar layout (sidebar, topbar) | UI/UX layout reference |
| `10-datatable.md` | Sistem DataTable shadcn-vue | CRUD table component |
| `STANDARDS.md` | Ringkasan semua standar proyek | Quick reference |
| `CORES.md` | Keputusan arsitektur dan alasannya | Evaluasi atau ubah tech stack |
| `SKILLS.md` | Daftar kemampuan starter kit + checklist | Cek fitur apa yang sudah/belum ada |

---

## Tech Stack

### Backend

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| Framework | Spring Boot | 3.4.5 |
| Language | Java | 21 |
| Database | PostgreSQL | - |
| ORM | Spring Data JPA + Hibernate | - |
| Auth | JWT (jjwt) | 0.12.6 |
| Security | Spring Security | - |
| Build | Maven | 3.9+ |

### Frontend

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| Framework | Vue | 3.5.33 |
| Build tool | Vite | 8.x |
| State | Pinia | 3.0.4 |
| HTTP | Axios | 1.13.6 |
| Router | Vue Router | 5.0.6 |
| Package Manager | pnpm | 10+ |
| UI | Radix Vue + Tailwind CSS | - |
| Icons | Lucide Vue | - |

---

## Konfigurasi Sistem

File: `src/main/resources/application.properties`

| Property | Nilai Default | Keterangan |
|----------|--------------|------------|
| `server.port` | `8090` | Port Spring Boot |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/db_spravel` | PostgreSQL |
| `spring.datasource.username` | `postgres` | DB user |
| `spring.datasource.password` | `postgres` | DB password |
| `app.jwt.secret` | Base64 string | HARUS diganti di production |
| `app.jwt.access-token-expiration` | `300000` (5 menit) | Dalam milliseconds |
| `app.jwt.refresh-token-expiration` | `86400000` (24 jam) | Dalam milliseconds |
| `app.enable.seeder` | `true` | Set `false` di production |
| `spring.jpa.hibernate.ddl-auto` | `update` | Gunakan SQL migration di production |

---

## Cara Menjalankan

### Development

```bash
# Jalankan keduanya sekaligus (Vite + Spring Boot parallel)
./spravel full:dev

# Atau terpisah:
./spravel backend:dev      # Terminal 1 — Spring Boot
./spravel frontend:dev     # Terminal 2 — Vite dev server

# Frontend: http://localhost:5173
# Backend API: http://localhost:8090
# Semua /api/* dari Vite diproxy ke :8090
```

### Production Build

```bash
# Build frontend + backend JAR
./spravel full:build

# Jalankan JAR
java -jar target/spravel-*.jar
# Akses: http://localhost:8090
```

---

## Database Default (setelah seeder)

Seeder berjalan otomatis saat startup jika `app.enable.seeder=true`.

| Resource | Default |
|----------|---------|
| User | `username: admin`, `password: password` |
| Role | `admin` (semua permission) |
| Permissions | Semua slug untuk: post, category, role, permission, module |

---

## Pola Dasar yang HARUS Diikuti

1. **Setiap endpoint API** HARUS return `ResponseEntity<ResData<T>>` via `ResponseBuilder` — lihat `04-api-standard.md`
2. **Setiap Response DTO** HARUS flat structure, pakai `@Data`, ada Javadoc, tidak expose field sensitif — lihat `04-api-standard.md`
3. **Setiap endpoint terproteksi** HARUS punya `@PreAuthorize("hasAuthority('slug')")` — lihat `07-token-system.md`
4. **Controller** TIDAK BOLEH akses `Repository` langsung — HARUS lewat `Service`
5. **Resource baru** HARUS ikuti urutan: Model → Repository → Service → Controller → DTO — lihat `06-crud-convention.md`
6. **Frontend** WAJIB pakai `pnpm` sebagai package manager — lihat `05-vue-thymeleaf.md`
7. **Frontend page baru** HARUS didaftarkan di router dengan `meta.permission` — lihat `05-vue-thymeleaf.md`
