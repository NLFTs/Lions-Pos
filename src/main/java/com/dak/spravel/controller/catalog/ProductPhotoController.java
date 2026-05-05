package com.dak.spravel.controller.catalog;

import com.dak.spravel.dto.request.catalog.ProductPhotoRequestDTO;
import com.dak.spravel.model.catalog.ProductPhoto;
import com.dak.spravel.service.catalog.ProductPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/product-photos")
@RequiredArgsConstructor
public class ProductPhotoController {

    private final ProductPhotoService productPhotoService;

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('product_photo.index')")
    public ResponseEntity<List<ProductPhoto>> getByProduct(@PathVariable Long productId) {
        log.info("[GET] /api/v1/product-photos/product/{}", productId);
        return ResponseEntity.ok(productPhotoService.findByProductId(productId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product_photo.show')")
    public ResponseEntity<ProductPhoto> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/product-photos/{}", id);
        return ResponseEntity.ok(productPhotoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('product_photo.store')")
    public ResponseEntity<ProductPhoto> store(
            @Valid @RequestBody ProductPhotoRequestDTO request) {
        log.info("[POST] /api/v1/product-photos productId={}", request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productPhotoService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product_photo.update')")
    public ResponseEntity<ProductPhoto> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductPhotoRequestDTO request) {
        log.info("[PUT] /api/v1/product-photos/{}", id);
        return ResponseEntity.ok(productPhotoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product_photo.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/product-photos/{}", id);
        productPhotoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}