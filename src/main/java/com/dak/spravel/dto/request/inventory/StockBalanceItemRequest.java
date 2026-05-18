package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockBalanceItemRequest {

    @NotNull(message = "productId wajib diisi")
    private Long productId;

    @NotNull(message = "qty wajib diisi")
    @PositiveOrZero(message = "qty tidak boleh negatif")
    private BigDecimal qty;
}