package com.dak.spravel.controller.order;

import com.dak.spravel.dto.request.order.PaymentsRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.service.order.PaymentsService;
import com.dak.spravel.util.ResponseBuilder;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final PaymentsService paymentsService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('payments.index')")
    public ResponseEntity<List<Payments>> getAllForAdmin() {
        log.info("[GET] /api/v1/payments/admin");
        return ResponseEntity.ok(paymentsService.findAllPayments());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('payments.index')")
    public ResponseEntity<List<Payments>> index() {
        log.info("[GET] /api/v1/payments");
        return ResponseEntity.ok(paymentsService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('partner.index')")
    public ResponseEntity<ResData<Page<Payments>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /partners/page page={} size={}", page, size);
        return ResponseBuilder.ok(paymentsService.findAll(page, size));
    }


    @PostMapping
    @PreAuthorize("hasAuthority('payments.store')")
    public ResponseEntity<Payments> pay(@Valid @RequestBody PaymentsRequest request) {
        log.info("[POST] /api/v1/payments orderId={}", request.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentsService.pay(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('payments.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/payments/{}", id);
        paymentsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}