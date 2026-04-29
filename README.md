# ⚡ Spravel

> **Spring Boot Starter Kit bergaya Laravel** — untuk developer Laravel yang ingin familiar di ekosistem Spring Boot.

Spravel menyediakan fondasi lengkap untuk aplikasi enterprise: autentikasi JWT dengan RBAC, audit logging, CRUD-ready structure, dan Vue 3 SPA frontend — semuanya sudah terkonfigurasi dan siap pakai.

---

## ✨ Core Features

| Feature | Deskripsi |
|---------|-----------|
| 🔐 **JWT Authentication** | Access token + refresh token per sesi, auto token refresh di frontend |
| 🛡️ **RBAC** | User → Role → Permission → Module hierarchy |
| 📝 **Audit Logging** | Semua HTTP request otomatis dicatat ke tabel `log_http` |
| 🌱 **Database Seeder** | Data awal otomatis saat pertama kali start |
| 📚 **CRUD-Ready** | Pola konsisten: Model → Repository → Service → Controller → DTO |
| 🖥️ **Vue 3 SPA** | Tailwind CSS + Pinia + Axios dengan auto token refresh |
| 📖 **Swagger UI** | Dokumentasi API interaktif |

---

## 📋 Requirements

| Tool | Versi | Keterangan |
|------|-------|------------|
| **Java JDK** | 21+ | Wajib |
| **Maven** | 3.9+ | Wrapper (`./mvnw`) tersedia |
| **Node.js** | 20.19+ or 22.12+ | Frontend dev server |
| **pnpm** | 10+ | Package manager frontend (via `npx -y pnpm`) |
| **PostgreSQL** | 14+ | Database |

---

## 🚀 Quick Start

### 1. Clone & Setup

```bash
git clone <repo-url>
cd Spravel
```

### 2. Buat Database

```bash
psql -U postgres -c "CREATE DATABASE db_spravel;"
```

### 3. Konfigurasi (opsional)

Edit `src/main/resources/application.properties` jika perlu menyesuaikan koneksi database atau JWT secret. Lihat bagian [Konfigurasi](#-konfigurasi) untuk detail lengkap.

### 4. Jalankan

**Development mode** (hot reload, 2 terminal):

```bash
# Terminal 1 — Backend
./spravel backend:dev

# Terminal 2 — Frontend (Vite dev server)
./spravel frontend:dev
```

Atau jalankan keduanya sekaligus:

```bash
./spravel full:dev
```

| Service | URL |
|---------|-----|
| Vue SPA (Vite dev) | http://localhost:5173/_/login |
| Vue SPA (Spring Boot) | http://localhost:8090/_/login |
| API (Spring Boot) | http://localhost:8090/api/v1/ |

**Production mode** (build JAR):

```bash
./spravel full:build
java -jar target/spravel-0.0.1-SNAPSHOT.jar
```

Akses: http://localhost:8090/_/login

---

## 📝 Commands (`./spravel`)

Spravel menyediakan CLI shortcut `./spravel` untuk semua operasi development:

```bash
./spravel                     # Tampilkan help
```

### Frontend

```bash
./spravel frontend:install    # Install dependencies (pnpm)
./spravel frontend:build      # Build Vue → static/
./spravel frontend:dev        # Vite dev server (HMR)
./spravel frontend:clean      # Clean build output
```

### Backend

```bash
./spravel backend:dev         # Spring Boot dev server
./spravel backend:build       # Compile backend only
./spravel backend:test        # Run tests
./spravel backend:clean       # Clean build
```

### Fullstack

```bash
./spravel full:build          # Frontend + backend JAR (production)
./spravel full:clean          # Clean everything
./spravel full:dev            # Vite + Spring Boot (parallel)
```

> Bisa juga langsung pakai Maven wrapper: `./mvnw spring-boot:run`, `./mvnw package`, dll.

---

## 🔐 Default Credentials

| Username | Password | Role |
|----------|----------|------|
| `su` | `su@123` | Super Admin |

> Seeder berjalan saat `app.enable.seeder=true`. Setelah production setup, set ke `false`.

---

## 🔧 Konfigurasi

Spravel menggunakan **satu file** `application.properties` untuk semua environment. Perbedaan antar environment dikontrol via **environment variable**.

### Environment Variables

| Variable | Default | Deskripsi |
|----------|---------|-----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/db_spravel` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Username database |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Password database |
| `APP_JWT_SECRET` | *(dev secret)* | Secret key JWT (base64, min 32 chars) |
| `SERVER_PORT` | `8090` | Port aplikasi |
| `SPRING_JPA_DDL_AUTO` | `update` | Hibernate DDL strategy |
| `SPRING_JPA_SHOW_SQL` | `true` | Tampilkan SQL query di log |
| `LOGGING_LEVEL_APP` | `DEBUG` | Log level aplikasi |
| `LOGGING_LEVEL_ROOT` | `INFO` | Log level root |
| `SPRING_THYMELEAF_CACHE` | `false` | Template cache |
| `APP_ENABLE_SEEDER` | `true` | Jalankan seeder saat startup |
| `SPRING_DATASOURCE_HIKARI_MAX_POOL_SIZE` | `10` | Max koneksi database |
| `SPRING_DATASOURCE_HIKARI_MIN_IDLE` | `5` | Min idle koneksi |






### Contoh Production

```bash
export SPRING_JPA_DDL_AUTO=validate
export SPRING_JPA_SHOW_SQL=false
export APP_ENABLE_SEEDER=false
export SPRING_THYMELEAF_CACHE=true
export LOGGING_LEVEL_APP=WARN
export APP_JWT_SECRET=<your-base64-secret>
export SPRING_DATASOURCE_PASSWORD=<your-db-password>

java -jar target/spravel-0.0.1-SNAPSHOT.jar
```

### Frontend — Vite .env

Konfigurasi frontend ada di `frontend/`:

| File | Kapan Digunakan |
|------|-----------------|
| `.env.example` | Template (commit ke git) |
| `.env.development` | Shared defaults development |
| `.env.production` | Production build defaults |
| `.env.local` | Override lokal (gitignored) |

| Variable | Default | Deskripsi |
|----------|---------|-----------|
| `VITE_API_URL` | `http://localhost:8090` | URL backend API |
| `VITE_DEV_PORT` | `5173` | Port Vite dev server |
| `VITE_APP_TITLE` | `Spravel` | Title di browser tab |

> **Note:** Saat development, Vite dev server menggunakan proxy `/api` → `http://localhost:8090` sehingga tidak ada CORS issue.

---

## 🗄️ Database

### Tabel yang Dibuat Otomatis

| Tabel | Fungsi |
|-------|--------|
| `users` | Data user untuk autentikasi |
| `roles` | Role untuk RBAC |
| `permissions` | Permission (slug + name) per module |
| `modules` | Grouping key untuk permissions |
| `tokens` | Access + refresh token per sesi, revocable |
| `posts` | Contoh resource CRUD |
| `categories` | Kategori untuk posts |
| `log_http` | Audit log setiap HTTP request |

> Tidak perlu membuat tabel manual. `ddl-auto=update` membuat tabel otomatis saat start.

---

## 📡 REST API

Base URL: `/api/v1`

### Response Format

Semua endpoint mengembalikan format yang konsisten:

```json
{
  "status": "success",
  "message": "Success",
  "data": { ... }
}
```

Field `status` berisi `"success"` (untuk 2xx) atau `"error"` (untuk 4xx/5xx). Field `data` berisi DTO yang sesuai dengan operasi, atau `null` untuk operasi tanpa data (DELETE, void).

### Authentication

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| `POST` | `/auth/login` | ❌ | Login → access + refresh token |
| `POST` | `/auth/refresh` | ❌ | Refresh access token |
| `POST` | `/auth/logout` | ✅ | Logout (revoke current token) |
| `POST` | `/auth/force-logout-all` | ✅ | Logout semua sesi user |

**Contoh login:**

```bash
curl -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "su", "password": "su@123"}'
```

### Resource APIs

| Resource | Endpoints | Auth |
|----------|-----------|------|
| **Posts** | `GET/POST/PUT/DELETE /posts` | ✅ |
| **Categories** | `GET/POST/PUT/DELETE /categories` | ✅ |

### Management APIs

| Resource | Endpoints | Auth |
|----------|-----------|------|
| Users | `/users` | ✅ |
| Roles | `/roles` | ✅ |
| Permissions | `/permissions` | ✅ |
| Modules | `/modules` | ✅ |
| Audit Logs | `/logs` | ✅ |

**Contoh request dengan token:**

```bash
curl http://localhost:8090/api/v1/posts \
  -H "Authorization: Bearer eyJ..."
```

---

## 🖥️ Web UI

Semua route Vue SPA ada di bawah prefix `/_/`:

| Route | Deskripsi | Auth |
|-------|-----------|------|
| `/_/login` | Halaman login | ❌ |
| `/_/dashboard` | Dashboard | ✅ |
| `/_/dashboard/categories` | CRUD Categories | ✅ |
| `/_/dashboard/posts` | CRUD Posts | ✅ |
| `/_/dashboard/logs` | Audit Log (permission: `log.index`) | ✅ |

Route Thymeleaf biasa **tidak** pakai prefix `/_/`, contoh:

| Route | Deskripsi |
|-------|-----------|
| `/hello` | Health check (REST) |
| `/docs` | Swagger docs redirect |

---

## 🏗️ Arsitektur

### Routing

Spravel memisahkan Vue SPA dan Thymeleaf views menggunakan prefix `/_/`:

```
http://localhost:8090/
├── /_/...                    → Vue SPA (login, dashboard, dll)
│   ├── /_/login              → Vue login page
│   ├── /_/dashboard          → Vue dashboard
│   └── /_/dashboard/**       → Vue sub-pages
│
├── /hello                    → Thymeleaf view (HelloController)
├── /docs                     → Thymeleaf redirect (DocsController)
│
└── /api/v1/...               → REST API (semua controllers)
```

Cara kerja:
1. **`WebController`** — mapping `/_/**` → serve `index.html` (Thymeleaf shell)
2. **Vue Router** — `base: '/_/'`, handle client-side routing di dalam SPA
3. **Thymeleaf template** — saat dev mode, load JS dari Vite dev server; saat production, load dari static build
4. **Route Thymeleaf biasa** — tetap di root path, tidak terpengaruh prefix `/_/`

### Project Structure

```
src/main/java/com/dak/spravel/
├── config/              # Security, Swagger, WebMvc
├── controller/          # REST controllers
├── dto/                 # Request & Response DTOs
├── handler/             # Exception handlers
├── middleware/          # JWT filter, HTTP log filter
├── model/               # JPA entities
├── repository/          # Spring Data JPA
├── seeder/              # Database seeders
├── service/             # Service interfaces
│   └── impl/            # Service implementations
└── util/                # JWT, ResponseBuilder, Constants

frontend/
├── src/
│   ├── components/      # Reusable UI
│   ├── pages/           # Page components
│   ├── stores/          # Pinia stores
│   ├── lib/             # API layer
│   └── router/          # Vue Router
├── package.json         # pnpm (packageManager field)
└── vite.config.js       # Vite 8 config
```

### Cara Membuat CRUD Baru

1. **Model** — `model/YourEntity.java` + `@Entity`
2. **Repository** — `repository/YourEntityRepository.java` extends `JpaRepository`
3. **Service** — `service/YourEntityService.java` (interface) + `impl/YourEntityServiceImpl.java`
4. **DTO** — `dto/request/` (Create & Update) + `dto/response/`
5. **Controller** — `controller/YourEntityController.java` + `@RequestMapping("/api/v1/entities")`

Gunakan `ResponseBuilder.ok(data)` untuk semua return. Controller mengembalikan `ResponseEntity<ResData<T>>` dimana T adalah DTO.

---

## 📖 Swagger UI

```
http://localhost:8090/swagger-ui.html
```

Semua endpoint terdokumentasi dengan skema request/response.

---

## 🛠️ Tech Stack

| Layer | Teknologi |
|-------|-----------|
| Backend | Spring Boot 3.4.5, Java 21 |
| Build Tool | Maven 3.9+ (wrapper included) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Auth | JWT (JJWT 0.12.6) + Spring Security |
| API Docs | SpringDoc OpenAPI |
| Frontend | Vue 3.5 + Vite 8 |
| Styling | Tailwind CSS v3 |
| State | Pinia |
| HTTP | Axios + auto token refresh |
| Package Manager | pnpm |
| Template | Thymeleaf |
| Logging | Logback (rolling, 7 hari) |

---

## 🔒 Security — Production Checklist

- [ ] Set `SPRING_DATASOURCE_PASSWORD` via environment variable
- [ ] Set `APP_JWT_SECRET` via environment variable (base64, beda dari dev)
- [ ] Set `SPRING_JPA_DDL_AUTO=validate` (atau `none`)
- [ ] Set `APP_ENABLE_SEEDER=false`
- [ ] Set `SPRING_THYMELEAF_CACHE=true`
- [ ] Set `LOGGING_LEVEL_APP=WARN`
- [ ] Jangan commit `.env.local` atau secret ke version control
- [ ] Vue SPA akses via `/_/`, bukan root path

---

## 📄 License

MIT
