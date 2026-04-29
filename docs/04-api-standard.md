# Standar Response API & DTO

## Aturan Utama

- **HARUS**: Semua endpoint return `ResponseEntity<ResData<T>>` via `ResponseBuilder`
- **TIDAK BOLEH**: Controller return `String`, `Map`, `Object`, atau `ResponseEntity` langsung tanpa wrapper
- **HARUS**: Error ditangani oleh `GlobalExceptionHandler`, bukan di controller
- **HARUS**: Field `data` berisi DTO yang sesuai — tidak expose entity langsung

---

## Standar Response DTO

### Aturan Umum Response DTO

- **HARUS** menggunakan `@Data` dari Lombok (getter/setter otomatis)
- **HARUS** memiliki Javadoc class-level yang menjelaskan kapan DTO ini dipakai
- **HARUS** flat structure — hindari nested object yang bisa menyebabkan circular reference
- **TIDAK BOLEH** expose field sensitif (password, token, dll)
- **HARUS** include `createdAt` dan `updatedAt` jika entity memiliki auditing

### Konvensi Penamaan

| Tipe | Format | Contoh |
|------|--------|--------|
| Response DTO | `{Entity}Response` | `PostResponse`, `UserResponse` |
| Request DTO (create) | `{Entity}Request` | `PostRequest` |
| Request DTO (update) | `Update{Entity}Request` | `UpdatePostRequest` |
| Internal DTO | `{Purpose}Data` | `ErrorData`, `RoleData` |

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

### Contoh Response DTO dengan Nested Data (Flat)

```java
/**
 * Response DTO for user listing with flattened role data.
 */
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String fullname;
    private List<RoleData> roles;

    @Data
    public static class RoleData {
        private Long id;
        private String slug;
        private String name;
    }
}
```

### Contoh Response DTO dengan Reference Flat (Module Slug)

```java
/**
 * Response DTO for permission listing and detail endpoints.
 * Module diwakili oleh moduleSlug (String) untuk menghindari circular reference
 * dengan ModuleResponse dan RoleResponse.
 */
@Data
public class PermissionResponse {
    private Long id;
    private String slug;
    private String name;
    private String moduleSlug;
}
```

### Field yang Wajib Ada di Response DTO

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | `Long` | Primary key resource |
| `createdAt` | `LocalDateTime` | Timestamp dibuat (jika entity pakai `@CreatedDate`) |
| `updatedAt` | `LocalDateTime` | Timestamp diubah (jika entity pakai `@LastModifiedDate`) |

### Field yang TIDAK Boleh Ada

| Field | Alasan |
|-------|--------|
| `password` | Keamanan — jangan pernah expose hash password |
| `token` / `accessToken` / `refreshToken` | Keamanan — token hanya untuk auth response |
| `revoked` | Internal field, tidak relevan untuk client |
| Field relasi full object | Bisa menyebabkan circular reference & infinite recursion |

---

## Kontrak Response (Data Shape)

Semua response memiliki struktur yang sama:

```typescript
// TypeScript-style untuk kejelasan
interface ResData<T> {
  status: string       // "success" (2xx) atau "error" (4xx/5xx)
  message: string      // pesan deskriptif
  data: T | null       // payload DTO, null jika tidak ada data
}
```

---

## HTTP Status Code

Kode HTTP di header sudah cukup — tidak perlu redundansi kode angka di dalam body.

| HTTP Status | Kapan Dipakai |
|-------------|---------------|
| 200 OK | Berhasil GET, PUT, DELETE |
| 201 Created | Berhasil POST (create) |
| 400 Bad Request | Request malformed |
| 401 Unauthorized | Tidak terautentikasi |
| 403 Forbidden | Tidak punya permission |
| 404 Not Found | Resource tidak ditemukan |
| 405 Method Not Allowed | Method HTTP salah |
| 422 Unprocessable Entity | Validasi input gagal |
| 500 Internal Server Error | Error tak terduga |

---

## Contoh Response Per Skenario

### GET berhasil (data tunggal)

```json
{
  "status": "success",
  "message": "Success",
  "data": {
    "id": 1,
    "title": "Hello World",
    "status": "PUBLISHED",
    "categoryId": 2,
    "categoryName": "Tech",
    "createdBy": "admin",
    "createdAt": "2026-04-21T10:00:00",
    "updatedAt": "2026-04-21T12:00:00"
  }
}
```

### GET berhasil (list)

```json
{
  "status": "success",
  "message": "Success",
  "data": [
    { "id": 1, "name": "Tech", "description": "Technology posts" },
    { "id": 2, "name": "Tutorial", "description": "How-to guides" }
  ]
}
```

### GET berhasil (paginated — Spring Page\<T\>)

```json
{
  "status": "success",
  "message": "Success",
  "data": {
    "content": [
      { "id": 1, "title": "Hello World" }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5,
    "last": false
  }
}
```

### POST berhasil (201 Created)

```json
{
  "status": "success",
  "message": "Created successfully",
  "data": {
    "id": 5,
    "name": "New Category",
    "description": "Fresh category"
  }
}
```

### DELETE berhasil (200, no data)

```json
{
  "status": "success",
  "message": "Deleted successfully",
  "data": null
}
```

### Error validasi (422)

```json
{
  "status": "error",
  "message": "Data validation error",
  "data": {
    "timestamp": "Mon Apr 21 10:00:00 WIB 2026",
    "status": "422",
    "error": "Unprocessable Entity",
    "message": "Title is required, Content must not be blank",
    "path": "/api/v1/posts"
  }
}
```

### Error tidak terautentikasi (401)

```json
{
  "status": "error",
  "message": "Authentication required",
  "data": {
    "timestamp": "...",
    "status": "401",
    "error": "Unauthorized",
    "message": "Full authentication is required",
    "path": "/api/v1/posts"
  }
}
```

### Error tidak punya akses (403)

```json
{
  "status": "error",
  "message": "Access denied",
  "data": {
    "timestamp": "...",
    "status": "403",
    "error": "Forbidden",
    "message": "Access denied",
    "path": "/api/v1/posts"
  }
}
```

### Error resource tidak ditemukan (404)

```json
{
  "status": "error",
  "message": "Post with id 99 not found",
  "data": {
    "timestamp": "...",
    "status": "404",
    "error": "Not Found",
    "message": "Post with id 99 not found",
    "path": "/api/v1/posts/99"
  }
}
```

### Error server (500)

```json
{
  "status": "error",
  "message": "An unexpected error occurred",
  "data": {
    "timestamp": "...",
    "status": "500",
    "error": "Internal Server Error",
    "message": "...",
    "path": "..."
  }
}
```

---

## `ResponseBuilder` — Cara Pakai

File: `src/main/java/com/dak/spravel/util/ResponseBuilder.java`

```java
// ✅ GET berhasil dengan data
return ResponseBuilder.ok(data);

// ✅ POST berhasil, resource dibuat
return ResponseBuilder.created(data);

// ✅ DELETE / logout berhasil, tidak ada data
return ResponseBuilder.ok();

// ✅ Error dengan kontrol penuh
return ResponseBuilder.error(HttpStatus.BAD_REQUEST, "Custom message", errorData);

// ❌ DILARANG — tidak menggunakan wrapper
return ResponseEntity.ok(someObject);
return ResponseEntity.ok(Map.of("key", "val"));
```

---

## `GlobalExceptionHandler` — Exception yang Ditangani Otomatis

File: `src/main/java/com/dak/spravel/handler/GlobalExceptionHandler.java`

| Exception Class | Trigger | HTTP |
|-----------------|---------|------|
| `MethodArgumentNotValidException` | `@Valid` gagal | 422 |
| `ResourceNotFoundException` | Resource tidak ditemukan | 404 |
| `HttpRequestMethodNotSupportedException` | Method HTTP salah | 405 |
| `NoHandlerFoundException` | Endpoint tidak ada | 404 |
| `IllegalArgumentException` | Argument tidak valid dari service | 422 |
| `AccessDeniedException` | Tidak punya permission | 403 |
| `Exception` (catch-all) | Semua error lain | 500 |

**Aturan**: Jika exception sudah ditangani `GlobalExceptionHandler`, controller **TIDAK BOLEH** punya try-catch untuk exception yang sama.

---

## `ErrorData` — Struktur Data Error

File: `src/main/java/com/dak/spravel/dto/data/ErrorData.java`

```java
public class ErrorData {
    private String timestamp;  // new Date().toString()
    private String status;     // HTTP status code sebagai String, e.g. "404"
    private String error;      // HTTP reason phrase, e.g. "Not Found"
    private String message;    // Pesan error spesifik
    private String path;       // Request URI
}
```

---

## Cara Frontend Membaca Response

File: `frontend/src/lib/api.js`

```javascript
// Success
const res = await api.get('/api/v1/posts')
const posts    = res.data.data.content        // array item
const total    = res.data.data.totalElements  // total semua record
const pages    = res.data.data.totalPages     // total halaman

// Success tunggal
const res  = await api.get('/api/v1/posts/1')
const post = res.data.data                   // object

// Error — catch di try/catch
try {
  await api.post('/api/v1/posts', payload)
} catch (err) {
  const message = err.response?.data?.message       // "Data validation error"
  const detail  = err.response?.data?.data?.message // "Title is required"
  const path    = err.response?.data?.data?.path    // "/api/v1/posts"
}
```

Pola fallback error di frontend:
```javascript
err.response?.data?.data?.message   // validasi error detail dari ErrorData
  || err.response?.data?.message    // top-level error message dari ResData
  || 'Failed to ...'                // generic fallback
```
