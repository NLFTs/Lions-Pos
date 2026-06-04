package com.dak.spravel.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnResponse {
    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private List<ReturnItemResponse> returnedItems;
    private BigDecimal totalRefund;
    private LocalDateTime returnedAt;
    // Informasi lokasi tujuan return
    private String returnLocationType;
    private Long returnLocationId;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReturnItemResponse {
        private Long orderItemId;
        private String productName;
        private Long qtyReturn;
        private BigDecimal unitPrice;
        private BigDecimal refundAmount;
        private String reason;
    }
}
