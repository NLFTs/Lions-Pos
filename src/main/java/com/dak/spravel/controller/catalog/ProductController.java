package com.dak.spravel.controller.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.dto.response.ResData;
<<<<<<< HEAD
import com.dak.spravel.model.auth.User;
=======
import com.dak.spravel.dto.response.catalogresponse.ProductResponse;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.service.catalog.ProductService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
=======
import org.springframework.security.access.prepost.PreAuthorize;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<ResData<Product>> create(@RequestBody ProductRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[POST] /api/v1/products - Request: {}", request);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        Product product = productService.create(request, partnerId);
        return ResponseBuilder.ok(product);
    }

    @GetMapping
    public ResponseEntity<ResData<List<Product>>> findAll(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("[GET] /api/v1/products");
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        List<Product> products = productService.findAll(partnerId);
        return ResponseBuilder.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResData<Product>> findById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[GET] /api/v1/products/{}", id);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        Product product = productService.findById(id, partnerId);
        return ResponseBuilder.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResData<Product>> update(@PathVariable Long id, @RequestBody ProductRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[PUT] /api/v1/products/{}", id);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        Product product = productService.updateProduct(id, request, partnerId);
        return ResponseBuilder.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
=======
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
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
        log.info("[DELETE] /api/v1/products/{}", id);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        productService.delete(id, partnerId);
        return ResponseBuilder.ok("Product deleted successfully");
    }

<<<<<<< HEAD
    @PutMapping("/soft-delete/{id}")
    public ResponseEntity<ResData<Product>> softDelete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[PATCH] /api/v1/products/{}", id);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        Product product = productService.softDeleteProduct(id, partnerId);
        return ResponseBuilder.ok(product);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<ResData<Product>> restore(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[PATCH] /api/v1/products/restore/{}", id);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        Product product = productService.restoreProduct(id, partnerId);
        return ResponseBuilder.ok(product);
    }

    @PutMapping("/set-true-track-stock/{id}")
    public ResponseEntity<ResData<Product>> setTrueTrackStock(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[PATCH] /api/v1/products/set-true-track-stock/{}", id);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        Product product = productService.setTrueTrackStock(id, partnerId);
        return ResponseBuilder.ok(product);
    }

    @PutMapping("/set-false-track-stock/{id}")
    public ResponseEntity<ResData<Product>> setFalseTrackStock(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[PATCH] /api/v1/products/set-false-track-stock/{}", id);
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Long partnerId = user.getPartner().getId();
        Product product = productService.setFalseTrackStock(id, partnerId);
        return ResponseBuilder.ok(product);
=======
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
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    }
}