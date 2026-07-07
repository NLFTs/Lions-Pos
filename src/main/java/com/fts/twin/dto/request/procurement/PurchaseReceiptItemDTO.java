package com.fts.twin.dto.request.procurement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseReceiptItemDTO {

    @NotNull(message = "Purchase Order Item ID tidak boleh kosong")
    private Long purchaseOrderItemId;

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty received tidak boleh kosong")
    @Positive(message = "Qty received harus lebih dari 0")
    private BigDecimal qtyReceived;

    private BigDecimal unitCost;
    private String notes;
}