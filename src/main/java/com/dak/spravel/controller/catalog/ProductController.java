package com.dak.spravel.controller.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.service.catalog.ProductService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ResData<Product>> create(@RequestBody ProductRequest request) {
        log.info("[POST] /api/v1/products - Request: {}", request);
        Product product = productService.create(request);
        return ResponseBuilder.ok(product);
    }

    @GetMapping
    public ResponseEntity<ResData<List<Product>>> findAll() {
        log.info("[GET] /api/v1/products");
        List<Product> products = productService.findAll();
        return ResponseBuilder.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResData<Product>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/products/{}", id);
        Product product = productService.findById(id);
        return ResponseBuilder.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/products/{}", id);
        productService.delete(id);
        return ResponseBuilder.ok("Product deleted successfully");
    }
}