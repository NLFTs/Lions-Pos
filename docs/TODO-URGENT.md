# TODO-URGENT.md — Daftar Pekerjaan yang Belum Selesai

> Dokumen ini adalah konsolidasi semua item `⏳ TODO` dari seluruh docs.
> Update status di sini setiap kali item selesai dikerjakan,
> lalu update juga file docs sumber yang bersangkutan.

---

## Prioritas 1 — Blocking (starter kit belum bisa dibilang lengkap tanpa ini)

### 1.1 Composable `usePermission.js`

**Mengapa urgent**: Semua frontend page harusnya cek permission via `can(slug)`.
Tanpa ini, tombol dan menu tidak bisa dikontrol berdasarkan hak akses.

**File yang harus dibuat**: `frontend/src/composables/usePermission.js`

```javascript
import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const auth = useAuthStore()
  const can  = (slug) => auth.permissions.includes(slug)
  return { can }
}
```

**Docs terkait**: `05-vue-thymeleaf.md`, `07-token-system.md`

Status: `✅ SELESAI`

---

### 1.2 Sidebar Menu Filtering by Permission

**Mengapa urgent**: Saat ini sidebar menampilkan semua menu meskipun user tidak punya akses.
Ini inkonsisten — route guard sudah block, tapi menu tetap muncul.

**File yang harus diubah**: `frontend/src/components/AppLayout.vue`

Setiap sidebar item harus dibungkus `v-if="can('resource.index')"`:

```vue
<script setup>
import { usePermission } from '@/composables/usePermission'
const { can } = usePermission()
</script>

<!-- Contoh: -->
<SidebarItem v-if="can('post.index')"       to="/dashboard/posts"       label="Posts" />
<SidebarItem v-if="can('category.index')"   to="/dashboard/categories"  label="Categories" />
<SidebarItem v-if="can('role.index')"       to="/dashboard/roles"       label="Roles" />
<SidebarItem v-if="can('permission.index')" to="/dashboard/permissions" label="Permissions" />
<SidebarItem v-if="can('module.index')"     to="/dashboard/modules"     label="Modules" />
<SidebarItem v-if="can('user.index')"       to="/dashboard/users"       label="Users" />
```

**Dependensi**: Selesaikan item 1.1 dulu.

**Docs terkait**: `05-vue-thymeleaf.md`, `SKILLS.md`

Status: `✅ SELESAI`

---

### 1.3 User CRUD — Backend

**Mengapa urgent**: User management adalah fitur inti admin panel. Tanpa ini, user baru
hanya bisa ditambahkan via seeder — tidak ada UI.

**File yang harus dibuat**:

| File | Path |
|------|------|
| Service interface | `src/main/java/com/dak/spravel/service/UserService.java` |
| Service impl | `src/main/java/com/dak/spravel/service/impl/UserServiceImpl.java` |
| Request DTO | `src/main/java/com/dak/spravel/dto/request/UserRequest.java` |
| Update DTO | `src/main/java/com/dak/spravel/dto/request/UpdateUserRequest.java` |
| Response DTO | `src/main/java/com/dak/spravel/dto/response/UserResponse.java` |
| Controller | `src/main/java/com/dak/spravel/controller/UserController.java` |

**Endpoints yang harus ada**:

| Method | Path | Permission |
|--------|------|------------|
| GET | `/api/v1/users?page=0&size=10` | `user.index` |
| GET | `/api/v1/users/{id}` | `user.show` |
| POST | `/api/v1/users` | `user.store` |
| PUT | `/api/v1/users/{id}` | `user.update` |
| DELETE | `/api/v1/users/{id}` | `user.delete` |

**Permission slugs yang harus ditambahkan ke seeder**:
`user.index`, `user.show`, `user.store`, `user.update`, `user.delete`

**Ikuti konvensi lengkap di**: `06-crud-convention.md`

**Docs terkait**: `06-crud-convention.md`, `03-structure.md`, `SKILLS.md`

Status: `✅ SELESAI`

---

### 1.4 User CRUD — Frontend

**Mengapa urgent**: Backend user CRUD tidak berguna tanpa UI.

**File yang harus dibuat**: `frontend/src/pages/UsersPage.vue`

**Route yang harus ditambahkan** di `frontend/src/router/index.js`:

```javascript
{
  path: '/dashboard/users',
  name: 'users',
  component: () => import('@/pages/UsersPage.vue'),
  meta: {
    requiresAuth: true,
    permission: 'user.index',
    pageTitle: 'Manajemen User',
    pageSubtitle: 'Kelola data user dan assign role.',
  }
}
```

**Dependensi**: Selesaikan item 1.1 dan 1.3 dulu.

**Docs terkait**: `05-vue-thymeleaf.md`, `SKILLS.md`

Status: `✅ SELESAI`

---

## Prioritas 2 — Penting (UX & DX)

### 2.1 Toast Notification Global

**Mengapa penting**: Saat ini tidak ada feedback visual setelah aksi (create, update, delete berhasil/gagal).

**Pendekatan yang disarankan**:
- Buat Pinia store `stores/toast.js` atau gunakan composable `useToast()`
- Render toast di `AppLayout.vue` atau `App.vue`
- Expose: `toast.success(msg)`, `toast.error(msg)`, `toast.info(msg)`

Status: `✅ SELESAI`

---

### 2.2 Confirm Dialog Reusable

**Mengapa penting**: Saat ini delete pakai `confirm()` bawaan browser — tidak konsisten dengan UI.

**Pendekatan yang disarankan**:
- Gunakan Radix Vue `AlertDialog` yang sudah ada di `components/ui/`
- Buat composable `useConfirm()` yang return Promise

```javascript
// Contoh API yang diinginkan
const { confirm } = useConfirm()
const ok = await confirm({ title: 'Hapus Post?', description: 'Aksi ini tidak bisa dibatalkan.' })
if (ok) await api.delete(`/api/v1/posts/${id}`)
```

Status: `✅ SELESAI`

---

### 2.3 Profile Page & Ganti Password

**Mengapa penting**: User harus bisa ganti password sendiri tanpa perlu admin.

**File yang harus dibuat**: `frontend/src/pages/ProfilePage.vue`

**Endpoint backend yang dibutuhkan**:
- `GET /api/v1/auth/me` → sudah ada ✅
- `PUT /api/v1/users/me/password` → sudah ada ✅

**Route**:

```javascript
{
  path: '/dashboard/profile',
  name: 'profile',
  component: () => import('@/pages/ProfilePage.vue'),
  meta: { requiresAuth: true }
  // Tidak butuh permission khusus — semua user bisa akses
}
```

Status: `✅ SELESAI`

---

## Prioritas 3 — Nice to Have

### 3.1 Swagger / OpenAPI Documentation

**File yang harus diubah**: `pom.xml` — tambah dependency:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

Setelah ditambahkan, Swagger UI tersedia di: `http://localhost:8090/swagger-ui.html`

Status: `✅ SELESAI`

---

### 3.2 Audit Log UI

**Mengapa**: Tabel `log_http` sudah diisi oleh `HttpLogFilter`, tapi belum ada halaman untuk melihatnya.

**File yang harus dibuat**: `frontend/src/pages/LogsPage.vue`

**Endpoint yang dibutuhkan**: `GET /api/v1/logs` ✅

Status: `✅ SELESAI`

---

### 3.3 Unit Test Backend

**File yang harus dibuat**:

| Test | Path |
|------|------|
| `JwtUtilTest` | `src/test/java/com/dak/spravel/util/JwtUtilTest.java` |
| `AuthControllerTest` | `src/test/java/com/dak/spravel/controller/AuthControllerTest.java` |
| `PostServiceImplTest` | `src/test/java/com/dak/spravel/service/impl/PostServiceImplTest.java` |

Status: `✅ SELESAI`

---

## Prioritas 4 — Production Readiness

### 4.1 Environment Config Terpisah

**File yang harus dibuat**:
- `src/main/resources/application-dev.properties` ✅
- `src/main/resources/application-prod.properties` ✅

**Yang HARUS dipindahkan ke env variable di production**:
- `app.jwt.secret` → `${APP_JWT_SECRET}` ✅
- `spring.datasource.password` → `${SPRING_DATASOURCE_PASSWORD}` ✅
- `spring.datasource.url` → `${SPRING_DATASOURCE_URL}` ✅

Status: `✅ SELESAI`

---

### 4.2 Rate Limiting Auth Endpoint

**Mengapa**: Endpoint login dan refresh rentan terhadap brute force.

**Pendekatan**: Gunakan `bucket4j` atau Spring `@RateLimiter` untuk:
- `POST /api/v1/auth/login` → maks 5 request/menit per IP
- `POST /api/v1/auth/refresh` → maks 10 request/menit per IP

Status: `⏳ BELUM DIIMPLEMENTASIKAN`

---

### 4.3 README.md Utama Project

**File yang harus diupdate**: `README.md` (di root project, bukan di `docs/`)

Harus berisi minimal:
- Deskripsi project
- Tech stack
- Cara setup lokal (clone → konfigurasi DB → run)
- Cara build production
- Link ke `docs/`

Status: `✅ SELESAI`

---

## Ringkasan Status

| # | Item | Prioritas | Status |
|---|------|-----------|--------|
| 1.1 | `usePermission.js` composable | 🔴 Blocking | ✅ |
| 1.2 | Sidebar filtering by permission | 🔴 Blocking | ✅ |
| 1.3 | User CRUD — Backend | 🔴 Blocking | ✅ |
| 1.4 | User CRUD — Frontend | 🔴 Blocking | ✅ |
| 2.1 | Toast notification global | 🟡 Penting | ✅ |
| 2.2 | Confirm dialog reusable | 🟡 Penting | ✅ |
| 2.3 | Profile page + ganti password | 🟡 Penting | ✅ |
| 3.1 | Swagger / OpenAPI | 🟢 Nice to have | ✅ |
| 3.2 | Audit log UI | 🟢 Nice to have | ✅ |
| 3.3 | Unit test backend | 🟢 Nice to have | ✅ |
| 4.1 | Environment config terpisah | 🔵 Production | ✅ |
| 4.2 | Rate limiting auth endpoint | 🔵 Production | ⏳ |
| 4.3 | README.md utama | 🔵 Production | ✅ |

---

## Cara Update File Ini

Saat item selesai:
1. Ubah `⏳` menjadi `✅` di tabel ringkasan dan di header item
2. Update juga docs sumber yang disebutkan di field **Docs terkait**
3. Update `01-roadmap.md` phase yang sesuai
4. Update `SKILLS.md` checklist
