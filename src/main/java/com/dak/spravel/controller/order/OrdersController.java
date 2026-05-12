package com.dak.spravel.controller.order;

import com.dak.spravel.dto.request.order.OrdersRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.service.order.OrdersService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    public ResponseEntity<ResData<Orders>> create(@RequestBody OrdersRequest request) {
        log.info("[POST] /api/v1/orders - Request: {}", request);

        Orders order = ordersService.create(request);

        return ResponseBuilder.ok(order);
    }

    @GetMapping
    public ResponseEntity<ResData<List<Orders>>> findAll() {
        log.info("[GET] /api/v1/orders");

        return ResponseBuilder.ok(ordersService.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResData<Orders>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/orders/{}", id);

        return ResponseBuilder.ok(ordersService.findById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/orders/{}", id);

        ordersService.delete(id);

        return ResponseBuilder.ok("Order deleted successfully");
    }
}