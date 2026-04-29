# Coding Rules & Conventions

> Dokumen ini berisi kumpulan aturan penulisan kode tambahan yang akan terus diperbarui secara bertahap
> seiring dengan berjalannya development project ini. Semua developer harus mematuhi aturan berikut.

---

## 1. Comment & Dokumentasi

- **DILARANG memakai divider ASCII (`======`)**: Dilarang merekayasa *header* / *divider* komentar menggunakan banyak tanda sama dengan (`====================================`). Hal ini membuat kode tidak rapi dan memakan banyak tempat visual. Gunakan saja komentar biasa, jdoc, atau Markdown standard bila diperlukan.

## 2. Dependency Injection

- **Wajib menggunakan `@RequiredArgsConstructor`**: Untuk dependency injection (field private final), dilarang menulis constructor manual. Gunakan annotasi `@RequiredArgsConstructor` dari Lombok untuk menjaga kode tetap ringkas dan konsisten (Constructor-based Injection).
- **Gunakan `private final`**: Semua service/repository yang dipanggil di class lain harus di-set sebagai `private final` agar otomatis masuk ke constructor Lombok.

## 3. Formatting

- **Satu Baris Kosong Antar Fungsi**: Pastikan ada **satu baris kosong** (blank line) di antara penutup fungsi `}` dan pembuka fungsi berikutnya. Hal ini untuk menjaga keterbacaan (readability) kode.

## 4. Controller Documentation

- **Wajib menggunakan Javadoc Comment pada setiap Mapping Method**: Setiap method di Controller yang memiliki annotasi `@GetMapping`, `@PostMapping`, dsb, wajib memiliki komentar Javadoc di atasnya.
- **Format**: Baris pertama berisi `[METHOD] [URL]`, baris kedua berisi deskripsi singkat fungsi tersebut.
    ```java
    /**
     * PUT /api/v1/modules/{id}
     * Updates name and description only. Slug is immutable.
     */
    ```

## 5. API Response Format

- **Wajib menggunakan `ResData<T>`**: Semua endpoint API wajib mengembalikan objek `ResponseEntity<ResData<T>>` dimana T adalah DTO.
- **Struktur Standard**: Response harus memiliki 3 field utama:
  - `status` (String): Label status, yaitu `"success"` (untuk 2xx) atau `"error"` (untuk 4xx/5xx).
  - `message` (String): Pesan deskriptif.
  - `data` (T): Objek data utama atau `null`. Field ini berisi DTO yang sesuai dengan operasi.
- **Cara Pakai**:
  - GET list: `ResponseEntity.ok(ResData.of(listDto))`
  - GET single: `ResponseEntity.ok(ResData.of(dto))`
  - POST: `ResponseEntity.status(HttpStatus.CREATED).body(ResData.created(dto))`
  - PUT: `ResponseEntity.ok(ResData.of(dto))`
  - DELETE: `service.delete(id); return ResponseEntity.ok(ResData.deleted());`
- **Kelebihan**: Fokus pada kejelasan status operasional tanpa redundansi kode angka di dalam body JSON (karena kode angka sudah ada di HTTP Header).

## 6. Comment Style untuk Non-Controller REST

- **Service Class**: Gunakan komentar Javadoc pada public method yang berisi logika bisnis. Format: baris pertama menjelaskan apa yang dilakukan method, baris kedua menjelaskan parameter khusus atau catatan penting (misalnya validasi atau side-effect).
  ```java
  /**
   * Creates a new user with encrypted password.
   * Throws IllegalArgumentException if username already exists.
   */
  public UserResponse create(CreateUserRequest request) { ... }
  ```
- **Repository Interface**: Dilarang menambahkan Javadoc pada method bawaan Spring Data JPA (findById, findAll, save, delete). Cukup tambahkan komentar pada custom query method (misalnya `@Query` atau method dengan nama yang tidak standar).
  ```java
  // Finds all active tokens for a given username
  List<Token> findAllByUsername(String username);
  ```
- **DTO Class**: Tambahkan Javadoc pada class level yang menjelaskan kapan DTO ini dipakai. Field-level comment hanya diperlukan jika nama field tidak cukup menjelaskan maksudnya.
  ```java
  /**
   * Response DTO for user listing endpoints.
   * Contains flattened role data to avoid circular reference.
   */
  public class UserResponse { ... }
  ```
- **Model/Entity Class**: Tidak wajib diberi Javadoc jika nama tabel dan field sudah self-explanatory. Tambahkan komentar hanya pada field yang memiliki logika khusus (misalnya `@PrePersist`, computed field, atau relasi yang tidak umum).

*(Aturan-aturan lainnya akan ditambahkan di sini...)*
