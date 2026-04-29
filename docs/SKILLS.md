# SKILLS.md — Spravel Starter Kit Capabilities

> Dokumen ini menjelaskan apa saja yang sudah bisa dilakukan oleh starter kit ini
> sebagai referensi saat mengembangkan fitur baru atau onboarding developer baru.

---

## 1. Authentication

| Kemampuan | Status | Catatan |
|-----------|--------|---------|
| Login dengan username + password | ✅ Done | BCrypt password hashing |
| JWT Access Token | ✅ Done | Embed `perms[]` slug di dalam token |
| JWT Refresh Token | ✅ Done | Refresh otomatis di interceptor frontend |
| Token revoke (logout) | ✅ Done | Token disimpan di DB, bisa di-revoke |
| Force logout semua sesi | ✅ Done | `POST /api/v1/auth/force-logout-all` |
| Fetch user info (`/me`) | ✅ Done | Return user + roles + permissions |

---

## 2. Role & Permission (RBAC + ACL)

| Kemampuan | Status | Catatan |
|-----------|--------|---------|
| Module management | ✅ Done | Grouping permission (e.g. "Posts Module") |
| Permission management | ✅ Done | Slug-based (e.g. `post.index`, `post.store`) |
| Role management | ✅ Done | Setiap role punya slug unik |
| Assign permission ke role | ✅ Done | ACL matrix per role |
| Assign role ke user | ✅ Done | User bisa punya banyak role (M2M) |
| Permission di-embed ke JWT | ✅ Done | Tidak perlu query DB setiap request |
| Permission di-refresh saat token refresh | ✅ Done | Saat access token expired, permissions di-reload dari DB |
| Backend check permission (`@PreAuthorize`) | ✅ Done | Via Spring Security method security |
| Frontend route guard by permission | ✅ Done | Via Vue Router `meta.permission` |
| Frontend composable `can(slug)` | ⏳ Perlu dibuat | Helper cek permission di template |

---

## 3. CRUD Modules

| Module | Index | Show | Store | Update | Delete |
|--------|-------|------|-------|--------|--------|
| Post | ✅ | ✅ | ✅ | ✅ | ✅ |
| Category | ✅ | ✅ | ✅ | ✅ | ✅ |
| Role | ✅ | ✅ | ✅ | ✅ | ✅ |
| Permission | ✅ | ✅ | ✅ | ✅ | ✅ |
| Module | ✅ | ✅ | ✅ | ✅ | ✅ |
| User | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## 4. API Layer

| Kemampuan | Status | Catatan |
|-----------|--------|---------|
| Standardized API response wrapper | ✅ Done | `ResData<T>` dengan `status`, `message`, `data` |
| Flat DTO design (hindari circular reference) | ✅ Done | `PermissionResponse.moduleSlug` |
| Pagination support | ✅ Done | Spring Data `Page<T>` |
| Global exception handler | ✅ Done | `GlobalExceptionHandler` → `ResData<ErrorData>` |
| Custom 401/403 handler | ✅ Done | `CustomAuthEntryPoint`, `CustomAccessDeniedHandler` |
| HTTP request logger | ✅ Done | `HttpLogFilter` middleware |
| Input validation | ✅ Done | Bean Validation (`@Valid`) |

---

## 5. Frontend

| Kemampuan | Status | Catatan |
|-----------|--------|---------|
| Login page | ✅ Done | |
| Dashboard layout (sidebar + header) | ✅ Done | `AppLayout.vue` |
| Auth state management (Pinia) | ✅ Done | Token + user + permissions di localStorage |
| Axios interceptor (auto token inject + refresh) | ✅ Done | `api.js` |
| Route guard (requiresAuth + permission) | ✅ Done | `router/index.js` |
| UI Component library | ✅ Done | Radix Vue + Tailwind CSS |
| Sidebar menu hidden jika tidak punya permission | ⏳ Perlu dibuat | Cek `can()` di sidebar item |
| Composable `usePermission()` / `can()` | ⏳ Perlu dibuat | |

---

## 6. Developer Experience

| Kemampuan | Status | Catatan |
|-----------|--------|---------|
| Database seeder | ✅ Done | `MainSeeder` + `UserSeeder`, auto-run via config |
| Hot reload frontend (Vite) | ✅ Done | `pnpm run dev` proxy ke port 8090 |
| Single JAR deployment | ✅ Done | Maven `package` embed frontend |
| Environment config | ✅ Done | `application.properties` |

---

## Checklist: Yang Masih Perlu Dibuat

- [ ] User CRUD (dengan assign role)
- [ ] Frontend composable `can(slug)` untuk cek permission di komponen
- [ ] Sidebar item hidden/disabled berdasarkan permission
- [ ] Notifikasi toast global
- [ ] Konfirmasi dialog reusable
- [ ] Profile page (edit profil, ganti password)
- [ ] Audit log UI (tampilkan `LogHttp`)
- [ ] Unit test backend (service layer)
- [ ] API documentation (Swagger/OpenAPI)
