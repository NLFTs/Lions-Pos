package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockBalanceItemRequest {

    @NotNull(message = "productId wajib diisi")
    private Long productId;

    @NotNull(message = "qty wajib diisi")
    @Positive(message = "qty minimal harus 1")
    private Long qty;
}