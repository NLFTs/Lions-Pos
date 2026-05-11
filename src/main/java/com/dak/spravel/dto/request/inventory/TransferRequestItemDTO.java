package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequestItemDTO {
    private Long productId;

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty requested tidak boleh kosong")
    private BigDecimal qtyRequested;
}