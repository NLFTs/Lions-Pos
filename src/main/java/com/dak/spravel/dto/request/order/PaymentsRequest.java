package com.dak.spravel.dto.request.order;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentsRequest {
    private Long orderId;
    private String method;

    private BigDecimal amount;

    private BigDecimal cashTendered;

    private String bankName;
    private String referenceNo;
    private String proofUrl;
}
