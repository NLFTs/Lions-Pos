# Standar Proyek Spravel

Dokumen ini mencatat semua standar, konvensi, dan aturan yang berlaku untuk proyek Spravel.
Semua developer dan AI agent **HARUS** mematuhi aturan berikut.

---

## 1. Package Manager Frontend

**WAJIB menggunakan `pnpm`** untuk semua operasi frontend.

### Alasan
- ⚡ Lebih cepat dari npm/yarn
- 💾 Hemat disk ~50-70% (hard links)
- 🔒 Strict mode — tidak bisa akses undeclared dependency
- 📦 Lock file: `pnpm-lock.yaml` (wajib commit)

### Konfigurasi
File: `frontend/package.json`
```json
{
  "packageManager": "pnpm@10.33.2"
}
```

### Cara Pakai
```bash
# Install pnpm (jika belum ada)
npm install -g pnpm

# Install dependencies
cd frontend
pnpm install

# Development
pnpm dev

# Build
pnpm build
```

### Dilarang
- ❌ Menggunakan `npm install`
- ❌ Menggunakan `yarn`
- ❌ Commit `package-lock.json` atau `yarn.lock`
- ❌ Delete `pnpm-lock.yaml`

---

## 2. Response DTO Standards

### Aturan Umum
- ✅ HARUS menggunakan `@Data` dari Lombok
- ✅ HARUS memiliki Javadoc class-level
- ✅ HARUS flat structure (hindari circular reference)
- ✅ HARUS include `createdAt` dan `updatedAt` (jika entity punya auditing)
- ❌ TIDAK BOLEH expose field sensitif (password, token, dll)
- ❌ TIDAK BOLEH return entity langsung

### Konvensi Penamaan
| Tipe | Format | Contoh |
|------|--------|--------|
| Response DTO | `{Entity}Response` | `PostResponse` |
| Request DTO (create) | `{Entity}Request` | `PostRequest` |
| Request DTO (update) | `Update{Entity}Request` | `UpdatePostRequest` |
| Internal DTO | `{Purpose}Data` | `ErrorData` |

### Contoh Response DTO yang Benar
```java
/**
 * Response DTO for post listing and detail endpoints with category data.
 * Uses flat structure (categoryId, categoryName) to avoid circular reference.
 */
@Data
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String status;
    private Long categoryId;
    private String categoryName;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Field Wajib
- `id` (Long) — Primary key
- `createdAt` (LocalDateTime) — Jika entity pakai `@CreatedDate`
- `updatedAt` (LocalDateTime) — Jika entity pakai `@LastModifiedDate`

### Field Dilarang
- `password` — Keamanan
- `token`, `accessToken`, `refreshToken` — Hanya untuk auth response
- `revoked` — Internal field
- Full object relasi — Bisa circular reference

---

## 3. API Response Format

### Wrapper
Semua endpoint HARUS return `ResponseEntity<ResData<T>>` via `ResponseBuilder`.

```java
// ✅ Benar
return ResponseBuilder.ok(data);
return ResponseBuilder.created(data);
return ResponseBuilder.ok(); // delete, no data

// ❌ Dilarang
return ResponseEntity.ok(data);
return ResponseEntity.ok(Map.of("key", "val"));
```

### Struktur Response
```json
{
  "status": "success",
  "message": "Success",
  "data": { ... }
}
```

### HTTP Status Codes
| Code | Penggunaan |
|------|------------|
| 200 | GET, PUT, DELETE berhasil |
| 201 | POST berhasil (create) |
| 400 | Request malformed |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 422 | Validation error |
| 500 | Server error |

---

## 4. Frontend Standards

### Vue Components
- ✅ HARUS menggunakan `<script setup>` syntax
- ✅ HARUS import `AppLayout` (kecuali LoginPage)
- ✅ HARUS gunakan `api` dari `lib/api.js` (bukan axios langsung)
- ✅ HARUS daftarkan route di `router/index.js`
- ✅ HARUS gunakan `usePermission()` untuk permission check

### Contoh Page yang Benar
```vue
<script setup>
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { usePermission } from '@/composables/usePermission'
import api from '@/lib/api'

const { can } = usePermission()
const items = ref([])

async function fetchAll() {
  const res = await api.get('/api/v1/posts?page=0&size=10')
  items.value = res.data.data.content
}

onMounted(fetchAll)
</script>

<template>
  <AppLayout>
    <div v-if="can('post.store')">
      <button>Tambah Post</button>
    </div>
  </AppLayout>
</template>
```

### Routing
```javascript
{
  path: '/dashboard/posts',
  name: 'posts',
  component: () => import('@/pages/PostsPage.vue'),
  meta: {
    requiresAuth: true,
    permission: 'post.index',
    pageTitle: 'Manajemen Post',
    pageSubtitle: 'Kelola konten post.',
  }
}
```

---

## 5. Backend Standards

### Controller
- ✅ HARUS punya Javadoc di setiap mapping method
- ✅ HARUS punya `@PreAuthorize` di setiap endpoint
- ✅ HARUS log format: `[METHOD] /api/v1/path param=val`
- ❌ TIDAK BOLEH ada try-catch (biar GlobalExceptionHandler yang tangkap)
- ❌ TIDAK BOLEH akses Repository langsung

### Service
- ✅ HARUS pakai `@RequiredArgsConstructor`
- ✅ HARUS `private final` untuk dependency
- ✅ HARUS punya Javadoc di public method
- ❌ TIDAK BOLEH ada divider ASCII (`======`)

### Repository
- ✅ HARUS extends `JpaRepository`
- ❌ TIDAK BOLEH ada business logic
- ❌ TIDAK BOLEH Javadoc untuk method bawaan Spring Data

---

## 6. Git Standards

### Commit Message
Format: `type: description`

| Type | Keterangan |
|------|------------|
| `feat` | Fitur baru |
| `fix` | Bug fix |
| `docs` | Dokumentasi |
| `style` | Formatting, styling |
| `refactor` | Code restructuring |
| `test` | Adding tests |
| `chore` | Maintenance |

Contoh:
```
feat: add user management CRUD
fix: resolve circular reference in RoleResponse
docs: update API standard documentation
```

### Branch Naming
- `feature/description`
- `fix/description`
- `docs/description`

---

## 7. Documentation Standards

### File Dokumentasi
Semua dokumentasi ada di `docs/`:
- `README.md` — Indeks utama
- `01-roadmap.md` — Status fase
- `02-architecture.md` — Arsitektur sistem
- `03-structure.md` — Peta direktori
- `04-api-standard.md` — Standar API + DTO
- `05-vue-thymeleaf.md` — Frontend integration
- `06-crud-convention.md` — CRUD convention
- `07-token-system.md` — JWT + permission
- `08-code-rules.md` — Coding rules
- `CORES.md` — Architectural decisions
- `SKILLS.md` — Capability checklist

### Kata Kunci
| Kata | Makna |
|------|-------|
| **HARUS** | Wajib, tidak ada pengecualian |
| **TIDAK BOLEH** | Dilarang keras |
| **WAJIB** | Sama dengan HARUS |
| **SEBAIKNYA** | Rekomendasi kuat |
| **BOLEH** | Opsional |

---

## Update Log

| Tanggal | Update |
|---------|--------|
| 2026-04-26 | PostResponse.java diperbarui mengikuti standar DTO |
| 2026-04-26 | Dokumentasi standar Response DTO ditambahkan di `04-api-standard.md` |
| 2026-04-26 | Aturan pnpm ditambahkan di `05-vue-thymeleaf.md` |
| 2026-04-26 | Dokumen standar proyek dibuat (`STANDARDS.md`) |

---

## Referensi

- API Standard: `docs/04-api-standard.md`
- Frontend Guide: `docs/05-vue-thymeleaf.md`
- CRUD Convention: `docs/06-crud-convention.md`
- Code Rules: `docs/08-code-rules.md`
