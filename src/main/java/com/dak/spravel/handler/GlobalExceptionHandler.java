package com.dak.spravel.handler;

import com.dak.spravel.dto.data.ErrorData;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.util.ResponseBuilder;
import com.dak.spravel.util.ResponseConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for validation, not found, and server errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private ErrorData buildError(HttpStatus status, String message, HttpServletRequest request) {
        ErrorData errorData = new ErrorData();
        errorData.setTimestamp(new Date().toString());
        errorData.setStatus(String.valueOf(status.value()));
        errorData.setError(status.getReasonPhrase());
        errorData.setMessage(message);
        errorData.setPath(request.getRequestURI());
        return errorData;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResData<ErrorData>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ErrorData errorData = buildError(HttpStatus.UNPROCESSABLE_ENTITY, message, request);
        return ResponseBuilder.error(HttpStatus.UNPROCESSABLE_ENTITY, ResponseConstant.MSG_VALIDATION_ERROR, errorData);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResData<ErrorData>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.NOT_FOUND, ex.getMessage(), errorData);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResData<ErrorData>> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), errorData);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResData<ErrorData>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.NOT_FOUND, ResponseConstant.MSG_NOT_FOUND, errorData);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResData<ErrorData>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), errorData);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResData<ErrorData>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.FORBIDDEN, ResponseConstant.MSG_FORBIDDEN, request);
        return ResponseBuilder.error(HttpStatus.FORBIDDEN, ResponseConstant.MSG_FORBIDDEN, errorData);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResData<ErrorData>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.BAD_REQUEST, ex.getMessage(), errorData);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResData<ErrorData>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String originalMessage = ex.getMessage();
        String customMessage = "Gagal memproses data akibat pelanggaran relasi di database.";
    
        if (originalMessage != null && originalMessage.contains("violates foreign key constraint")) {
            
            Pattern pattern = Pattern.compile("on table \"([^\"]+)\"");
            Matcher matcher = pattern.matcher(originalMessage);
            
            if (matcher.find()) {
                String tableName = matcher.group(1); 
                
                String readableTable = tableName.replace("_", " ").toUpperCase();
                
                customMessage = "Akses Ditolak: Pengguna tidak dapat dihapus karena data akun masih terikat dengan riwayat transaksi aktif di bagian [" + readableTable + "].";
            } else {
                customMessage = "Akses Ditolak: Pengguna tidak dapat dihapus karena datanya masih terikat dengan transaksi lain di dalam sistem.";
            }
        }
    
        ErrorData errorData = buildError(HttpStatus.BAD_REQUEST, customMessage, request);
        return ResponseBuilder.error(HttpStatus.BAD_REQUEST, customMessage, errorData);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResData<ErrorData>> handleException(Exception ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseConstant.MSG_SERVER_ERROR, errorData);
    }
}
