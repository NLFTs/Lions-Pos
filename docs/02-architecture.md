# Arsitektur Sistem

---

## 1. Layer System

Spravel menggunakan arsitektur berlapis. Setiap request melewati layer dari atas ke bawah,
response kembali dari bawah ke atas.

```
┌─────────────────────────────────────────────────────────┐
│  CLIENT (Browser / API Consumer)                        │
└─────────────────────────────────────────────────────────┘
                          │ HTTP Request
                          ▼
┌─────────────────────────────────────────────────────────┐
│  MIDDLEWARE LAYER                                        │
│  1. HttpLogFilter     → log method, path, IP, durasi    │
│  2. JwtAuthFilter     → validasi Bearer token,          │
│                          set SecurityContext             │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  SECURITY LAYER (Spring Security)                        │
│  - Cek apakah path butuh auth                           │
│  - @PreAuthorize("hasAuthority('slug')") per method     │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  PRESENTATION LAYER                                      │
│  Controller     ← menerima request, validasi @Valid      │
│  DTO (Request)  ← binding + constraint validation        │
│  DTO (Response) ← mapping output, tidak expose entity   │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  BUSINESS LAYER                                          │
│  Service (interface) + ServiceImpl                       │
│  - Semua business logic ada di sini                     │
│  - Controller TIDAK BOLEH akses Repository langsung     │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  DATA ACCESS LAYER                                       │
│  Repository (extends JpaRepository)                      │
│  - Query ke database                                     │
│  - Tidak ada business logic                             │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  DATABASE LAYER                                          │
│  PostgreSQL ← dipetakan via JPA Entity/Model            │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Alur Request API (Protected Endpoint)

```
POST /api/v1/posts  (Authorization: Bearer <token>)
│
├─[1] HttpLogFilter
│      → catat: method, path, IP, timestamp
│
├─[2] JwtAuthFilter
│      → extract token dari header Authorization
│      → jwtUtil.validateToken(token)
│      → getUsernameFromToken(token)        → "admin"
│      → getPermissionsFromToken(token)     → ["post.store", ...]
│      → set SecurityContextHolder(username, permissions)
│      → TIDAK query DB
│
├─[3] Spring Security
│      → anyRequest().authenticated() → pass (sudah set di SecurityContext)
│
├─[4] PostController.store()
│      → @PreAuthorize("hasAuthority('post.store')")
│      → cek authorities di SecurityContext → pass / throw AccessDeniedException
│      → @Valid @RequestBody PostRequest → validasi field
│      → panggil postService.create(request)
│
├─[5] PostServiceImpl.create()
│      → business logic
│      → panggil postRepository.save(entity)
│
├─[6] PostRepository
│      → INSERT ke tabel posts
│
└─[7] Response
       → ResponseBuilder.created(postResponse)
       → {"status":"success","message":"Created successfully","data":{...}}
```

---

## 3. Alur Login & Token

```
POST /api/v1/auth/login  {username, password}
│
├─[1] Cari user by username → UserRepository
├─[2] BCrypt.matches(inputPassword, storedHash)
├─[3] Kumpulkan permissions:
│      user.getRoles()
│        .flatMap(role → role.getPermissions())
│        .map(perm → perm.getSlug())
│        .distinct().sorted()
│      hasil: ["category.index", "post.delete", "post.index", ...]
│
├─[4] Revoke semua token lama user di DB
├─[5] JWT Access Token = generateAccessToken(username, permissions)
│      payload: { sub: "admin", perms: [...], iat: ..., exp: now+5min }
│
├─[6] JWT Refresh Token = generateRefreshToken(username)
│      payload: { sub: "admin", iat: ..., exp: now+24h }
│
├─[7] Simpan record ke tabel tokens:
│      { access_token, refresh_token, username, token_type:"Bearer",
│        revoked:false, expiry_date: now+24h }
│
└─[8] Return: { accessToken, refreshToken }
```

---

## 4. Alur Refresh Token

```
POST /api/v1/auth/refresh?refreshToken=<rt>
│
├─[1] Cari token record by refreshToken di tabel tokens
├─[2] Validasi: revoked == false && expiryDate > now
├─[3] Load ulang user dari DB → ambil permissions TERBARU
│      (ini satu-satunya titik di mana permissions di-resync dari DB)
├─[4] Generate access token baru dengan permissions terbaru
├─[5] Update record: access_token = newAccessToken (refresh_token tetap sama)
└─[6] Return: { newAccessToken, refreshToken (sama) }
```

---

## 5. Alur Frontend (Vue SPA)

```
Browser buka http://localhost:5173 (dev) atau http://localhost:8090 (prod)
│
├─ Vue Router: cek route yang diminta
│   ├─ meta.requiresAuth=true + auth.isAuthenticated=false → redirect /login
│   ├─ meta.guest=true + sudah login → redirect /dashboard
│   └─ meta.permission='post.index' + tidak ada di auth.permissions → redirect /dashboard
│
├─ Halaman di-render oleh Vue component
│
└─ Setiap call API:
    ├─ api.js interceptor: inject Authorization: Bearer <accessToken>
    ├─ Response 200 → data dikembalikan ke component
    └─ Response 401 → auto refresh token
         ├─ POST /api/v1/auth/refresh
         ├─ Sukses: update token di localStorage + Pinia, retry request asal
         └─ Gagal: logout() → redirect /login
```

---

## 6. Perbandingan dengan Laravel

### Request Lifecycle

| Tahap | Laravel | Spravel |
|-------|---------|---------|
| Entry point | `public/index.php` | Tomcat embedded (Spring Boot) |
| Global middleware | `app/Http/Kernel.php` | `HttpLogFilter` |
| Auth middleware | `auth` middleware | `JwtAuthFilter` |
| Router | `routes/api.php` | `@RequestMapping` annotations |
| Request validation | `FormRequest` / `$request->validate()` | `@Valid` + DTO |
| Controller | `app/Http/Controllers` | `controller/` |
| Business logic | Service class | `service/impl/` |
| Database | Eloquent ORM | Spring Data JPA |
| Response | `response()->json()` | `ResponseBuilder.ok(data)` |
| Error handling | `app/Exceptions/Handler.php` | `GlobalExceptionHandler` |

### Auth & Permission

| Konsep | Laravel (Sanctum + Spatie) | Spravel |
|--------|--------------------------|---------|
| Login | `Auth::attempt()` | Manual BCrypt + JWT |
| Token | Sanctum token (opaque) | JWT (self-contained) |
| Permission storage | DB table tiap request | Di-embed dalam JWT |
| Check permission | `$user->can('slug')` | `@PreAuthorize("hasAuthority('slug')")` |
| Frontend check | Props/inertia shared | `auth.permissions.includes('slug')` |
| Revoke | `token()->delete()` | `token.setRevoked(true)` di DB |

---

## 7. Entitas & Relasi Database

```
modules (id, slug, name, description)
    │
    └─── permissions (id, slug, name, module_id)
              │
              └─── role_permissions (role_id, permission_id)  [M2M]
                        │
                        └─── roles (id, slug, name)
                                  │
                                  └─── user_roles (user_id, role_id)  [M2M]
                                            │
                                            └─── users (id, username, fullname, password)

posts (id, title, content, status, category_id, created_at, updated_at)
    └─── categories (id, name, description, created_at, updated_at)

tokens (id, access_token, refresh_token, token_type, username, revoked, expiry_date)

log_http (id, method, url, status, duration, ip, created_at, ...)
```

---

## 8. Package Structure Backend

```
com.dak.spravel
├── config/          → Konfigurasi Spring (Security, WebMvc, Jackson)
├── controller/      → REST controller — HANYA routing + response mapping
├── service/
│   ├── {Name}Service.java         → Interface (kontrak)
│   └── impl/{Name}ServiceImpl.java → Implementasi (business logic)
├── repository/      → Spring Data JPA — HANYA query
├── model/           → JPA Entity — HANYA struktur data + ORM mapping
├── dto/
│   ├── request/     → Input DTO dengan @Valid annotations
│   ├── response/    → Output DTO (tidak expose field sensitif)
│   └── data/        → Internal DTO (ErrorData, dll)
├── middleware/      → Filter (JwtAuthFilter, HttpLogFilter)
├── handler/         → Exception handler + security entry points
├── seeder/          → Data seeder (dev/staging only)
└── util/            → Stateless helper (JwtUtil, ResponseBuilder, ResponseConstant)
```
