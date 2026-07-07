package com.fts.twin.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockBalanceItemRequest {

    @NotNull(message = "productId wajib diisi")
    private Long productId;

    @NotNull(message = "qty wajib diisi")
    @Positive(message = "qty minimal harus 1")
    private Long qty;
}