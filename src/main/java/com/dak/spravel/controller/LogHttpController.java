package com.dak.spravel.controller;

import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.service.system.LogHttpService;
import com.dak.spravel.dto.response.LogHttpResponse;
import com.dak.spravel.util.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * Read-only controller for HTTP audit logs with pagination support.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogHttpController {
    private final LogHttpService logHttpService;

    /**
     * GET /api/v1/logs
     * List all HTTP logs with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('log.index')")
    public ResponseEntity<ResData<Page<LogHttpResponse>>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("[GET] /api/v1/logs page={} size={}", page, size);
        return ResponseBuilder.ok(logHttpService.findAll(page, size));
    }

    /**
     * GET /api/v1/logs/{id}
     * Get details of a specific HTTP log entry by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('log.show')")
    public ResponseEntity<ResData<LogHttpResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/logs/{}", id);
        return ResponseBuilder.ok(logHttpService.findById(id));
    }
}
