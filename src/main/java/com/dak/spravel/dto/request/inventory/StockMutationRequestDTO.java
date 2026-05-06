package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockMutationRequestDTO {
    private Long productId;
    private Long partnerId;
    private String type;
    private String fromLocationType;
    private Long fromLocationId;
    private String toLocationType;
    private Long toLocationId;
    private BigDecimal qty;
    private String referenceType;
    private Long referenceId;
    private String notes;
}