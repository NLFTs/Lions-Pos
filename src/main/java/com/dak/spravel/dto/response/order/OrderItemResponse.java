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
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long qty;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Long returnQty;
    private String returnReason;
}
