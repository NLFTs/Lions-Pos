# Token System & Permission Flow

## Aturan Utama

- **HARUS**: Permission dicek dari JWT claim — **TIDAK BOLEH** query DB per request
- **HARUS**: Setiap endpoint terproteksi pakai `@PreAuthorize("hasAuthority('slug')")`
- **HARUS**: Refresh token digunakan hanya untuk mendapat access token baru
- **TIDAK BOLEH**: Menyimpan password atau data sensitif lain di dalam JWT
- **HARUS**: Saat refresh token, permissions di-reload dari DB (satu-satunya resync)

---

## Struktur Token

### Access Token (JWT)

```
Header:  { alg: "HS256" }
Payload: {
  sub:  "admin",                                           // username
  perms: ["category.index", "post.delete", "post.index"], // permission slugs
  iat:  1713686400,                                        // issued at (unix)
  exp:  1713686700                                         // expiry (now + 5 menit)
}
Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
```

### Refresh Token (JWT)

```
Payload: {
  sub: "admin",
  iat: 1713686400,
  exp: 1713772800   // expiry (now + 24 jam)
}
// Tidak ada perms — hanya untuk identifikasi user
```

---

## Database: Tabel `tokens`

| Kolom | Tipe | Nilai | Keterangan |
|-------|------|-------|------------|
| `id` | BIGINT PK | auto | - |
| `access_token` | TEXT | JWT string | Dapat di-revoke |
| `refresh_token` | TEXT | JWT string | Untuk minta token baru |
| `token_type` | VARCHAR | `"Bearer"` | Selalu Bearer |
| `username` | VARCHAR | `"admin"` | Pemilik token |
| `revoked` | BOOLEAN | `false` | `true` = token tidak valid |
| `expiry_date` | TIMESTAMP | now+24h | Expiry refresh token |

File: `src/main/java/com/dak/spravel/model/Token.java`

---

## Konfigurasi JWT

File: `src/main/resources/application.properties`

```properties
app.jwt.secret=dGhpc19pc19hX3Zlcnlfc2VjdXJlX3NlY3JldF9mb3Jfand0IQ==
app.jwt.access-token-expiration=300000
app.jwt.refresh-token-expiration=86400000
```

**HARUS diganti di production**: `app.jwt.secret` HARUS berupa string Base64 yang kuat dan unik per environment.

---

## Alur 1: Login

**Endpoint**: `POST /api/v1/auth/login`
**Auth**: Tidak butuh token (public)

```
Input:  { "username": "admin", "password": "password" }

Proses:
  1. userRepository.findByUsername(username)
  2. BCryptPasswordEncoder.matches(inputPassword, storedHash)
     → false: throw IllegalArgumentException("Invalid credentials") → 422
     → true: lanjut
  3. Kumpulkan permissions:
     user.getRoles()
       .flatMap(role → role.getPermissions())
       .map(perm → perm.getSlug())
       .distinct()
       .sorted()
     hasil contoh: ["category.index", "post.delete", "post.index", "post.store"]
  4. Revoke semua token lama user:
     tokenRepository.findAllByUsername(username)
       .forEach(t → { t.setRevoked(true); tokenRepository.save(t); })
  5. Generate access token dengan permissions (JwtUtil)
  6. Generate refresh token
  7. Simpan ke tabel tokens
  8. Return TokenResponse

Output: {
  "status": "success",
  "message": "Success",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

File: `src/main/java/com/dak/spravel/controller/AuthController.java`

---

## Alur 2: Request API dengan Token

**Setiap request ke `/api/v1/**` (kecuali public paths)**

```
Request: GET /api/v1/posts
Header:  Authorization: Bearer eyJ...

Proses di JwtAuthFilter:
  1. Extract token dari header: split("Bearer ")[1]
  2. jwtUtil.validateToken(token)
     → invalid: filterChain.doFilter tanpa set SecurityContext
     → valid: lanjut
  3. username = jwtUtil.getUsernameFromToken(token)
  4. permissions = jwtUtil.getPermissionsFromToken(token)
     → TIDAK query DB, baca dari JWT claim "perms"
  5. Set SecurityContextHolder:
     UsernamePasswordAuthenticationToken(username, null, authorities)
     authorities = permissions.map(slug → new SimpleGrantedAuthority(slug))

Proses di Controller:
  @PreAuthorize("hasAuthority('post.index')")
  → Spring cek authorities di SecurityContext
  → ada "post.index": lanjut ke method
  → tidak ada: throw AccessDeniedException → 403

File: src/main/java/com/dak/spravel/middleware/JwtAuthFilter.java
```

---

## Alur 3: Refresh Token

**Endpoint**: `POST /api/v1/auth/refresh?refreshToken=<rt>`
**Auth**: Tidak butuh token (public)

```
Proses:
  1. tokenRepository.findByRefreshToken(refreshToken)
     → tidak ada: throw ResourceNotFoundException → 404
  2. Validasi: token.isRevoked() == true → throw IllegalArgumentException
             token.getExpiryDate().before(now) → error
  3. Load user TERBARU dari DB:
     userRepository.findByUsername(token.getUsername())
  4. Kumpulkan permissions TERBARU dari DB (sama seperti proses login langkah 3)
     → INI SATU-SATUNYA titik resync permissions dari DB
  5. Generate access token BARU dengan permissions terbaru
  6. Update record di DB: token.setAccessToken(newAccessToken)
  7. Refresh token TIDAK berubah

Output: {
  "status": "success",
  "message": "Success",
  "data": {
    "accessToken": "eyJ...baru...",
    "refreshToken": "eyJ...sama..."
  }
}
```

---

## Alur 4: Frontend Auto-Refresh

File: `frontend/src/lib/api.js`

```
Request API → 401 Unauthorized
│
├─ isRefreshing == true?
│   └─ Masukkan request ke failedQueue (array Promise)
│       → Tunggu token baru tersedia
│
└─ isRefreshing == false?
    ├─ Set isRefreshing = true
    ├─ POST /api/v1/auth/refresh?refreshToken=<localStorage.refresh_token>
    │
    ├─ SUKSES:
    │   ├─ localStorage.setItem('access_token', newToken)
    │   ├─ Decode permissions dari JWT: parseJwtPerms(newToken)
    │   │   → atob(token.split('.')[1]) → parse JSON → .perms
    │   ├─ Update Pinia store: auth.permissions = newPerms
    │   ├─ localStorage.setItem('auth_permissions', JSON.stringify(newPerms))
    │   ├─ processQueue(null, newToken) → resolve semua request yang menunggu
    │   └─ Retry request asal dengan token baru
    │
    └─ GAGAL:
        ├─ processQueue(error, null) → reject semua request yang menunggu
        └─ useAuthStore().logout() → clear localStorage → redirect /login
```

---

## Alur 5: Logout

**Endpoint**: `POST /api/v1/auth/logout?refreshToken=<rt>`

```
Proses Backend:
  1. tokenRepository.findByRefreshToken(refreshToken)
  2. token.setRevoked(true)
  3. tokenRepository.save(token)
  4. Return 200 OK (data null)

Proses Frontend (auth store):
  1. POST /api/v1/auth/logout
  2. localStorage.removeItem('access_token')
  3. localStorage.removeItem('refresh_token')
  4. localStorage.removeItem('auth_user')
  5. localStorage.removeItem('auth_permissions')
  6. Reset Pinia state: accessToken=null, refreshToken=null, user=null, permissions=[]
  7. router.push('/login')
```

---

## Permission: Backend

### `@PreAuthorize` di Controller

```java
// Format: hasAuthority('modul.aksi')
@PreAuthorize("hasAuthority('post.index')")   // GET list
@PreAuthorize("hasAuthority('post.show')")    // GET detail
@PreAuthorize("hasAuthority('post.store')")   // POST create
@PreAuthorize("hasAuthority('post.update')")  // PUT update
@PreAuthorize("hasAuthority('post.delete')")  // DELETE
```

Jika user tidak punya authority → Spring throw `AccessDeniedException`
→ `GlobalExceptionHandler.handleAccessDenied()` → return 403.

### Sumber Authority di SecurityContext

```java
// Di JwtAuthFilter — permission dibaca dari JWT, bukan DB
var authorities = jwtUtil.getPermissionsFromToken(token)  // ["post.index", ...]
        .stream()
        .map(SimpleGrantedAuthority::new)
        .toList();

// Tidak ada SELECT ke tabel permissions
```

---

## Permission: Frontend

### Pinia Store (`stores/auth.js`)

```javascript
// State permissions diisi saat login (dari /api/v1/auth/me)
// atau saat refresh (dari parseJwtPerms)
permissions: ref([])  // e.g. ["post.index", "post.store", "role.index"]
```

### Composable `usePermission` (⏳ Perlu dibuat)

File target: `frontend/src/composables/usePermission.js`

```javascript
import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const auth = useAuthStore()
  const can = (slug) => auth.permissions.includes(slug)
  return { can }
}
```

### Cara Pakai di Komponen

```vue
<script setup>
import { usePermission } from '@/composables/usePermission'
const { can } = usePermission()
</script>

<template>
  <!-- Tampilkan tombol hanya jika punya permission -->
  <button v-if="can('post.store')">Tambah Post</button>
  <button v-if="can('post.delete')" @click="hapus(id)">Hapus</button>
</template>
```

### Router Guard

File: `frontend/src/router/index.js`

```javascript
// Jika route punya meta.permission dan user tidak punya → redirect dashboard
if (to.meta.permission && !auth.permissions.includes(to.meta.permission)) {
  return next({ name: 'dashboard' })
}
```

---

## Naming Convention Permission Slug

Format wajib: `{modul}.{aksi}` — huruf kecil, pisah dengan titik.

| Modul | Slug yang Harus Ada |
|-------|---------------------|
| post | `post.index`, `post.show`, `post.store`, `post.update`, `post.delete` |
| category | `category.index`, `category.show`, `category.store`, `category.update`, `category.delete` |
| role | `role.index`, `role.show`, `role.store`, `role.update`, `role.delete` |
| permission | `permission.index`, `permission.show`, `permission.store`, `permission.update`, `permission.delete` |
| module | `module.index`, `module.show`, `module.store`, `module.update`, `module.delete` |
| user | `user.index`, `user.show`, `user.store`, `user.update`, `user.delete` ⏳ |

---

## Path yang Tidak Butuh Token (Public)

File: `src/main/java/com/dak/spravel/config/SecurityConfig.java`

```java
String[] publicApiPaths = {
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/auth/logout",
    "/api/v1/auth/force-logout-all",
};

String[] webPaths = {
    "/", "/login", "/dashboard", "/dashboard/**",
    "/assets/**", "/css/**", "/js/**", "/favicon.ico", "/error"
};
```

Semua path lain yang cocok `/api/v1/**` HARUS authenticated.

---

## Kapan Permission Diperbarui?

| Event | Permission Diperbarui? | Keterangan |
|-------|----------------------|------------|
| Setiap API request | ❌ | Baca dari JWT, tidak query DB |
| Login | ✅ | Token baru dengan permission saat itu |
| Refresh token | ✅ | Satu-satunya resync dari DB |
| Admin ubah role user | ❌ (delayed) | Berlaku setelah token user kadaluarsa dan direfresh |
| Logout + login ulang | ✅ | Token baru |

**Implikasi desain**: Jika admin cabut permission user, user masih bisa akses maksimal 5 menit
(durasi access token) sebelum refresh mengambil permission terbaru. Ini trade-off yang disengaja
antara performa (tidak query DB) vs konsistensi real-time.
