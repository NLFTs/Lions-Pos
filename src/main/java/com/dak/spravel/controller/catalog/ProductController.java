package com.dak.spravel.controller.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.catalogresponse.ProductResponse;
import com.dak.spravel.service.catalog.ProductService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('produk.store')")
    public ResponseEntity<ResData<ProductResponse>> create(@RequestBody ProductRequest request) {
        log.info("[POST] /api/v1/products - Request: {}", request.getName());
        return ResponseBuilder.ok(productService.create(request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('produk.index')")
    public ResponseEntity<ResData<Page<ProductResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/products/admin - Superadmin access, page: {}, size: {}", page, size);
        return ResponseBuilder.ok(productService.findAllProduct(page, size));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('produk.index')")
    public ResponseEntity<ResData<Page<ProductResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/products - page: {}, size: {}", page, size);
        return ResponseBuilder.ok(productService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('produk.show')")
    public ResponseEntity<ResData<ProductResponse>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/products/{}", id);
        return ResponseBuilder.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('produk.update')")
    public ResponseEntity<ResData<ProductResponse>> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        log.info("[PUT] /api/v1/products/{}", id);
        return ResponseBuilder.ok(productService.patchProduct(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('produk.update')")
    public ResponseEntity<ResData<ProductResponse>> patch(@PathVariable Long id, @RequestBody ProductRequest request) {
        log.info("[PATCH] /api/v1/products/{}", id);
        return ResponseBuilder.ok(productService.patchProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('produk.delete')")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/products/{}", id);
        productService.delete(id);
        return ResponseBuilder.ok("Product deleted successfully");
    }

    @PutMapping("/soft-delete/{id}") // Pake PATCH lebih tepat buat update partial
    @PreAuthorize("hasAuthority('produk.update')")
    public ResponseEntity<ResData<ProductResponse>> softDelete(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/soft-delete/{}", id);
        return ResponseBuilder.ok(productService.softDeleteProduct(id));
    }

    @PutMapping("/restore/{id}")
    @PreAuthorize("hasAuthority('produk.update')")
    public ResponseEntity<ResData<ProductResponse>> restore(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/restore/{}", id);
        return ResponseBuilder.ok(productService.restoreProduct(id));
    }

    @PutMapping("/track-stock/enable/{id}")
    @PreAuthorize("hasAuthority('produk.update')")
    public ResponseEntity<ResData<ProductResponse>> setTrueTrackStock(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/{}/track-stock/enable", id);
        return ResponseBuilder.ok(productService.setTrueTrackStock(id));
    }

    @PutMapping("/track-stock/disable/{id}")
    @PreAuthorize("hasAuthority('produk.update')")
    public ResponseEntity<ResData<ProductResponse>> setFalseTrackStock(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/products/{}/track-stock/disable", id);
        return ResponseBuilder.ok(productService.setFalseTrackStock(id));
    }
}