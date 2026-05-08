package com.dak.spravel.controller.order;

import com.dak.spravel.dto.request.order.PaymentsRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.service.order.PaymentsService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final PaymentsService paymentService;

    @PostMapping
    public ResponseEntity<ResData<Payments>> pay(@RequestBody PaymentsRequest request) {
        log.info("[POST] /api/v1/payments - Request: {}", request);
        return ResponseBuilder.ok(paymentService.pay(request));
    }
}