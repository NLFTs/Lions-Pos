package com.fts.twin.controller.procurement;

import com.fts.twin.dto.request.procurement.PurchaseOrderRequestDTO;
import com.fts.twin.model.procurement.PurchaseOrder;
import com.fts.twin.model.procurement.PurchaseOrderItems;
import com.fts.twin.service.procurement.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping("/admin")
    public ResponseEntity<List<PurchaseOrder>> getAllForAdmin() {
        log.info("[GET] /api/v1/purchase-orders/admin");
        return ResponseEntity.ok(purchaseOrderService.findAllPurchaseOrders());
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> index() {
        log.info("[GET] /api/v1/purchase-orders");
        return ResponseEntity.ok(purchaseOrderService.findAll());
    }
    
    @GetMapping("/page")
    public ResponseEntity<Page<PurchaseOrder>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/purchase-orders/page page={} size={}", page, size);
        return ResponseEntity.ok(purchaseOrderService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/purchase-orders/{}", id);
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<PurchaseOrderItems>> getItems(@PathVariable Long id) {
        log.info("[GET] /api/v1/purchase-orders/{}/items", id);
        return ResponseEntity.ok(purchaseOrderService.findItemsByOrderId(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> store(@Valid @RequestBody PurchaseOrderRequestDTO request) {
        log.info("[POST] /api/v1/purchase-orders");
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.create(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrder> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("[PATCH] /api/v1/purchase-orders/{}/status status={}", id, status);
        return ResponseEntity.ok(purchaseOrderService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/purchase-orders/{}", id);
        purchaseOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}