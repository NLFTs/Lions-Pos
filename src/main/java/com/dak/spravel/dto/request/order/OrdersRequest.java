package com.dak.spravel.dto.request.order;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrdersRequest {

    private Long branchId;
    private Long cashierId; // Optional, defaults to current user
    private Long customerId; // Optional

    private String orderNumber;

    private Long voucherId;
    private String notes;

    private List<OrderItemRequest> items;
    private PaymentRequest payment;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Long qty;
        private BigDecimal unitPrice;
    }

    @Data
    public static class PaymentRequest {
        private String method; // CASH, TRANSFER
        private BigDecimal amount;
        private BigDecimal cashTendered;
        private BigDecimal changeDue;
        private String bankName;
        private String referenceNo;
    }
}