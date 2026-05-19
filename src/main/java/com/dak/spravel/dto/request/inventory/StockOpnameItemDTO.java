package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class StockOpnameItemDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    // qtySystem diambil otomatis dari stock_balances saat start-counting
    // tidak perlu diisi oleh user

    private Long qtySystem;

    @NotNull(message = "Qty physical tidak boleh kosong")
    @PositiveOrZero(message = "Qty physical tidak boleh negatif")
    private Long qtyPhysical; // diisi saat counting

    private String notes; // opsional
}