package com.fts.twin.controller.catalog;

import com.fts.twin.dto.request.catalog.ProductPhotoRequestDTO;
import com.fts.twin.model.catalog.ProductPhoto;
import com.fts.twin.service.catalog.ProductPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
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