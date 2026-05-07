package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockOpnameItemDTO {
    private Long productId;
    private BigDecimal qtySystem;
    private BigDecimal qtyPhysical;
    private String notes;
}