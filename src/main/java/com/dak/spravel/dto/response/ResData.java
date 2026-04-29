package com.dak.spravel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response wrapper.
 * Semua endpoint API mengembalikan ResponseEntity&lt;ResData&lt;T&gt;&gt; dimana T adalah DTO.
 *
 * Contoh JSON output:
 * {
 *   "status": "success",
 *   "message": "Success",
 *   "data": { ... }
 * }
 *
 * Contoh penggunaan di controller:
 * // GET single resource
 * return ResponseEntity.ok(ResData.of(dto));
 *
 * // GET list
 * return ResponseEntity.ok(ResData.of(listDto));
 *
 * // POST - 201
 * return ResponseEntity.status(HttpStatus.CREATED).body(ResData.created(dto));
 *
 * // DELETE - 200 no data
 * service.delete(id);
 * return ResponseEntity.ok(ResData.deleted());
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResData<T> {

    private String status;
    private String message;
    private T data;

    // Success with data
    public static <T> ResData<T> of(T data) {
        return ResData.<T>builder()
                .status("success")
                .message("Success")
                .data(data)
                .build();
    }

    // Success with custom message and data
    public static <T> ResData<T> of(String message, T data) {
        return ResData.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }

    // Success, no data (e.g. DELETE, void operations)
    public static ResData<Void> noContent() {
        return ResData.<Void>builder()
                .status("success")
                .message("Success")
                .build();
    }

    // Created (201) with data
    public static <T> ResData<T> created(T data) {
        return ResData.<T>builder()
                .status("success")
                .message("Created successfully")
                .data(data)
                .build();
    }

    // Deleted (200) confirmation
    public static ResData<Void> deleted() {
        return ResData.<Void>builder()
                .status("success")
                .message("Deleted successfully")
                .build();
    }

    // Error response
    public static <T> ResData<T> error(int code, String message, T data) {
        return ResData.<T>builder()
                .status(code >= 200 && code < 300 ? "success" : "error")
                .message(message)
                .data(data)
                .build();
    }
}
