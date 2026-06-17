package com.dak.spravel.controller.order;

import com.dak.spravel.dto.request.order.OrderItemsRequest;
import com.dak.spravel.model.order.OrderItems;
import com.dak.spravel.service.order.OrderItemsService;
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
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemsController {

    private final OrderItemsService orderItemsService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('order_item.index')")
    public ResponseEntity<List<OrderItems>> getAllForAdmin() {
        log.info("[GET] /api/v1/order-items/admin - Superadmin access");
        return ResponseEntity.ok(orderItemsService.findAllOrderItems());
    }

    @PreAuthorize("hasAuthority('order_item.index')")
    public ResponseEntity<List<OrderItems>> index() {
        log.info("[GET] /api/v1/order-items");
        return ResponseEntity.ok(orderItemsService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order_item.show')")
    public ResponseEntity<OrderItems> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/order-items/{}", id);
        return ResponseEntity.ok(orderItemsService.findById(id));
    }

    @GetMapping("/product/{productName}")
    @PreAuthorize("hasAuthority('order_item.show')")
    public ResponseEntity<OrderItems> findByProductName(
            @PathVariable String productName) {
        log.info("[GET] /api/v1/order-items/product/{}", productName);
        return ResponseEntity.ok(orderItemsService.findByProductName(productName));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('order_item.store')")
    public ResponseEntity<OrderItems> store(
            @Valid @RequestBody OrderItemsRequest request) {
        log.info("[POST] /api/v1/order-items");
        return ResponseEntity.status(HttpStatus.CREATED).body(orderItemsService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order_item.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/order-items/{}", id);
        orderItemsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}