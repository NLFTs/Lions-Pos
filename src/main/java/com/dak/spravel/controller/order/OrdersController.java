package com.dak.spravel.controller.order;

import com.dak.spravel.dto.request.order.OrdersRequest;
import com.dak.spravel.dto.request.order.ReturnRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.order.OrdersResponse;
import com.dak.spravel.dto.response.order.ReturnResponse;
import com.dak.spravel.service.order.OrdersService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    // Auth dikontrol penuh oleh service layer (cek role + partner)
    // Super admin → findAllOrders(), partner/employee → findAll()

    @GetMapping("/admin")
    public ResponseEntity<ResData<List<OrdersResponse>>> findAllForAdmin() {
        log.info("[GET] /api/v1/orders/admin");
        return ResponseBuilder.ok(ordersService.findAll());
    }

    @GetMapping
    public ResponseEntity<ResData<List<OrdersResponse>>> findAll() {
        log.info("[GET] /api/v1/orders");
        return ResponseBuilder.ok(ordersService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResData<OrdersResponse>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/orders/{}", id);
        return ResponseBuilder.ok(ordersService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResData<OrdersResponse>> create(@RequestBody OrdersRequest request) {
        log.info("[POST] /api/v1/orders");
        return ResponseBuilder.ok(ordersService.create(request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ResData<OrdersResponse>> cancel(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/orders/{}/cancel", id);
        return ResponseBuilder.ok(ordersService.cancelOrder(id));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ResData<ReturnResponse>> returnOrder(
            @PathVariable Long id,
            @RequestBody ReturnRequest request) {
        log.info("[POST] /api/v1/orders/{}/return", id);
        return ResponseBuilder.ok(ordersService.returnOrder(id, request));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<ResData<OrdersResponse>> receipt(@PathVariable Long id) {
        log.info("[GET] /api/v1/orders/{}/receipt", id);
        return ResponseBuilder.ok(ordersService.getReceipt(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/orders/{}", id);
        ordersService.delete(id);
        return ResponseBuilder.ok("Order deleted successfully");
    }
}
