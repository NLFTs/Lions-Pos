package com.dak.spravel.dto.response.order;

import java.math.BigDecimal;

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
    private String method;
    private String status;
    private BigDecimal amount;
    private BigDecimal cashTendered;
    private BigDecimal changeDue;
}
