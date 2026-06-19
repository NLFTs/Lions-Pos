package com.dak.spravel.dto.request.order;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrdersRequest {

    private Long branchId;
    private Long cashierId;
    private Long customerId; 

    private String orderNumber;

    private Long voucherId;
    private String notes;
    private String buyerName;

    private String manualDiscountType;
    private BigDecimal manualDiscountValue;
    private String manualDiscountNote;

    private List<OrderItemRequest> items;
    private PaymentRequest payment;
    private List<PaymentRequest> payments;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Long qty;
        private BigDecimal unitPrice;
        private String itemDiscountType;
        private BigDecimal itemDiscountValue;
        private BigDecimal itemDiscountAmount;
    }

    @Data
    public static class PaymentRequest {
        private String method;
        private BigDecimal amount;
        private BigDecimal cashTendered;
        private BigDecimal changeDue;
        private String bankName;
        private String referenceNo;
    }
}