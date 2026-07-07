package com.fts.twin.controller.catalog;

import com.fts.twin.dto.request.catalog.CategoryProductCreate;
import com.fts.twin.dto.response.ResData;
import com.fts.twin.dto.response.catalogresponse.CategoryProductResponse;
import com.fts.twin.service.catalog.CategoryProductService;
import com.fts.twin.util.ResponseBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryProductController {

    private final CategoryProductService categoryProductService;

    @GetMapping("/admin")
    public ResponseEntity<ResData<Page<CategoryProductResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/categories/admin - Superadmin access, page: {}, size: {}", page, size);
        return ResponseBuilder.ok(categoryProductService.findAllCategoryProduct(page, size));
    }
    

    @GetMapping
    @PreAuthorize("hasAuthority('category.index')")
    public ResponseEntity<ResData<List<CategoryProductResponse>>> index() {
        log.info("[GET] /api/v1/categories");
        return ResponseBuilder.ok(categoryProductService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('category.index')")
    public ResponseEntity<ResData<Page<CategoryProductResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/categories/page page={} size={}", page, size);
        return ResponseBuilder.ok(categoryProductService.findAll(page, size));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('category.store')")
    public ResponseEntity<ResData<CategoryProductResponse>> store(
            @Valid @RequestBody CategoryProductCreate request) {
        log.info("[POST] /api/v1/categories name={}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBuilder.created(categoryProductService.create(request)).getBody());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category.update')")
    public ResponseEntity<ResData<CategoryProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryProductCreate request) {
        log.info("[PUT] /api/v1/categories/{}", id);
        return ResponseBuilder.ok(categoryProductService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/categories/{}", id);
        categoryProductService.delete(id);
        return ResponseEntity.noContent().build();
    }


}