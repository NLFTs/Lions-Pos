import{_ as a,o as n,c as e,ae as p}from"./chunks/framework.CDhj6wcg.js";const h=JSON.parse('{"title":"Arsitektur Sistem","description":"","frontmatter":{},"headers":[],"relativePath":"02-architecture.md","filePath":"02-architecture.md"}'),t={name:"02-architecture.md"};function l(i,s,r,o,d,c){return n(),e("div",null,[...s[0]||(s[0]=[p(`<h1 id="arsitektur-sistem" tabindex="-1">Arsitektur Sistem <a class="header-anchor" href="#arsitektur-sistem" aria-label="Permalink to &quot;Arsitektur Sistem&quot;">​</a></h1><hr><h2 id="_1-layer-system" tabindex="-1">1. Layer System <a class="header-anchor" href="#_1-layer-system" aria-label="Permalink to &quot;1. Layer System&quot;">​</a></h2><p>Spravel menggunakan arsitektur berlapis. Setiap request melewati layer dari atas ke bawah, response kembali dari bawah ke atas.</p><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>┌─────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│  CLIENT (Browser / API Consumer)                        │</span></span>
<span class="line"><span>└─────────────────────────────────────────────────────────┘</span></span>
<span class="line"><span>                          │ HTTP Request</span></span>
<span class="line"><span>                          ▼</span></span>
<span class="line"><span>┌─────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│  MIDDLEWARE LAYER                                        │</span></span>
<span class="line"><span>│  1. HttpLogFilter     → log method, path, IP, durasi    │</span></span>
<span class="line"><span>│  2. JwtAuthFilter     → validasi Bearer token,          │</span></span>
<span class="line"><span>│                          set SecurityContext             │</span></span>
<span class="line"><span>└─────────────────────────────────────────────────────────┘</span></span>
<span class="line"><span>                          │</span></span>
<span class="line"><span>                          ▼</span></span>
<span class="line"><span>┌─────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│  SECURITY LAYER (Spring Security)                        │</span></span>
<span class="line"><span>│  - Cek apakah path butuh auth                           │</span></span>
<span class="line"><span>│  - @PreAuthorize(&quot;hasAuthority(&#39;slug&#39;)&quot;) per method     │</span></span>
<span class="line"><span>└─────────────────────────────────────────────────────────┘</span></span>
<span class="line"><span>                          │</span></span>
<span class="line"><span>                          ▼</span></span>
<span class="line"><span>┌─────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│  PRESENTATION LAYER                                      │</span></span>
<span class="line"><span>│  Controller     ← menerima request, validasi @Valid      │</span></span>
<span class="line"><span>│  DTO (Request)  ← binding + constraint validation        │</span></span>
<span class="line"><span>│  DTO (Response) ← mapping output, tidak expose entity   │</span></span>
<span class="line"><span>└─────────────────────────────────────────────────────────┘</span></span>
<span class="line"><span>                          │</span></span>
<span class="line"><span>                          ▼</span></span>
<span class="line"><span>┌─────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│  BUSINESS LAYER                                          │</span></span>
<span class="line"><span>│  Service (interface) + ServiceImpl                       │</span></span>
<span class="line"><span>│  - Semua business logic ada di sini                     │</span></span>
<span class="line"><span>│  - Controller TIDAK BOLEH akses Repository langsung     │</span></span>
<span class="line"><span>└─────────────────────────────────────────────────────────┘</span></span>
<span class="line"><span>                          │</span></span>
<span class="line"><span>                          ▼</span></span>
<span class="line"><span>┌─────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│  DATA ACCESS LAYER                                       │</span></span>
<span class="line"><span>│  Repository (extends JpaRepository)                      │</span></span>
<span class="line"><span>│  - Query ke database                                     │</span></span>
<span class="line"><span>│  - Tidak ada business logic                             │</span></span>
<span class="line"><span>└─────────────────────────────────────────────────────────┘</span></span>
<span class="line"><span>                          │</span></span>
<span class="line"><span>                          ▼</span></span>
<span class="line"><span>┌─────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│  DATABASE LAYER                                          │</span></span>
<span class="line"><span>│  PostgreSQL ← dipetakan via JPA Entity/Model            │</span></span>
<span class="line"><span>└─────────────────────────────────────────────────────────┘</span></span></code></pre></div><hr><h2 id="_2-alur-request-api-protected-endpoint" tabindex="-1">2. Alur Request API (Protected Endpoint) <a class="header-anchor" href="#_2-alur-request-api-protected-endpoint" aria-label="Permalink to &quot;2. Alur Request API (Protected Endpoint)&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>POST /api/v1/posts  (Authorization: Bearer &lt;token&gt;)</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[1] HttpLogFilter</span></span>
<span class="line"><span>│      → catat: method, path, IP, timestamp</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[2] JwtAuthFilter</span></span>
<span class="line"><span>│      → extract token dari header Authorization</span></span>
<span class="line"><span>│      → jwtUtil.validateToken(token)</span></span>
<span class="line"><span>│      → getUsernameFromToken(token)        → &quot;admin&quot;</span></span>
<span class="line"><span>│      → getPermissionsFromToken(token)     → [&quot;post.store&quot;, ...]</span></span>
<span class="line"><span>│      → set SecurityContextHolder(username, permissions)</span></span>
<span class="line"><span>│      → TIDAK query DB</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[3] Spring Security</span></span>
<span class="line"><span>│      → anyRequest().authenticated() → pass (sudah set di SecurityContext)</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[4] PostController.store()</span></span>
<span class="line"><span>│      → @PreAuthorize(&quot;hasAuthority(&#39;post.store&#39;)&quot;)</span></span>
<span class="line"><span>│      → cek authorities di SecurityContext → pass / throw AccessDeniedException</span></span>
<span class="line"><span>│      → @Valid @RequestBody PostRequest → validasi field</span></span>
<span class="line"><span>│      → panggil postService.create(request)</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[5] PostServiceImpl.create()</span></span>
<span class="line"><span>│      → business logic</span></span>
<span class="line"><span>│      → panggil postRepository.save(entity)</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[6] PostRepository</span></span>
<span class="line"><span>│      → INSERT ke tabel posts</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>└─[7] Response</span></span>
<span class="line"><span>       → ResponseBuilder.created(postResponse)</span></span>
<span class="line"><span>       → {&quot;status&quot;:&quot;success&quot;,&quot;message&quot;:&quot;Created successfully&quot;,&quot;data&quot;:{...}}</span></span></code></pre></div><hr><h2 id="_3-alur-login-token" tabindex="-1">3. Alur Login &amp; Token <a class="header-anchor" href="#_3-alur-login-token" aria-label="Permalink to &quot;3. Alur Login &amp; Token&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>POST /api/v1/auth/login  {username, password}</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[1] Cari user by username → UserRepository</span></span>
<span class="line"><span>├─[2] BCrypt.matches(inputPassword, storedHash)</span></span>
<span class="line"><span>├─[3] Kumpulkan permissions:</span></span>
<span class="line"><span>│      user.getRoles()</span></span>
<span class="line"><span>│        .flatMap(role → role.getPermissions())</span></span>
<span class="line"><span>│        .map(perm → perm.getSlug())</span></span>
<span class="line"><span>│        .distinct().sorted()</span></span>
<span class="line"><span>│      hasil: [&quot;category.index&quot;, &quot;post.delete&quot;, &quot;post.index&quot;, ...]</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[4] Revoke semua token lama user di DB</span></span>
<span class="line"><span>├─[5] JWT Access Token = generateAccessToken(username, permissions)</span></span>
<span class="line"><span>│      payload: { sub: &quot;admin&quot;, perms: [...], iat: ..., exp: now+5min }</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[6] JWT Refresh Token = generateRefreshToken(username)</span></span>
<span class="line"><span>│      payload: { sub: &quot;admin&quot;, iat: ..., exp: now+24h }</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[7] Simpan record ke tabel tokens:</span></span>
<span class="line"><span>│      { access_token, refresh_token, username, token_type:&quot;Bearer&quot;,</span></span>
<span class="line"><span>│        revoked:false, expiry_date: now+24h }</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>└─[8] Return: { accessToken, refreshToken }</span></span></code></pre></div><hr><h2 id="_4-alur-refresh-token" tabindex="-1">4. Alur Refresh Token <a class="header-anchor" href="#_4-alur-refresh-token" aria-label="Permalink to &quot;4. Alur Refresh Token&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>POST /api/v1/auth/refresh?refreshToken=&lt;rt&gt;</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─[1] Cari token record by refreshToken di tabel tokens</span></span>
<span class="line"><span>├─[2] Validasi: revoked == false &amp;&amp; expiryDate &gt; now</span></span>
<span class="line"><span>├─[3] Load ulang user dari DB → ambil permissions TERBARU</span></span>
<span class="line"><span>│      (ini satu-satunya titik di mana permissions di-resync dari DB)</span></span>
<span class="line"><span>├─[4] Generate access token baru dengan permissions terbaru</span></span>
<span class="line"><span>├─[5] Update record: access_token = newAccessToken (refresh_token tetap sama)</span></span>
<span class="line"><span>└─[6] Return: { newAccessToken, refreshToken (sama) }</span></span></code></pre></div><hr><h2 id="_5-alur-frontend-vue-spa" tabindex="-1">5. Alur Frontend (Vue SPA) <a class="header-anchor" href="#_5-alur-frontend-vue-spa" aria-label="Permalink to &quot;5. Alur Frontend (Vue SPA)&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>Browser buka http://localhost:5173 (dev) atau http://localhost:8090 (prod)</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─ Vue Router: cek route yang diminta</span></span>
<span class="line"><span>│   ├─ meta.requiresAuth=true + auth.isAuthenticated=false → redirect /login</span></span>
<span class="line"><span>│   ├─ meta.guest=true + sudah login → redirect /dashboard</span></span>
<span class="line"><span>│   └─ meta.permission=&#39;post.index&#39; + tidak ada di auth.permissions → redirect /dashboard</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>├─ Halaman di-render oleh Vue component</span></span>
<span class="line"><span>│</span></span>
<span class="line"><span>└─ Setiap call API:</span></span>
<span class="line"><span>    ├─ api.js interceptor: inject Authorization: Bearer &lt;accessToken&gt;</span></span>
<span class="line"><span>    ├─ Response 200 → data dikembalikan ke component</span></span>
<span class="line"><span>    └─ Response 401 → auto refresh token</span></span>
<span class="line"><span>         ├─ POST /api/v1/auth/refresh</span></span>
<span class="line"><span>         ├─ Sukses: update token di localStorage + Pinia, retry request asal</span></span>
<span class="line"><span>         └─ Gagal: logout() → redirect /login</span></span></code></pre></div><hr><h2 id="_6-perbandingan-dengan-laravel" tabindex="-1">6. Perbandingan dengan Laravel <a class="header-anchor" href="#_6-perbandingan-dengan-laravel" aria-label="Permalink to &quot;6. Perbandingan dengan Laravel&quot;">​</a></h2><h3 id="request-lifecycle" tabindex="-1">Request Lifecycle <a class="header-anchor" href="#request-lifecycle" aria-label="Permalink to &quot;Request Lifecycle&quot;">​</a></h3><table tabindex="0"><thead><tr><th>Tahap</th><th>Laravel</th><th>Spravel</th></tr></thead><tbody><tr><td>Entry point</td><td><code>public/index.php</code></td><td>Tomcat embedded (Spring Boot)</td></tr><tr><td>Global middleware</td><td><code>app/Http/Kernel.php</code></td><td><code>HttpLogFilter</code></td></tr><tr><td>Auth middleware</td><td><code>auth</code> middleware</td><td><code>JwtAuthFilter</code></td></tr><tr><td>Router</td><td><code>routes/api.php</code></td><td><code>@RequestMapping</code> annotations</td></tr><tr><td>Request validation</td><td><code>FormRequest</code> / <code>$request-&gt;validate()</code></td><td><code>@Valid</code> + DTO</td></tr><tr><td>Controller</td><td><code>app/Http/Controllers</code></td><td><code>controller/</code></td></tr><tr><td>Business logic</td><td>Service class</td><td><code>service/impl/</code></td></tr><tr><td>Database</td><td>Eloquent ORM</td><td>Spring Data JPA</td></tr><tr><td>Response</td><td><code>response()-&gt;json()</code></td><td><code>ResponseBuilder.ok(data)</code></td></tr><tr><td>Error handling</td><td><code>app/Exceptions/Handler.php</code></td><td><code>GlobalExceptionHandler</code></td></tr></tbody></table><h3 id="auth-permission" tabindex="-1">Auth &amp; Permission <a class="header-anchor" href="#auth-permission" aria-label="Permalink to &quot;Auth &amp; Permission&quot;">​</a></h3><table tabindex="0"><thead><tr><th>Konsep</th><th>Laravel (Sanctum + Spatie)</th><th>Spravel</th></tr></thead><tbody><tr><td>Login</td><td><code>Auth::attempt()</code></td><td>Manual BCrypt + JWT</td></tr><tr><td>Token</td><td>Sanctum token (opaque)</td><td>JWT (self-contained)</td></tr><tr><td>Permission storage</td><td>DB table tiap request</td><td>Di-embed dalam JWT</td></tr><tr><td>Check permission</td><td><code>$user-&gt;can(&#39;slug&#39;)</code></td><td><code>@PreAuthorize(&quot;hasAuthority(&#39;slug&#39;)&quot;)</code></td></tr><tr><td>Frontend check</td><td>Props/inertia shared</td><td><code>auth.permissions.includes(&#39;slug&#39;)</code></td></tr><tr><td>Revoke</td><td><code>token()-&gt;delete()</code></td><td><code>token.setRevoked(true)</code> di DB</td></tr></tbody></table><hr><h2 id="_7-entitas-relasi-database" tabindex="-1">7. Entitas &amp; Relasi Database <a class="header-anchor" href="#_7-entitas-relasi-database" aria-label="Permalink to &quot;7. Entitas &amp; Relasi Database&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>modules (id, slug, name, description)</span></span>
<span class="line"><span>    │</span></span>
<span class="line"><span>    └─── permissions (id, slug, name, module_id)</span></span>
<span class="line"><span>              │</span></span>
<span class="line"><span>              └─── role_permissions (role_id, permission_id)  [M2M]</span></span>
<span class="line"><span>                        │</span></span>
<span class="line"><span>                        └─── roles (id, slug, name)</span></span>
<span class="line"><span>                                  │</span></span>
<span class="line"><span>                                  └─── user_roles (user_id, role_id)  [M2M]</span></span>
<span class="line"><span>                                            │</span></span>
<span class="line"><span>                                            └─── users (id, username, fullname, password)</span></span>
<span class="line"><span></span></span>
<span class="line"><span>posts (id, title, content, status, category_id, created_at, updated_at)</span></span>
<span class="line"><span>    └─── categories (id, name, description, created_at, updated_at)</span></span>
<span class="line"><span></span></span>
<span class="line"><span>tokens (id, access_token, refresh_token, token_type, username, revoked, expiry_date)</span></span>
<span class="line"><span></span></span>
<span class="line"><span>log_http (id, method, url, status, duration, ip, created_at, ...)</span></span></code></pre></div><hr><h2 id="_8-package-structure-backend" tabindex="-1">8. Package Structure Backend <a class="header-anchor" href="#_8-package-structure-backend" aria-label="Permalink to &quot;8. Package Structure Backend&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>com.fts.twin</span></span>
<span class="line"><span>├── config/          → Konfigurasi Spring (Security, WebMvc, Jackson)</span></span>
<span class="line"><span>├── controller/      → REST controller — HANYA routing + response mapping</span></span>
<span class="line"><span>├── service/</span></span>
<span class="line"><span>│   ├── {Name}Service.java         → Interface (kontrak)</span></span>
<span class="line"><span>│   └── impl/{Name}ServiceImpl.java → Implementasi (business logic)</span></span>
<span class="line"><span>├── repository/      → Spring Data JPA — HANYA query</span></span>
<span class="line"><span>├── model/           → JPA Entity — HANYA struktur data + ORM mapping</span></span>
<span class="line"><span>├── dto/</span></span>
<span class="line"><span>│   ├── request/     → Input DTO dengan @Valid annotations</span></span>
<span class="line"><span>│   ├── response/    → Output DTO (tidak expose field sensitif)</span></span>
<span class="line"><span>│   └── data/        → Internal DTO (ErrorData, dll)</span></span>
<span class="line"><span>├── middleware/      → Filter (JwtAuthFilter, HttpLogFilter)</span></span>
<span class="line"><span>├── handler/         → Exception handler + security entry points</span></span>
<span class="line"><span>├── seeder/          → Data seeder (dev/staging only)</span></span>
<span class="line"><span>└── util/            → Stateless helper (JwtUtil, ResponseBuilder, ResponseConstant)</span></span></code></pre></div>`,29)])])}const m=a(t,[["render",l]]);export{h as __pageData,m as default};
