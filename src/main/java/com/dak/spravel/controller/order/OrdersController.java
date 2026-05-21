package com.dak.spravel.controller.order;

    import com.dak.spravel.dto.request.order.OrdersRequest;
    import com.dak.spravel.dto.request.order.ReturnRequest;
    import com.dak.spravel.dto.response.ResData;
    import com.dak.spravel.dto.response.order.OrdersResponse;
    import com.dak.spravel.dto.response.order.ReturnResponse;
    import com.dak.spravel.model.order.Orders;
    import com.dak.spravel.service.order.OrdersService;
    import com.dak.spravel.util.ResponseBuilder;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @Slf4j
    @RestController
    @RequestMapping("/api/v1/orders")
    @RequiredArgsConstructor
    public class OrdersController {

        private final OrdersService ordersService;

        // SUPER ADMIN ONLY
        @GetMapping("/admin")
        @PreAuthorize("hasAuthority('order.index')")
        public ResponseEntity<ResData<List<Orders>>> findAllForAdmin() {
            log.info("[GET] /api/v1/orders/admin");
            return ResponseBuilder.ok(ordersService.findAllOrders());
        }

        // PARTNER / EMPLOYEE ONLY
        @GetMapping
        // @PreAuthorize("hasAuthority('order.index')")
        public ResponseEntity<ResData<List<OrdersResponse>>> findAll() {
            log.info("[GET] /api/v1/orders");
            return ResponseBuilder.ok(ordersService.findAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('order.show')")
        public ResponseEntity<ResData<OrdersResponse>> findById(@PathVariable Long id) {
            log.info("[GET] /api/v1/orders/{}", id);
            return ResponseBuilder.ok(ordersService.findById(id));
        }

        @PostMapping
        // @PreAuthorize("hasAuthority('order.store')")
        public ResponseEntity<ResData<OrdersResponse>> create(@RequestBody OrdersRequest request) {
            log.info("[POST] /api/v1/orders - Request: {}", request);
            return ResponseBuilder.ok(ordersService.create(request));
        }

        // Cancel Order 
        @PatchMapping("/{id}/cancel")
        @PreAuthorize("hasAuthority('order.update')")
        public ResponseEntity<ResData<OrdersResponse>> cancel(@PathVariable Long id) {
            log.info("[PATCH] /api/v1/orders/{}/cancel", id);
            return ResponseBuilder.ok(ordersService.cancelOrder(id));
        }

        // Return Order
        @PostMapping("/{id}/return")
        // @PreAuthorize("hasAuthority('order.update')")
        public ResponseEntity<ResData<ReturnResponse>> returnOrder(
                @PathVariable Long id,
                @RequestBody ReturnRequest request) {
            log.info("[POST] /api/v1/orders/{}/return", id);
            return ResponseBuilder.ok(ordersService.returnOrder(id, request));
        }

        // Cetak Download Struk
        @GetMapping("/{id}/receipt")
        @PreAuthorize("hasAuthority('order.show')")
        public ResponseEntity<ResData<OrdersResponse>> receipt(@PathVariable Long id) {
            log.info("[GET] /api/v1/orders/{}/receipt", id);
            return ResponseBuilder.ok(ordersService.getReceipt(id));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('order.delete')")
        public ResponseEntity<ResData<String>> delete(@PathVariable Long id) {
            log.info("[DELETE] /api/v1/orders/{}", id);
            ordersService.delete(id);
            return ResponseBuilder.ok("Order deleted successfully");
        }
    }