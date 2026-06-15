package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class StockOpnameItemDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    private Long qtySystem;

    @NotNull(message = "Qty physical tidak boleh kosong")
    @PositiveOrZero(message = "Qty physical tidak boleh negatif")
    private Long qtyPhysical;

    private String notes;
}