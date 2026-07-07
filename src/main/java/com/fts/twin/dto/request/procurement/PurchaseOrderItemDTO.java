package com.fts.twin.dto.request.procurement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty ordered tidak boleh kosong")
    @Positive(message = "Qty ordered harus lebih dari 0")
    private BigDecimal qtyOrdered;

    @NotNull(message = "Unit cost tidak boleh kosong")
    @Positive(message = "Unit cost harus lebih dari 0")
    private BigDecimal unitCost;
}