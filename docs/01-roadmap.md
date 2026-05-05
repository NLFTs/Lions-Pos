# Roadmap Spravel Starter Kit

## Status Overview

```
Phase 1 — Core Foundation        ✅ DONE
Phase 2 — Response Standard      ✅ DONE
Phase 3 — Token Enhancement      ✅ DONE
Phase 4 — CRUD Convention        ✅ DONE
Phase 5 — Vue 3 + Vite SPA       ✅ DONE
Phase 6 — RBAC / ACL System      ✅ DONE
Phase 7 — Polish & Docs          🔄 IN PROGRESS
Phase 8 — User Management        ⏳ TODO
Phase 9 — Production Ready       ⏳ TODO
```

---

## Phase 1 — Core Foundation ✅

**Goal:** Infrastruktur dasar berjalan, JWT auth aktif, logging berjalan.

| Task | Status |
|------|--------|
| Project init Spring Boot 3.4.5 (Java 21) | ✅ |
| PostgreSQL + JPA + Hikari config | ✅ |
| User model + UserRepository | ✅ |
| Token model (access + refresh token) | ✅ |
| JWT utility (generate, validate, extract claims) | ✅ |
| JWT filter middleware (`JwtAuthFilter`) | ✅ |
| HTTP request logging filter (`HttpLogFilter`) | ✅ |
| Global exception handler (`GlobalExceptionHandler`) | ✅ |
| Custom 401/403 handler | ✅ |
| User seeder (`UserSeeder` + `MainSeeder`) | ✅ |
| `ResData<T>` DTO | ✅ |
| `ResponseConstant` utility | ✅ |
| `ResponseBuilder` utility | ✅ |

---

## Phase 2 — Response Standard ✅

**Goal:** Semua endpoint menggunakan format response seragam.

| Task | Status |
|------|--------|
| `ResData<T>` wrapper (`status`, `message`, `data`) | ✅ |
| `ResponseBuilder` helper (ok, created, error) — updated to ResData | ✅ |
| `GlobalExceptionHandler` cover semua case — updated to ResData | ✅ |
| `ErrorData` DTO untuk error detail | ✅ |
| Flat DTO design: `PermissionResponse.moduleSlug` (hindari circular reference) | ✅ |
| Dokumentasi di `docs/04-api-standard.md` | ✅ |
| `docs/08-code-rules.md` — comment style + response rules | ✅ |

---

## Phase 3 — Token Enhancement ✅

**Goal:** Token menyimpan access + refresh, permission di-embed di JWT.

| Task | Status |
|------|--------|
| `Token` model: `accessToken` + `refreshToken` + `tokenType` | ✅ |
| Permission slugs di-embed sebagai claim `perms` di JWT | ✅ |
| Login: kumpulkan semua permission → masuk ke token | ✅ |
| Refresh: reload permission dari DB → token baru | ✅ |
| Logout: revoke token di DB | ✅ |
| Force logout semua sesi | ✅ |
| Frontend: decode permission dari JWT payload tanpa library | ✅ |
| Dokumentasi di `docs/07-token-system.md` | ✅ |

---

## Phase 4 — CRUD Convention ✅

**Goal:** Template CRUD standar, semua module ikuti pola yang sama.

| Task | Status |
|------|--------|
| Service layer wajib (interface + impl) | ✅ |
| `PostController` CRUD lengkap | ✅ |
| `CategoryController` CRUD lengkap | ✅ |
| `RoleController` + assign permissions | ✅ |
| `PermissionController` CRUD | ✅ |
| `ModuleController` CRUD | ✅ |
| Request DTO dengan `@Valid` validation | ✅ |
| Response DTO (tidak expose entity langsung) | ✅ |
| `ResourceNotFoundException` custom exception | ✅ |
| `@PreAuthorize` di setiap endpoint | ✅ |
| Pagination support di semua list endpoint | ✅ |
| Dokumentasi di `docs/06-crud-convention.md` | ✅ |

---

## Phase 5 — Vue 3 + Vite SPA ✅

**Goal:** Frontend modern dengan Vue 3, Pinia, Vite, dan Tailwind CSS.

| Task | Status |
|------|--------|
| Vue 3 + Vite project setup | ✅ |
| Pinia auth store (token + user + permissions) | ✅ |
| Vue Router 5 dengan navigation guard | ✅ |
| Guard `requiresAuth` + `permission` per route | ✅ |
| Axios interceptor: inject token + auto refresh 401 | ✅ |
| Queue pattern untuk concurrent 401 handling | ✅ |
| `AppLayout.vue` (sidebar + header) | ✅ |
| Radix Vue + Tailwind CSS UI components | ✅ |
| Login page | ✅ |
| Dashboard page | ✅ |
| Maven build: frontend + backend JAR | ✅ |
| Dev proxy Vite → Spring Boot (:8090) | ✅ |
| Dokumentasi di `docs/05-vue-thymeleaf.md` | ✅ |

---

## Phase 6 — RBAC / ACL System ✅

**Goal:** Role management granular dengan permission matrix per role.

| Task | Status |
|------|--------|
| `Module` entity (grouping permission) | ✅ |
| `Permission` entity (slug-based) | ✅ |
| `Role` entity (slug unik, many-to-many permission) | ✅ |
| User → Roles (M2M) | ✅ |
| Role → Permissions (M2M) | ✅ |
| Permission → Module (M2O) | ✅ |
| API: assign permissions ke role | ✅ |
| Permission matrix UI di `RolesPage.vue` | ✅ |
| Frontend pages: Roles, Permissions, Modules | ✅ |
| Seeder: admin role + semua permission default | ✅ |
| Backend `@PreAuthorize` per endpoint | ✅ |

---

## Phase 7 — Polish & Docs 🔄 In Progress

**Goal:** Starter kit siap di-fork, semua pola terdokumentasi.

| Task | Status |
|------|--------|
| `docs/04-api-standard.md` — response format lengkap | ✅ |
| `docs/05-vue-thymeleaf.md` — Vue 3 Vite SPA integration | ✅ |
| `docs/06-crud-convention.md` — CRUD backend + frontend | ✅ |
| `docs/07-token-system.md` — JWT + permission flow | ✅ |
| `docs/CORES.md` — keputusan arsitektur | ✅ |
| `docs/SKILLS.md` — capability checklist | ✅ |
| Composable `usePermission.js` (`can(slug)`) | ⏳ |
| Sidebar menu filtering by permission | ⏳ |
| Toast notification global | ⏳ |
| Reusable confirm dialog | ⏳ |
| Swagger / OpenAPI docs (`springdoc-openapi`) | ⏳ |
| `README.md` utama di-update | ⏳ |

---

## Phase 8 — User Management ⏳ TODO

**Goal:** CRUD user lengkap dengan assign role — fitur inti admin panel.

| Task | Status |
|------|--------|
| `UserController` CRUD (index, show, store, update, delete) | ⏳ |
| `UserService` + `UserServiceImpl` | ⏳ |
| `UserRequest` DTO (username, fullname, password, roleIds) | ⏳ |
| `UserResponse` DTO (tanpa password) | ⏳ |
| Assign roles ke user saat create/update | ⏳ |
| Endpoint `PUT /api/v1/users/{id}/roles` | ⏳ |
| Permission slugs: `user.index`, `user.store`, dll | ⏳ |
| `UsersPage.vue` frontend | ⏳ |
| Ganti password (self-service) | ⏳ |
| Profile page | ⏳ |

---

## Phase 9 — Production Ready ⏳ TODO

**Goal:** Siap deploy ke production environment.

| Task | Status |
|------|--------|
| Environment variable config (single `application.properties`) | ✅ |
| JWT secret dari environment variable (bukan hardcode) | ✅ |
| Rate limiting di `/api/v1/auth/login` dan `/refresh` | ⏳ |
| Matikan seeder otomatis di production (`APP_ENABLE_SEEDER=false`) | ✅ |
| SQL migration scripts (untuk yang tidak pakai `ddl-auto=update`) | ⏳ |
| Health check endpoint (`GET /actuator/health`) | ⏳ |
| CORS config yang proper | ⏳ |
| Unit test: `JwtUtil`, `AuthController`, service layer | ⏳ |
| Docker + docker-compose | ⏳ |

---

## Next Steps (Prioritas)

```
1. Selesaikan Phase 7:
   → Buat composables/usePermission.js
   → Filter sidebar item berdasarkan permission
   → Toast notification global

2. Lanjut Phase 8:
   → User CRUD (backend + frontend)
   → Assign role ke user

3. Phase 9 bisa dikerjakan bertahap sesuai kebutuhan deployment
```
