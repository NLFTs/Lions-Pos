package com.dak.spravel.util;

import com.dak.spravel.dto.response.ResData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Static utility for building standardized ResponseEntity with ResData wrapper.
 */
public class ResponseBuilder {
    
    

    public static <T> ResponseEntity<ResData<T>> ok(T data) {
        return ResponseEntity.ok(ResData.of(data));
    }

    public static <T> ResponseEntity<ResData<T>> ok(String message, T data) {
        return ResponseEntity.ok(ResData.of(message, data));
    }

    public static ResponseEntity<ResData<Void>> ok() {
        return ResponseEntity.ok(ResData.noContent());
    }

    public static <T> ResponseEntity<ResData<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResData.created(data));
    }

    public static <T> ResponseEntity<ResData<T>> error(int code, String message, T data) {
        return ResponseEntity.status(code).body(ResData.error(code, message, data));
    }

    public static <T> ResponseEntity<ResData<T>> error(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(ResData.error(status.value(), message, data));
    }
}
