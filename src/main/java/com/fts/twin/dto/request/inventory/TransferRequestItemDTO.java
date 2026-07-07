package com.fts.twin.dto.request.inventory;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequestItemDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "Qty requested tidak boleh kosong")
    @JsonProperty("qty_requested")
    private BigDecimal qtyRequested;
}