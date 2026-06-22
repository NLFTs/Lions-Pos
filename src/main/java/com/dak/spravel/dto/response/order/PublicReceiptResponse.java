package com.dak.spravel.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
// Data Struk
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicReceiptResponse {

    private String orderNumber;
    private String branchName;
    private String cashierName;
    private String buyerName;
    private LocalDateTime createdAt;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private List<ItemLine> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemLine {
        private String productName;
        private Long qty;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String itemNote;
    }
}
