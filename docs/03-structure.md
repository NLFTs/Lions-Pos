# Peta Direktori Proyek

Panduan ini menjelaskan lokasi setiap file dan fungsinya.
Gunakan dokumen ini untuk menemukan file yang tepat sebelum membuat atau memodifikasi kode.

---

## Backend: `src/main/java/com/dak/spravel/`

```
com/dak/spravel/
│
├── MainApplication.java                 ← Entry point. Annotasi @EnableJpaAuditing harus ada di sini.
│
├── config/
│   ├── SecurityConfig.java              ← Filter chain, whitelist path, CSRF disable
│   ├── WebMvcConfig.java                ← Static resource handler, CORS config
│   └── JacksonConfig.java               ← (jika ada) Timezone, serialisasi date
│
├── controller/                          ← HANYA routing + delegate ke service
│   ├── MainController.java              ← Server UI, SPA handler (/_/*), Hello (/), Info (/api/v1/info), Docs (/docs)
│   ├── AuthController.java              ← /api/v1/auth/** (login, me, refresh, logout)
│   ├── PostController.java              ← /api/v1/posts
│   ├── CategoryController.java          ← /api/v1/categories
│   ├── RoleController.java              ← /api/v1/roles
│   ├── PermissionController.java        ← /api/v1/permissions
│   ├── ModuleController.java            ← /api/v1/modules
│   ├── UserController.java              ← /api/v1/users
│   └── LogHttpController.java           ← /api/v1/logs
│
├── service/                             ← Implementasi + business logic
│   ├── PostService.java
│   ├── CategoryService.java
│   ├── RoleService.java
│   ├── PermissionService.java
│   └── ModuleService.java
│
├── repository/                          ← HANYA query — extends JpaRepository
│   ├── UserRepository.java
│   ├── TokenRepository.java
│   ├── PostRepository.java
│   ├── CategoryRepository.java
│   ├── RoleRepository.java
│   ├── PermissionRepository.java
│   ├── ModuleRepository.java
│   └── LogHttpRepository.java
│
├── model/                               ← JPA Entity — mapping ke tabel DB
│   ├── User.java                        ← tabel: users
│   ├── Token.java                       ← tabel: tokens
│   ├── Post.java                        ← tabel: posts
│   ├── Category.java                    ← tabel: categories
│   ├── Role.java                        ← tabel: roles + role_permissions (M2M)
│   ├── Permission.java                  ← tabel: permissions
│   ├── Module.java                      ← tabel: modules
│   └── LogHttp.java                     ← tabel: log_http
│
├── dto/
│   ├── request/                         ← Input DTO dengan @Valid annotations
│   │   ├── LoginRequest.java
│   │   ├── PostRequest.java
│   │   ├── UpdatePostRequest.java
│   │   ├── CategoryRequest.java
│   │   ├── UpdateCategoryRequest.java
│   │   ├── RoleRequest.java
│   │   ├── PermissionRequest.java
│   │   └── ModuleRequest.java
│   │
│   ├── response/                        ← Output DTO — tidak ada field sensitif
│   │   ├── ResData.java                 ← { status, message, data } — wrapper response
│   │   ├── TokenResponse.java           ← { accessToken, refreshToken }
│   │   ├── MeResponse.java              ← { id, username, fullname, roles, permissions }
│   │   ├── UserResponse.java            ← { id, username, fullname, roles: RoleData[] }
│   │   ├── PostResponse.java
│   │   ├── CategoryResponse.java
│   │   ├── RoleResponse.java            ← { id, slug, name, permissions: PermissionResponse[] }
│   │   ├── PermissionResponse.java      ← { id, slug, name, moduleSlug } — flat, no circular ref
│   │   └── ModuleResponse.java
│   │
│   └── data/                            ← Internal DTO
│       └── ErrorData.java               ← { timestamp, status, error, message, path }
│
├── middleware/                          ← OncePerRequestFilter
│   ├── JwtAuthFilter.java               ← Validasi token + set SecurityContext
│   └── HttpLogFilter.java               ← Log setiap HTTP request/response
│
├── handler/                             ← Error & security handlers
│   ├── GlobalExceptionHandler.java      ← @RestControllerAdvice — tangkap semua exception
│   ├── ResourceNotFoundException.java   ← Custom exception → 404
│   ├── CustomAuthEntryPoint.java        ← 401 response handler
│   └── CustomAccessDeniedHandler.java   ← 403 response handler
│
├── seeder/
│   ├── MainSeeder.java                  ← Orchestrator — implements ApplicationRunner
│   └── UserSeeder.java                  ← Seed user + role + permission default
│
└── util/                                ← Stateless helper — tidak ada @Autowired
    ├── JwtUtil.java                     ← generate/validate/extract JWT
    ├── ResponseBuilder.java             ← Build ResData — ok(), created(), error()
    ├── ResponseConstant.java            ← Pesan standar
    └── StringUtil.java                  ← String helper
```

---

## Backend: `src/main/resources/`

```
resources/
├── application.properties               ← Konfigurasi utama (DB, JWT, port, seeder)
├── logback.xml                          ← Konfigurasi logging
└── static/                             ← Output build frontend (jangan edit manual)
    ├── index.html                       ← Vue shell (generated by Vite)
    └── assets/
        ├── index.js                     ← Vue bundle (generated)
        └── index.css                    ← CSS bundle (generated)
```

**TIDAK BOLEH**: Edit file di `static/` secara manual. File ini di-generate oleh `pnpm run build`.

---

## Frontend: `frontend/src/`

```
frontend/src/
│
├── main.js                              ← createApp(App).use(Pinia).use(Router).mount('#app')
├── App.vue                              ← Root component — hanya berisi <RouterView />
│
├── router/
│   └── index.js                         ← Route list + beforeEach navigation guard
│
├── stores/
│   └── auth.js                          ← Pinia store: accessToken, user, permissions
│                                          Methods: login(), logout(), fetchMe()
│
├── lib/
│   └── api.js                           ← Axios instance
│                                          Interceptor request: inject Bearer token
│                                          Interceptor response: auto refresh 401
│
├── composables/
│   ├── usePermission.js                 ← { can(slug): boolean }
│   ├── useToast.js                      ← toast.success/error/info
│   └── useConfirm.js                    ← confirm dialog reusable
│
├── components/
│   ├── AppLayout.vue                    ← Sidebar + header + <slot /> untuk konten
│   └── ui/                              ← Radix Vue headless components
│       ├── button/
│       ├── input/
│       ├── table/
│       ├── dialog/
│       ├── badge/
│       └── ... (dst)
│
└── pages/
    ├── LoginPage.vue                    ← Form login — tidak pakai AppLayout
    ├── DashboardPage.vue                ← Halaman utama setelah login
    ├── PostsPage.vue                    ← CRUD post
    ├── CategoriesPage.vue               ← CRUD category
    ├── RolesPage.vue                    ← CRUD role + permission matrix
    ├── PermissionsPage.vue              ← CRUD permission
    ├── ModulesPage.vue                  ← CRUD module
    ├── UsersPage.vue                    ← CRUD user + assign roles
    ├── LogsPage.vue                     ← Audit log viewer
    └── ProfilePage.vue                  ← Profile + change password
```

---

## Frontend: Config Files

```
frontend/
├── index.html                           ← HTML shell: <div id="app">
├── vite.config.js                       ← outDir: ../src/main/resources/static
│                                          proxy: /api → http://localhost:8090
├── package.json                         ← Dependencies + scripts
│                                          build: "vite build"
│                                          dev: "vite"
├── tailwind.config.js                   ← Tailwind CSS configuration
└── postcss.config.js                    ← PostCSS (untuk Tailwind)
```

---

## Build & Maven

```
pom.xml                                  ← Dependencies + build plugins
mvnw                                     ← Maven wrapper script
spravel                                  ← CLI shortcut script

./spravel frontend:install               ← pnpm install
./spravel frontend:build                 ← pnpm run build
./spravel frontend:dev                   ← pnpm run dev (Vite HMR)
./spravel backend:dev                    ← spring-boot:run
./spravel full:build                     ← Frontend + backend JAR
./spravel full:dev                       ← Vite + Spring Boot (parallel)
```

---

## Docs

```
docs/
├── README.md                            ← Indeks utama — baca ini pertama
├── 01-roadmap.md                        ← Status semua fase
├── 02-architecture.md                   ← Alur request, layer, relasi DB
├── 03-structure.md                      ← File ini — peta direktori
├── 04-api-standard.md                   ← Kontrak response API
├── 05-vue-thymeleaf.md                  ← Vue 3 + Vite integration
├── 06-crud-convention.md                ← Langkah buat resource CRUD baru
├── 07-token-system.md                   ← JWT auth + permission flow
├── CORES.md                             ← Keputusan arsitektur
└── SKILLS.md                            ← Capability checklist
```

---

## Aturan Lokasi File

| Kondisi | Lokasi |
|---------|--------|
| Kelas yang hanya berisi `@Entity` | `model/` |
| Kelas yang hanya berisi query (`findBy...`, `@Query`) | `repository/` |
| Interface kontrak business logic | `service/` |
| Implementasi business logic | `service/impl/` |
| Kelas yang terima input dari HTTP body/param | `dto/request/` |
| Kelas yang dikirim ke client | `dto/response/` |
| Kelas yang dipakai antar-service | `dto/data/` |
| Kelas yang handle HTTP request + delegate ke service | `controller/` |
| Filter yang berjalan tiap request | `middleware/` |
| Kelas yang handle Exception | `handler/` |
| Data awal untuk dev/staging | `seeder/` |
| Utility stateless (tidak ada state, tidak ada `@Autowired`) | `util/` |
