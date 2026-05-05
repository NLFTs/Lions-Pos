package com.dak.spravel.dto.data;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response DTO used by GlobalExceptionHandler for structured error responses.
 */
@Data
@NoArgsConstructor
public class ErrorData {
    private String timestamp;
    private String status;
    private String error;
    private String message;
    private String path;
}
