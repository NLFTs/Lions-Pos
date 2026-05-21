package com.dak.spravel.controller.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dak.spravel.dto.request.order.OrdersRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.order.OrdersResponse;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.service.order.OrdersService;
import com.dak.spravel.util.ResponseBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    // SUPER ADMIN ONLY
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('order.index') and hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ResData<List<Orders>>> findAllForAdmin() {
        log.info("[GET] /api/v1/orders/admin");
        return ResponseBuilder.ok(ordersService.findAllOrders());
    }

    // PARTNER / EMPLOYEE ONLY
    @GetMapping
    @PreAuthorize("hasAuthority('order.index') or hasRole('EMPLOYEE')")
    public ResponseEntity<ResData<List<OrdersResponse>>> findAll() {
        log.info("[GET] /api/v1/orders");
        return ResponseBuilder.ok(ordersService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order.show') or hasRole('EMPLOYEE')")
    public ResponseEntity<ResData<OrdersResponse>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/orders/{}", id);
        return ResponseBuilder.ok(ordersService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('order.store') or hasRole('EMPLOYEE')")
    public ResponseEntity<ResData<OrdersResponse>> create(@RequestBody OrdersRequest request) {
        log.info("[POST] /api/v1/orders - Request: {}", request);
        return ResponseBuilder.ok(ordersService.create(request));
    }

    // Tetap Amankan Endpoint Delete (Hanya yang punya Authority Delete dan BUKAN Employee murni)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order.delete') and !hasRole('EMPLOYEE')")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/orders/{}", id);
        ordersService.delete(id);
        return ResponseBuilder.ok("Order deleted successfully");
    }
}