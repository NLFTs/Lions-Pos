package com.dak.spravel.dto.request.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemsRequest {
    private Long orderId;
    private Long productId;

    private BigDecimal qty;
}
