package com.fts.twin.dto.response.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private String method;
    private String status;
    private BigDecimal amount;
    private BigDecimal cashTendered;
    private BigDecimal changeDue;
    private String bankName;
    private String referenceNo;
    private String proofUrl;
    private LocalDateTime createdAt;
}
