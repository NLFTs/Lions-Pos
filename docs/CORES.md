# CORES.md — Spravel Starter Kit: Core Architecture

> Dokumen ini menjelaskan keputusan arsitektur inti, alasan di baliknya,
> dan panduan untuk developer yang akan melanjutkan atau mengembangkan starter kit ini.

---

## Stack yang Dipakai

| Layer | Teknologi | Alasan |
|-------|-----------|--------|
| Backend | Spring Boot 3.x (Java 21) | Mature, production-ready, ekosistem luas |
| Database | PostgreSQL | Reliable, support JSON, full SQL |
| ORM | JPA + Hibernate | Standard Java ORM, mendukung audit field |
| Auth | JWT (jjwt 0.12.x) | Stateless, scalable, permission bisa di-embed |
| Frontend | Vue 3 + Vite | Lihat bagian "Nuxt vs Vue" di bawah |
| State | Pinia | Official Vue store, ringan, TypeScript-friendly |
| HTTP Client | Axios | Mature, mudah intercept request/response |
| CSS | Tailwind CSS 3.x | Utility-first, cepat prototyping |
| UI Primitives | Radix Vue | Headless, accessible, composable |

---

## Keputusan: Nuxt vs Vue (SPA biasa)

### Pilih **Vue SPA** (current) jika:
- Frontend dan backend adalah dua project yang benar-benar terpisah (misalnya beda server/repo)
- Tidak butuh SEO (admin panel, internal tools)
- Tim lebih familiar dengan setup manual
- Deployment sederhana: satu JAR embed semua

### Pilih **Nuxt** jika:
- Mau DX layaknya Laravel (file-based routing, auto-import, middleware, layouts)
- Butuh SSR/SSG untuk SEO
- Mau Nuxt server routes sebagai BFF (Backend For Frontend) di depan Spring Boot
- Ingin patterns yang lebih opinionated dan konsisten

### Rekomendasi untuk Starter Kit ini

> **Tetap pakai Vue SPA** selama backend-nya Java.
> Alasannya: Nuxt paling powerful ketika server-nya juga JavaScript (Node.js).
> Dengan Spring Boot di backend, Nuxt hanya jadi SSR layer yang nambah kompleksitas
> tanpa benefit SSR yang signifikan (karena ini admin panel, bukan public website).
>
> Kalau mau full Laravel-like DX: pertimbangkan **pindah backend ke Hono/Fastify + Nuxt**
> sebagai full-stack JavaScript starter kit (tapi itu project berbeda).

---

## Core: Permission System

### Filosofi
Permission di-check tanpa query ke DB. Semua permission slug sudah ada di JWT
saat user login. Pattern ini mirip Laravel Sanctum + Spatie Permission.

### Alur Kerja

```
Login
  └─► Backend: kumpulkan permission slug dari User→Roles→Permissions
  └─► Simpan sebagai claim `perms: ["post.index", "role.store", ...]` di JWT
  └─► Frontend: simpan permission[] di Pinia store + localStorage

Request ke endpoint protected
  └─► JwtAuthFilter: extract token → set SecurityContext (username + perms sebagai authorities)
  └─► @PreAuthorize("hasAuthority('post.store')") → cek langsung dari SecurityContext
  └─► TIDAK query DB untuk permission

Saat access token expired (401)
  └─► Axios interceptor: otomatis call /auth/refresh
  └─► Refresh endpoint: reload permission dari DB (satu-satunya saat permission di-resync)
  └─► Token baru berisi permission terbaru
```

### Naming Convention Permission Slug

Format: `{modul}.{aksi}`

```
post.index      → tampilkan list post
post.show       → tampilkan detail post
post.store      → buat post baru
post.update     → edit post
post.delete     → hapus post

role.index
role.show
role.store
role.update
role.delete

category.index
...dst
```

### Cara Cek Permission

**Backend (Spring):**
```java
// Di controller
@PreAuthorize("hasAuthority('post.store')")
@PostMapping("/")
public ResponseEntity<?> store(@RequestBody PostRequest req) { ... }
```

**Frontend (Vue) — composable `usePermission`:**
```js
// src/composables/usePermission.js
import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const auth = useAuthStore()
  const can = (slug) => auth.permissions.includes(slug)
  return { can }
}

// Di komponen:
const { can } = usePermission()
if (can('post.store')) { /* tampilkan tombol tambah */ }
```

**Frontend (Vue Router guard):**
```js
// router/index.js — meta.permission
{
  path: '/dashboard/posts',
  meta: { requiresAuth: true, permission: 'post.index' }
}
```

---

## Core: JWT Strategy

| Token | Durasi | Isi | Tujuan |
|-------|--------|-----|--------|
| Access Token | 5 menit | username + `perms[]` | Auth setiap request |
| Refresh Token | 24 jam | username only | Minta access token baru |

### Kenapa Access Token Pendek (5 menit)?
Kalau permission user diubah admin, user lama masih punya token lama.
Dengan access token pendek, maksimal 5 menit user akan dapat token baru
dengan permission yang sudah diupdate.

### Token Storage
- Access token: `localStorage.access_token`
- Refresh token: `localStorage.refresh_token`
- Permission list: `localStorage.auth_permissions` (mirror dari token)
- Token record juga disimpan di DB table `tokens` untuk keperluan revoke

### Kapan Permission Di-resync dari DB?
Hanya saat token di-refresh (`POST /api/v1/auth/refresh`).
Ini intentional: tidak mau hit DB di setiap request.

---

## Core: API Response Format

Semua endpoint harus return format ini:

```json
{
  "status": "success",
  "message": "OK",
  "data": { ... }
}
```

Error format (dari GlobalExceptionHandler):
```json
{
  "status": "error",
  "message": "Validation failed",
  "data": {
    "timestamp": "...",
    "status": "422",
    "error": "Unprocessable Entity",
    "message": "Field X is required",
    "path": "/api/v1/..."
  }
}
```

Wrapper class: `ResData<T>` di `dto/response/ResData.java`
Builder utility: `ResponseBuilder` di `util/ResponseBuilder.java`

```java
return ResponseBuilder.ok(data);          // 200 + data
return ResponseBuilder.created(data);     // 201 + data
return ResponseBuilder.ok();              // 200 no data (DELETE)
return ResponseBuilder.error(status, msg, errorData);  // error
```

---

## Core: Database Schema (Key Relations)

```
users
  ├── user_roles (M2M) ──► roles
  │                          └── role_permissions (M2M) ──► permissions
  │                                                             └── module_id ──► modules
  ├── id, username, fullname, password

posts
  └── category_id ──► categories

tokens
  └── username (FK by value, not FK constraint)
  └── access_token, refresh_token, revoked, expiry_date

log_http
  └── method, url, status, duration, ip, user_agent, body, created_at
```

---

## Core: Project Structure Convention

```
src/main/java/com/dak/spravel/
├── config/          # Spring config (Security, WebMvc, Jackson)
├── controller/      # REST controller — hanya routing + response mapping
├── service/
│   ├── {Name}Service.java         # Interface
│   └── impl/{Name}ServiceImpl.java # Implementation
├── repository/      # Spring Data JPA repositories
├── model/           # JPA entities
├── dto/
│   ├── request/     # Input DTO (dari body/param request)
│   ├── response/    # Output DTO (apa yang dikirim ke client)
│   └── data/        # Internal DTO (antar service)
├── middleware/      # Filter/Interceptor (JWT, HTTP Log)
├── handler/         # Exception handler, security entry points
├── seeder/          # Database seeders (hanya dev/staging)
└── util/            # Stateless helper (JwtUtil, ResponseBuilder, dll)
```

```
frontend/src/
├── pages/           # Satu file per route/halaman
├── components/
│   ├── ui/          # Reusable UI primitives (Button, Input, dll)
│   └── {feature}/   # Komponen spesifik fitur
├── stores/          # Pinia stores
├── composables/     # Vue composables (usePermission, usePagination, dll)
├── lib/             # Utility non-Vue (api.js, formatter.js, dll)
└── router/          # Route definitions + guards
```

---

## Core: Seeder Convention

Seeder dijalankan otomatis saat app start jika `app.enable.seeder=true`.
Gunakan flag ini hanya di `dev` dan `staging`, matikan di `production`.

Seeder harus **idempotent** — aman dijalankan berkali-kali:
```java
// Contoh pattern idempotent
if (!userRepository.existsByUsername("admin")) {
    userRepository.save(adminUser);
}
```

---

## Yang Perlu Diselesaikan (Core Level)

- [ ] Composable `usePermission()` di frontend (pattern sudah ada, tinggal buat file)
- [ ] User CRUD endpoint + halaman frontend
- [ ] Sidebar menu filtering berdasarkan permission
- [ ] Swagger/OpenAPI documentation (`springdoc-openapi`)
- [ ] Environment profile terpisah (dev/staging/prod di `application-{env}.properties`)
- [ ] Rate limiting di auth endpoint (`/login`, `/refresh`)
