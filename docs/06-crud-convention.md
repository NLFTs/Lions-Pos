# Konvensi CRUD — Panduan Lengkap

## Aturan Utama

- **HARUS**: Setiap resource baru mengikuti urutan: Model → Repository → Service → Controller → DTO
- **HARUS**: Controller HANYA routing + delegate ke Service + return `ResponseBuilder`
- **TIDAK BOLEH**: Controller akses Repository langsung
- **TIDAK BOLEH**: Return Entity langsung — HARUS mapping ke Response DTO dulu
- **HARUS**: Setiap endpoint punya `@PreAuthorize("hasAuthority('slug')")` sesuai permission
- **HARUS**: Semua model punya field `createdAt` dan `updatedAt`

---

## Langkah Membuat Resource Baru

Gunakan `User` sebagai contoh di bawah. Ganti `User/user/users` sesuai nama resource baru.

### Langkah 1 — Model (Entity)

File: `src/main/java/com/dak/spravel/model/User.java`

```java
@Data
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false)
    private String password;  // BCrypt hash

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Aturan model:**
- `@EntityListeners(AuditingEntityListener.class)` HARUS ada jika pakai `@CreatedDate`/`@LastModifiedDate`
- Relasi pakai `FetchType.LAZY` by default
- Field sensitif (password, token) TIDAK BOLEH ada di Response DTO

---

### Langkah 2 — Repository

File: `src/main/java/com/dak/spravel/repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // Pagination query custom jika dibutuhkan
    Page<User> findByFullnameContainingIgnoreCase(String keyword, Pageable pageable);
}
```

**Aturan repository:**
- Hanya query — tidak ada business logic
- Gunakan Spring Data method naming untuk query sederhana
- Gunakan `@Query("...")` hanya jika method naming tidak cukup

---

### Langkah 3 — DTO

#### 3a. Request DTO — Create

File: `src/main/java/com/dak/spravel/dto/request/UserRequest.java`

```java
@Data
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotBlank(message = "Full name is required")
    private String fullname;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotEmpty(message = "At least one role is required")
    private List<Long> roleIds;
}
```

#### 3b. Request DTO — Update

File: `src/main/java/com/dak/spravel/dto/request/UpdateUserRequest.java`

```java
@Data
public class UpdateUserRequest {
    // Semua field nullable — partial update
    @Size(min = 3, max = 50)
    private String fullname;

    @Size(min = 8)
    private String password;  // null = tidak ganti password

    private List<Long> roleIds;  // null = tidak ganti role
}
```

#### 3c. Response DTO

File: `src/main/java/com/dak/spravel/dto/response/UserResponse.java`

```java
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String fullname;
    private List<String> roles;      // slug role saja
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // ❌ TIDAK ADA field password
}
```

---

### Langkah 4 — Service

File: `src/main/java/com/dak/spravel/service/UserService.java`

```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Page<UserResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public UserResponse findById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullname(request.getFullname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(resolveRoles(request.getRoleIds()));
        return toResponse(userRepository.save(user));
    }

    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (request.getFullname() != null) user.setFullname(request.getFullname());
        if (request.getPassword() != null) user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRoleIds() != null)  user.setRoles(resolveRoles(request.getRoleIds()));
        return toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException("User", id);
        userRepository.deleteById(id);
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        return roleIds.stream()
                .map(rid -> roleRepository.findById(rid)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", rid)))
                .collect(Collectors.toSet());
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setFullname(user.getFullname());
        res.setRoles(user.getRoles().stream().map(Role::getSlug).sorted().toList());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }
}
```

---

### Langkah 5 — Controller

File: `src/main/java/com/dak/spravel/controller/UserController.java`

```java
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * GET /api/v1/users
     * List all users.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user.index')")
    public ResponseEntity<ResData<Page<UserResponse>>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/users page={} size={}", page, size);
        return ResponseBuilder.ok(userService.findAll(page, size));
    }

    /**
     * GET /api/v1/users/{id}
     * Get details of a specific user by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.show')")
    public ResponseEntity<ResData<UserResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/users/{}", id);
        return ResponseBuilder.ok(userService.findById(id));
    }

    /**
     * POST /api/v1/users
     * Create a new user.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user.store')")
    public ResponseEntity<ResData<UserResponse>> store(
            @Valid @RequestBody CreateUserRequest request) {
        log.info("[POST] /api/v1/users username={}", request.getUsername());
        return ResponseBuilder.created(userService.create(request));
    }

    /**
     * PUT /api/v1/users/{id}
     * Update an existing user's information by ID.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<ResData<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("[PUT] /api/v1/users/{}", id);
        return ResponseBuilder.ok(userService.update(id, request));
    }

    /**
     * DELETE /api/v1/users/{id}
     * Delete a user by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user.delete')")
    public ResponseEntity<ResData<Void>> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/users/{}", id);
        userService.delete(id);
        return ResponseBuilder.ok();
    }
}
```

**Aturan controller:**
- Log format HARUS `[METHOD] /api/v1/path param=val` di setiap method
- `@PreAuthorize` HARUS ada di setiap method
- TIDAK BOLEH ada `try-catch` — biarkan `GlobalExceptionHandler` tangkap

---

### Langkah 6 — Daftarkan Permission di Seeder

File: `src/main/java/com/dak/spravel/seeder/MainSeeder.java`

Tambahkan permission slugs untuk resource baru:

```java
// Daftar slug yang harus ada untuk resource User
List<String> userPermissions = List.of(
    "user.index", "user.show", "user.store", "user.update", "user.delete"
);
// Tambahkan ke module "User Management"
```

---

### Langkah 7 — Frontend: Page Vue

File: `frontend/src/pages/UsersPage.vue`

```vue
<script setup>
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { usePermission } from '@/composables/usePermission'
import api from '@/lib/api'

const { can } = usePermission()

const users   = ref([])
const loading = ref(false)
const page    = ref(0)
const total   = ref(0)

async function fetchAll() {
  loading.value = true
  try {
    const res = await api.get(`/api/v1/users?page=${page.value}&size=10`)
    users.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

async function deleteUser(id) {
  if (!confirm('Hapus user ini?')) return
  try {
    await api.delete(`/api/v1/users/${id}`)
    await fetchAll()
  } catch (err) {
    console.error('Failed to delete:', err.response?.data?.message)
  }
}

onMounted(fetchAll)
</script>

<template>
  <AppLayout>
    <div class="p-6">
      <div class="flex items-center justify-between mb-4">
        <h1 class="text-xl font-bold">Manajemen User</h1>
        <button v-if="can('user.store')" @click="openCreate">+ Tambah User</button>
      </div>

      <table>
        <thead>
          <tr>
            <th>Username</th>
            <th>Full Name</th>
            <th>Roles</th>
            <th>Aksi</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.username }}</td>
            <td>{{ user.fullname }}</td>
            <td>{{ user.roles.join(', ') }}</td>
            <td>
              <button v-if="can('user.update')" @click="openEdit(user)">Edit</button>
              <button v-if="can('user.delete')" @click="deleteUser(user.id)">Hapus</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AppLayout>
</template>
```

---

### Langkah 8 — Daftarkan Route Frontend

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
    pageSubtitle: 'Kelola user dan assign role.',
  }
}
```

---

## Ringkasan Checklist Resource Baru

```
Backend:
  [ ] Model (Entity) di model/
  [ ] Repository di repository/
  [ ] Service class di service/
  [ ] Request DTO (create) di dto/request/
  [ ] Request DTO (update, nullable fields) di dto/request/
  [ ] Response DTO (tanpa field sensitif) di dto/response/
  [ ] Controller dengan @PreAuthorize di setiap endpoint
  [ ] Tambah permission slugs di seeder

Frontend:
  [ ] Page.vue di frontend/src/pages/
  [ ] Route di frontend/src/router/index.js dengan meta.permission
  [ ] Sidebar item di AppLayout.vue dengan v-if="can('resource.index')"
```

---

## Konvensi Penamaan

| Tipe | Format | Contoh |
|------|--------|--------|
| Entity class | `PascalCase` | `PostCategory` |
| Table name | `snake_case` plural | `post_categories` |
| Column name | `snake_case` | `created_at`, `user_id` |
| Repository | `{Entity}Repository` | `PostCategoryRepository` |
| Service | `{Entity}Service` | `PostCategoryService` |
| Controller | `{Entity}Controller` | `PostCategoryController` |
| Request DTO create | `{Entity}Request` | `PostCategoryRequest` |
| Request DTO update | `Update{Entity}Request` | `UpdatePostCategoryRequest` |
| Response DTO | `{Entity}Response` | `PostCategoryResponse` |
| API path | `/api/v1/{plural-kebab}` | `/api/v1/post-categories` |
| Permission slug | `{modul}.{aksi}` | `post-category.store` |
| Vue page | `{Entity}sPage.vue` | `PostCategoriesPage.vue` |

---

## Annotation Validasi yang Tersedia

| Annotation | Keterangan | Contoh |
|------------|------------|--------|
| `@NotNull` | Tidak boleh null | Field wajib tapi boleh string kosong |
| `@NotBlank` | Tidak boleh null atau hanya whitespace | Field teks wajib isi |
| `@NotEmpty` | Tidak boleh null atau collection kosong | List roleIds wajib |
| `@Size(min, max)` | Panjang string atau collection | `@Size(min=3, max=255)` |
| `@Min(n)` / `@Max(n)` | Nilai numerik | `@Min(1)` untuk page |
| `@Email` | Format email valid | Field email |
| `@Pattern(regexp)` | Custom regex | `@Pattern(regexp="^(DRAFT|PUBLISHED)$")` |
| `@Positive` | Harus > 0 | ID, quantity |

Semua message error HARUS diisi: `@NotBlank(message = "Field X is required")`

Jika validation gagal → `GlobalExceptionHandler` tangkap `MethodArgumentNotValidException` → return 422.
