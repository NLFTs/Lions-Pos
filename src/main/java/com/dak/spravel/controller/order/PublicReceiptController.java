package com.dak.spravel.controller.order;

import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.order.PublicReceiptResponse;
import com.dak.spravel.model.order.OrderItems;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.repository.order.OrdersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Public endpoint — no authentication required.
 * Digunakan pelanggan untuk cek struk via QR code.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicReceiptController {

    private final OrdersRepository ordersRepository;

    @GetMapping("/receipt/{orderNumber}")
    public ResponseEntity<ResData<?>> getReceipt(@PathVariable String orderNumber) {
        log.info("[PUBLIC] GET /api/v1/public/receipt/{}", orderNumber);

        Orders order = ordersRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResData.error(404, "Struk tidak ditemukan.", null));
        }

        return ResponseEntity.ok(ResData.of(toResponse(order)));
    }

    // ─── Private mapper ──────────────────────────────────────────────────────

    private PublicReceiptResponse toResponse(Orders order) {
        List<PublicReceiptResponse.ItemLine> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItems i : order.getItems()) {
                items.add(PublicReceiptResponse.ItemLine.builder()
                        .productName(i.getProductName())
                        .qty(i.getQty())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .itemNote(i.getItemNote())
                        .build());
            }
        }

        String paymentMethod = null;
        String paymentStatus = null;
        if (order.getPayments() != null && !order.getPayments().isEmpty()) {
            Payments p = order.getPayments().iterator().next();
            paymentMethod = p.getMethod() != null ? p.getMethod().name() : null;
            paymentStatus = p.getStatus() != null ? p.getStatus().name() : null;
        }

        return PublicReceiptResponse.builder()
                .orderNumber(order.getOrderNumber())
                .branchName(order.getBranch() != null ? order.getBranch().getName() : null)
                .cashierName(order.getCashier() != null ? order.getCashier().getFullname() : null)
                .buyerName(order.getBuyerName())
                .createdAt(order.getCreatedAt())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .total(order.getTotal())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .items(items)
                .build();
    }
}
