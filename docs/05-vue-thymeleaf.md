# Vue 3 + Vite SPA Integration

> Nama file ini dipertahankan untuk kompatibilitas, tapi kontennya telah diperbarui.
> Spravel menggunakan **Vue 3 + Vite SPA** — bukan Thymeleaf + CDN.

## Aturan Utama

- **WAJIB**: Frontend **HARUS** menggunakan `pnpm` sebagai package manager
  - Alasan: lebih cepat, lebih efisien disk usage, strict dependency resolution
  - File lock: `pnpm-lock.yaml` (wajib commit ke git)
  - Package manager field: `"packageManager": "pnpm@10.33.2"` di `package.json`
  - **TIDAK BOLEH** menggunakan `npm` atau `yarn` — akan menyebabkan inkonsistensi dependency
- **HARUS**: Setiap page baru daftarkan di `router/index.js` dengan `meta.requiresAuth: true`
- **HARUS**: Route yang butuh permission tambahkan `meta.permission: 'slug'`
- **HARUS**: Semua API call gunakan instance `api` dari `lib/api.js` — bukan `axios` langsung
- **TIDAK BOLEH**: Import `axios` langsung di komponen — kecuali di `auth.js` untuk login/logout yang belum punya token
- **HARUS**: Setiap page dibungkus oleh `<AppLayout>` (kecuali LoginPage)

---

## Arsitektur Frontend

```
Development
  Browser (:5173)
    ├─ Vite Dev Server
    │   └─ proxy /api/* → http://localhost:8090
    └─ HMR (Hot Module Replacement)

Production
  ./spravel full:build
    ├─ pnpm run build → src/main/resources/static/
    └─ Maven package → target/spravel-*.jar
       Browser (:8090) → Spring Boot serve index.html + assets
```

---

## Struktur File Frontend

```
frontend/
├── index.html                       ← HTML shell: <div id="app">
├── vite.config.js                   ← Build config + dev proxy
├── package.json
└── src/
    ├── main.js                      ← Entry: createApp + Pinia + Router
    ├── App.vue                      ← Root component (hanya <RouterView />)
    ├── router/
    │   └── index.js                 ← Route definitions + beforeEach guard
    ├── stores/
    │   └── auth.js                  ← Pinia store: token + user + permissions
    ├── lib/
    │   └── api.js                   ← Axios instance + request/response interceptors
    ├── composables/
    │   └── usePermission.js         ← ⏳ BELUM ADA — perlu dibuat
    ├── components/
    │   ├── AppLayout.vue            ← Sidebar + header + <slot /> konten
    │   └── ui/                      ← Radix Vue components (Button, Input, Table, dll)
    └── pages/
        ├── LoginPage.vue            ✅
        ├── DashboardPage.vue        ✅
        ├── PostsPage.vue            ✅
        ├── CategoriesPage.vue       ✅
        ├── RolesPage.vue            ✅
        ├── PermissionsPage.vue      ✅
        ├── ModulesPage.vue          ✅
        ├── UsersPage.vue            ✅
        ├── LogsPage.vue             ✅
        └── ProfilePage.vue          ✅
```

---

## Routing

File: `frontend/src/router/index.js`

### Skema Route

```javascript
const routes = [
  // Root → login
  { path: '/', redirect: '/login' },

  // Login page (public, redirect ke dashboard jika sudah login)
  {
    path: '/login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { guest: true }
  },

  // Dashboard (butuh auth, tidak butuh permission spesifik)
  {
    path: '/dashboard',
    component: () => import('@/pages/DashboardPage.vue'),
    meta: { requiresAuth: true, pageTitle: 'Dashboard' }
  },

  // Resource page (butuh auth + permission)
  {
    path: '/dashboard/posts',
    component: () => import('@/pages/PostsPage.vue'),
    meta: {
      requiresAuth: true,
      permission: 'post.index',   // ← slug yang dicek di auth.permissions
      pageTitle: 'Manajemen Post',
      pageSubtitle: 'Kelola konten post.'
    }
  },

  // Catch-all
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]
```

### Navigation Guard Logic

```javascript
router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()

  // 1. Butuh auth tapi belum login → ke /login
  if (to.meta.requiresAuth && !auth.isAuthenticated) return next('/login')

  // 2. Halaman guest tapi sudah login → ke /dashboard
  if (to.meta.guest && auth.isAuthenticated) return next('/dashboard')

  // 3. Butuh permission tapi tidak punya → ke /dashboard
  if (to.meta.permission && !auth.permissions.includes(to.meta.permission)) {
    return next('/dashboard')
  }

  next()
})
```

---

## Pinia Auth Store

File: `frontend/src/stores/auth.js`

### State

| State | Type | localStorage Key | Keterangan |
|-------|------|-----------------|------------|
| `accessToken` | `string\|null` | `access_token` | JWT access token |
| `refreshToken` | `string\|null` | `refresh_token` | JWT refresh token |
| `user` | `object\|null` | `auth_user` | `{ id, username, fullname, roles }` |
| `permissions` | `string[]` | `auth_permissions` | `["post.index", ...]` |
| `isAuthenticated` | computed | - | `!!accessToken` |

### Methods

| Method | Keterangan |
|--------|------------|
| `login(username, password)` | POST ke `/api/v1/auth/login`, simpan token, panggil `fetchMe()` |
| `logout()` | POST ke `/api/v1/auth/logout`, clear semua state + localStorage |
| `fetchMe()` | GET `/api/v1/auth/me`, update `user` + `permissions` |

### Contoh Penggunaan di Komponen

```javascript
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()

// Cek login
if (auth.isAuthenticated) { ... }

// Akses user info
console.log(auth.user.username)
console.log(auth.user.fullname)

// Cek permission (cara manual — SEBAIKNYA pakai usePermission composable)
if (auth.permissions.includes('post.store')) { ... }

// Logout
await auth.logout()
```

---

## Axios Interceptor

File: `frontend/src/lib/api.js`

### Request Interceptor

```javascript
// Inject access token ke setiap request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
```

### Response Interceptor — Auto Refresh

```javascript
// Jika dapat 401 → coba refresh → retry request asal
api.interceptors.response.use(
  response => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      // Queue pattern: jika refresh sedang berjalan, queue request ini
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const rt  = localStorage.getItem('refresh_token')
        const res = await axios.post(`/api/v1/auth/refresh?refreshToken=${rt}`)
        const newToken = res.data.data.accessToken

        // Update storage + store
        localStorage.setItem('access_token', newToken)
        const auth = useAuthStore()
        auth.accessToken  = newToken
        auth.permissions  = parseJwtPerms(newToken)   // decode dari JWT, tidak call API
        localStorage.setItem('auth_permissions', JSON.stringify(auth.permissions))

        processQueue(null, newToken)   // resolve semua request yang menunggu
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)   // retry request asal
      } catch (err) {
        processQueue(err, null)
        useAuthStore().logout()
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)
```

### Decode JWT Permissions (tanpa library)

```javascript
function parseJwtPerms(token) {
  try {
    const payload = JSON.parse(
      atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
    )
    return Array.isArray(payload.perms) ? payload.perms : []
  } catch {
    return []
  }
}
```

---

## Cara Frontend Membaca API Response

Semua endpoint mengembalikan `ResData<T>` dengan struktur:
```json
{ "status": "success", "message": "Success", "data": { ... } }
```

```javascript
// Success — data ada di res.data.data (Axion parse body → ResData wrapper)
const res = await api.get('/api/v1/posts')
const posts = res.data.data.content

// Error — catch di try/catch
try {
  await api.post('/api/v1/posts', payload)
} catch (err) {
  // Pola 3-tier fallback
  const message = err.response?.data?.data?.message   // validasi detail
    || err.response?.data?.message                     // top-level error
    || 'Gagal memproses request.'                      // generic fallback
}
```

---

## Composable `usePermission` ✅ Sudah Dibuat

File: `frontend/src/composables/usePermission.js`

```javascript
import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const auth = useAuthStore()
  const can  = (slug) => auth.permissions.includes(slug)
  return { can }
}
```

Cara pakai:

```vue
<script setup>
import { usePermission } from '@/composables/usePermission'
const { can } = usePermission()
</script>

<template>
  <button v-if="can('post.store')">Tambah</button>
</template>
```

---

## Cara Membuat Halaman Baru

### Langkah 1 — Buat file page

```
frontend/src/pages/UsersPage.vue
```

Template minimal:

```vue
<script setup>
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { usePermission } from '@/composables/usePermission'
import api from '@/lib/api'

const { can } = usePermission()
const items   = ref([])

async function fetchAll() {
  const res = await api.get('/api/v1/users?page=0&size=10')
  items.value = res.data.data.content
  // Error handling: try/catch + err.response?.data?.message
}

onMounted(fetchAll)
</script>

<template>
  <AppLayout>
    <!-- Konten halaman -->
  </AppLayout>
</template>
```

### Langkah 2 — Daftarkan route

File: `frontend/src/router/index.js`

```javascript
{
  path: '/dashboard/users',
  name: 'users',
  component: () => import('@/pages/UsersPage.vue'),
  meta: {
    requiresAuth: true,
    permission: 'user.index',
    pageTitle: 'Manajemen User',
    pageSubtitle: 'Kelola data user dan role.',
  }
}
```

### Langkah 3 — Tambah sidebar item

File: `frontend/src/components/AppLayout.vue`

Tambahkan item menu dengan `v-if="can('user.index')"` agar menu hanya muncul
jika user punya permission.

---

## Vite Config

File: `frontend/vite.config.js`

```javascript
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  build: {
    outDir: '../src/main/resources/static',   // output ke Spring Boot static
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8090'          // forward API ke Spring Boot
    }
  }
})
```

---

## Spring Boot: Serve SPA

File: `src/main/java/com/dak/spravel/controller/MainController.java`

Semua route Vue SPA ada di bawah prefix `/_/`. MainController handle forwarding ke `index.html`:

```java
@Controller
public class MainController {
    // Route Vue SPA: /_/login, /_/dashboard, /_/dashboard/**
    // Thymeleaf view tetap di root: /, /docs
    // API: /api/v1/**
}
```

---

## CLI Commands

```bash
# Install dependencies (WAJIB pakai pnpm, bukan npm!)
cd frontend
pnpm install

# Development — Vite hot reload
pnpm dev
# atau via spravel CLI:
./spravel frontend:dev

# Build frontend saja
pnpm build
# atau via spravel CLI:
./spravel frontend:build

# Build JAR penuh (dengan frontend)
./spravel full:build

# Run Spring Boot tanpa rebuild frontend
./spravel backend:dev

# Run keduanya parallel
./spravel full:dev
```

### Mengapa pnpm?

| Fitur | npm | pnpm |
|-------|-----|------|
| Installation speed | ⚡⚡ | ⚡⚡⚡⚡ |
| Disk usage | 100% | ~30-50% (hard links) |
| Strict mode | ❌ | ✅ (tidak bisa access undeclared dependency) |
| Lock file | package-lock.json | pnpm-lock.yaml |
| Node modules structure | flat (masalah phantom dep) | non-flat (strict) |

**Aturan**: Jika Anda tidak punya pnpm, install dulu:
```bash
npm install -g pnpm
```
