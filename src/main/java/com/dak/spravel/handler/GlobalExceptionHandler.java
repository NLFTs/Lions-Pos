package com.dak.spravel.handler;

import com.dak.spravel.dto.data.ErrorData;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.util.ResponseBuilder;
import com.dak.spravel.util.ResponseConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

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

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ResData<ErrorData>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.FORBIDDEN, ResponseConstant.MSG_FORBIDDEN, request);
        return ResponseBuilder.error(HttpStatus.FORBIDDEN, ResponseConstant.MSG_FORBIDDEN, errorData);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResData<ErrorData>> handleException(Exception ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseConstant.MSG_SERVER_ERROR, errorData);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResData<ErrorData>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        ErrorData errorData = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return ResponseBuilder.error(HttpStatus.BAD_REQUEST, ex.getMessage(), errorData);
    }
}
