package com.dak.spravel.controller.order;

import com.dak.spravel.dto.request.order.OrderItemsRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.model.order.OrderItems;
import com.dak.spravel.service.order.OrderItemsService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemsController {

    private final OrderItemsService orderItemsService;

    @PostMapping
    public ResponseEntity<ResData<OrderItems>> create(@RequestBody OrderItemsRequest request) {
        log.info("[POST] /api/v1/order-items - Request: {}", request);
        return ResponseBuilder.ok(orderItemsService.create(request));
    }

    @GetMapping
    public ResponseEntity<ResData<List<OrderItems>>> findAll() {
        log.info("[GET] /api/v1/order-items");
        return ResponseBuilder.ok(orderItemsService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResData<OrderItems>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/order-items/{}", id);
        return ResponseBuilder.ok(orderItemsService.findById(id));
    }

    @GetMapping("/product-name/{name}")
    public ResponseEntity<ResData<OrderItems>> findByProductName(@PathVariable String name) {
        log.info("[GET] /api/v1/order-items/product-name/{}", name);
        return ResponseBuilder.ok(orderItemsService.findByProductName(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/order-items/{}", id);
        orderItemsService.delete(id);
        return ResponseBuilder.ok("Order item deleted");
    }
}