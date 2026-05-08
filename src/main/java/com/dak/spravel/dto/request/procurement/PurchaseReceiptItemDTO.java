package com.dak.spravel.dto.request.procurement;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseReceiptItemDTO {
    private Long purchaseOrderItemId;
    private Long productId;
    private BigDecimal qtyReceived;
    private BigDecimal unitCost;
    private String notes;
}