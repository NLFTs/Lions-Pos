package com.dak.spravel.controller.procurement;

import com.dak.spravel.dto.request.procurement.PurchaseReceiptRequestDTO;
import com.dak.spravel.model.procurement.PurchaseReceipt;
import com.dak.spravel.model.procurement.PurchaseReceiptItem;
import com.dak.spravel.service.procurement.PurchaseReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/purchase-receipts")
@RequiredArgsConstructor
public class PurchaseReceiptController {

    private final PurchaseReceiptService purchaseReceiptService;

    @GetMapping("/order/{purchaseOrderId}")
    @PreAuthorize("hasAuthority('purchase_receipt.index')")
    public ResponseEntity<List<PurchaseReceipt>> getByOrder(@PathVariable Long purchaseOrderId) {
        log.info("[GET] /api/v1/purchase-receipts/order/{}", purchaseOrderId);
        return ResponseEntity.ok(purchaseReceiptService.findByOrderId(purchaseOrderId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase_receipt.show')")
    public ResponseEntity<PurchaseReceipt> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/purchase-receipts/{}", id);
        return ResponseEntity.ok(purchaseReceiptService.findById(id));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('purchase_receipt.show')")
    public ResponseEntity<List<PurchaseReceiptItem>> getItems(@PathVariable Long id) {
        log.info("[GET] /api/v1/purchase-receipts/{}/items", id);
        return ResponseEntity.ok(purchaseReceiptService.findItemsByReceiptId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase_receipt.store')")
    public ResponseEntity<PurchaseReceipt> store(@Valid @RequestBody PurchaseReceiptRequestDTO request) {
        log.info("[POST] /api/v1/purchase-receipts purchaseOrderId={}", request.getPurchaseOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseReceiptService.create(request));
    }
}