package com.dak.spravel.dto.response.order;

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

    private Long branchId;
    private String branchName;

    private Long cashierId;
    private String cashierName;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
    private List<PaymentResponse> payments;
}
