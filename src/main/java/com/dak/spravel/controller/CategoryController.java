package com.dak.spravel.controller;

import com.dak.spravel.dto.request.CreateCategoryRequest;
import com.dak.spravel.dto.request.UpdateCategoryRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.CategoryResponse;
import com.dak.spravel.service.CategoryService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Provides full CRUD operations for categories with pagination support.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * GET /api/v1/categories
     * List all categories ordered by name.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('category.index')")
    public ResponseEntity<ResData<List<CategoryResponse>>> index() {
        log.info("[GET] /api/v1/categories");
        return ResponseBuilder.ok(categoryService.findAll());
    }

    /**
     * GET /api/v1/categories/page
     * Get paginated categories.
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('category.index')")
    public ResponseEntity<ResData<Page<CategoryResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/categories/page page={} size={}", page, size);
        return ResponseBuilder.ok(categoryService.findAll(page, size));
    }

    /**
     * GET /api/v1/categories/{id}
     * Get a single category by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('category.show')")
    public ResponseEntity<ResData<CategoryResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/categories/{}", id);
        return ResponseBuilder.ok(categoryService.findById(id));
    }

    /**
     * POST /api/v1/categories
     * Create a new category.
     */
    @PostMapping
@PreAuthorize("hasAuthority('category.store')")
    public ResponseEntity<ResData<CategoryResponse>> store(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("[POST] /api/v1/categories name={}", request.getName());
        return ResponseBuilder.created(categoryService.create(request));
    }

    /**
     * PUT /api/v1/categories/{id}
     * Update an existing category by ID.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category.update')")
    public ResponseEntity<ResData<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("[PUT] /api/v1/categories/{}", id);
        return ResponseBuilder.ok(categoryService.update(id, request));
    }

    /**
     * DELETE /api/v1/categories/{id}
     * Delete a category by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category.delete')")
    public ResponseEntity<ResData<Void>> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/categories/{}", id);
        categoryService.delete(id);
        return ResponseBuilder.ok();
    }
}
