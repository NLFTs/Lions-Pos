package com.dak.spravel.dto.request.procurement;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemDTO {
    private Long productId;
    private BigDecimal qtyOrdered;
    private BigDecimal unitCost;
}