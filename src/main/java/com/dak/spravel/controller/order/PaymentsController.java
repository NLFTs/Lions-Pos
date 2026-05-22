package com.dak.spravel.controller.order;

import com.dak.spravel.dto.request.order.PaymentsRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.order.PaymentResponse;
import com.dak.spravel.service.order.PaymentsService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final PaymentsService paymentsService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('payments.index')")
    public ResponseEntity<ResData<List<PaymentResponse>>> getAllForAdmin() {
        log.info("[GET] /api/v1/payments/admin");
        return ResponseBuilder.ok(paymentsService.findAllPayments());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('payments.index')")
    public ResponseEntity<ResData<List<PaymentResponse>>> index() {
        log.info("[GET] /api/v1/payments");
        return ResponseBuilder.ok(paymentsService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('payments.index')")
    public ResponseEntity<ResData<Page<PaymentResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /payments/page page={} size={}", page, size);
        return ResponseBuilder.ok(paymentsService.findAll(page, size));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('payments.store')")
    public ResponseEntity<ResData<PaymentResponse>> pay(@Valid @RequestBody PaymentsRequest request) {
        log.info("[POST] /api/v1/payments orderId={}", request.getOrderId());
        return ResponseBuilder.ok(paymentsService.pay(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('payments.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/payments/{}", id);
        paymentsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // VERIFY PAYMENT — konfirmasi transfer, hanya admin-partners (owner/manager)
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('payments.update')")
    public ResponseEntity<ResData<PaymentResponse>> verify(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/payments/{}/verify", id);
        return ResponseBuilder.ok(paymentsService.verifyPayment(id));
    }
}
