package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockBalanceRequestDTO {
    private Long productId;
    private String locationType; // "branch" | "warehouse"
    private Long locationId;
    private BigDecimal qty;
}