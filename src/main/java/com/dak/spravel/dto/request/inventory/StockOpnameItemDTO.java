package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockOpnameItemDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty system tidak boleh kosong")
    private BigDecimal qtySystem;

    private BigDecimal qtyPhysical; // opsional, diisi saat counting

    private String notes; // opsional

}