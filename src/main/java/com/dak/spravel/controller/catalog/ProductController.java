package com.dak.spravel.controller.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.catalogresponse.ProductResponse;
import com.dak.spravel.service.catalog.ProductService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('product.store')")
    public ResponseEntity<ResData<ProductResponse>> create(@RequestBody ProductRequest request) {
        log.info("[POST] /api/v1/products - Request: {}", request.getName());
        return ResponseBuilder.ok(productService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('product.index')")
    public ResponseEntity<ResData<List<ProductResponse>>> findAll() {
        log.info("[GET] /api/v1/products");
        return ResponseBuilder.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product.show')")
    public ResponseEntity<ResData<ProductResponse>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/products/{}", id);
        return ResponseBuilder.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product.update')")
    public ResponseEntity<ResData<ProductResponse>> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        log.info("[PUT] /api/v1/products/{}", id);
        return ResponseBuilder.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product.delete')")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/products/{}", id);
        productService.delete(id);
        return ResponseBuilder.ok("Product deleted successfully");
    }

    @PutMapping("/soft-delete/{id}") // Pake PATCH lebih tepat buat update partial
    @PreAuthorize("hasAuthority('product.update')")
    public ResponseEntity<ResData<ProductResponse>> softDelete(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/soft-delete/{}", id);
        return ResponseBuilder.ok(productService.softDeleteProduct(id));
    }

    @PutMapping("/restore/{id}")
    @PreAuthorize("hasAuthority('product.update')")
    public ResponseEntity<ResData<ProductResponse>> restore(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/restore/{}", id);
        return ResponseBuilder.ok(productService.restoreProduct(id));
    }

    @PutMapping("/track-stock/enable/{id}")
    @PreAuthorize("hasAuthority('product.update')")
    public ResponseEntity<ResData<ProductResponse>> setTrueTrackStock(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/{}/track-stock/enable", id);
        return ResponseBuilder.ok(productService.setTrueTrackStock(id));
    }

    @PutMapping("/track-stock/disable/{id}")
    @PreAuthorize("hasAuthority('product.update')")
    public ResponseEntity<ResData<ProductResponse>> setFalseTrackStock(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/{}/track-stock/disable", id);
        return ResponseBuilder.ok(productService.setFalseTrackStock(id));
    }
}