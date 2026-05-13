package com.dak.spravel.controller.procurement;

import com.dak.spravel.dto.request.procurement.PurchaseOrderRequestDTO;
import com.dak.spravel.model.procurement.PurchaseOrder;
import com.dak.spravel.model.procurement.PurchaseOrderItems;
import com.dak.spravel.service.procurement.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // SUPER ADMIN ONLY
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('purchase_order.index')")
    public ResponseEntity<List<PurchaseOrder>> getAllForAdmin() {
        log.info("[GET] /api/v1/purchase-orders/admin");
        return ResponseEntity.ok(purchaseOrderService.findAllPurchaseOrders());
    }

    // PARTNER / EMPLOYEE ONLY
    @GetMapping
    @PreAuthorize("hasAuthority('purchase_order.index')")
    public ResponseEntity<List<PurchaseOrder>> index() {
        log.info("[GET] /api/v1/purchase-orders");
        return ResponseEntity.ok(purchaseOrderService.findAll());
    }

    // PAGINATION PARTNER / EMPLOYEE ONLY
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('purchase_order.index')")
    public ResponseEntity<Page<PurchaseOrder>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/purchase-orders/page page={} size={}", page, size);
        return ResponseEntity.ok(purchaseOrderService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase_order.show')")
    public ResponseEntity<PurchaseOrder> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/purchase-orders/{}", id);
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('purchase_order.show')")
    public ResponseEntity<List<PurchaseOrderItems>> getItems(@PathVariable Long id) {
        log.info("[GET] /api/v1/purchase-orders/{}/items", id);
        return ResponseEntity.ok(purchaseOrderService.findItemsByOrderId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase_order.store')")
    public ResponseEntity<PurchaseOrder> store(@Valid @RequestBody PurchaseOrderRequestDTO request) {
        log.info("[POST] /api/v1/purchase-orders");
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('purchase_order.update')")
    public ResponseEntity<PurchaseOrder> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("[PATCH] /api/v1/purchase-orders/{}/status status={}", id, status);
        return ResponseEntity.ok(purchaseOrderService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase_order.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/purchase-orders/{}", id);
        purchaseOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}