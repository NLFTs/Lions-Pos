package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequestItemDTO {
    private Long productId;
    private BigDecimal qtyRequested;
}