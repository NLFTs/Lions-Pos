package com.dak.spravel.dto.request.order;

import lombok.Data;

@Data
public class OrderItemsRequest {
    private Long orderId;
    private Long productId;

    private Long qty;
}
