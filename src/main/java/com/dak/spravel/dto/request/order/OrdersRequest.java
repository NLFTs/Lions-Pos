package com.dak.spravel.dto.request.order;

import lombok.Data;

@Data
public class OrdersRequest {

    private Long partnerId;
    private Long branchId;
    private Long cashierId;

    private String orderNumber;

    private Long voucherId;
    private String notes;
}