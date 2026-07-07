package com.fts.twin.dto.response.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdersResponse {
    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String notes;
    private String buyerName;
    private String manualDiscountType;
    private java.math.BigDecimal manualDiscountValue;
    private String manualDiscountNote;
    private Long branchId;
    private String branchName;
    private Long cashierId;
    private String cashierName;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
    private List<PaymentResponse> payments;
    private ReturnInfo returnInfo;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReturnInfo {
        private java.time.LocalDateTime returnedAt;
        private java.math.BigDecimal totalRefund;
        private List<ReturnItemDetail> items;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ReturnItemDetail {
            private String productName;
            private Long qtyReturn;
            private String reason;
            private java.math.BigDecimal refundAmount;
        }
    }
}
