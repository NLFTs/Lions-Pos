package com.dak.spravel.controller.catalog;

import com.dak.spravel.dto.request.catalog.CategoryProductCreate;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.service.catalog.CategoryProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/category-products")
@RequiredArgsConstructor
public class CategoryProductController {

    private final CategoryProductService categoryProductService;

    @GetMapping
    @PreAuthorize("hasAuthority('category_product.index')")
    public ResponseEntity<List<CategoryProduct>> index() {
        log.info("[GET] /api/v1/category-products");
        return ResponseEntity.ok(categoryProductService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('category_product.index')")
    public ResponseEntity<Page<CategoryProduct>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/category-products/page page={} size={}", page, size);
        return ResponseEntity.ok(categoryProductService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('category_product.show')")
    public ResponseEntity<CategoryProduct> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/category-products/{}", id);
        return ResponseEntity.ok(categoryProductService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('category_product.store')")
    public ResponseEntity<CategoryProduct> store(
            @Valid @RequestBody CategoryProductCreate request) {
        log.info("[POST] /api/v1/category-products name={}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryProductService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category_product.update')")
    public ResponseEntity<CategoryProduct> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryProductCreate request) {
        log.info("[PUT] /api/v1/category-products/{}", id);
        return ResponseEntity.ok(categoryProductService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category_product.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/category-products/{}", id);
        categoryProductService.delete(id);
        return ResponseEntity.noContent().build();
    }
}