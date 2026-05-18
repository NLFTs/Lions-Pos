package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TransferRequestItemDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "Qty requested tidak boleh kosong")
    @JsonProperty("qty_requested")
    private Long qtyRequested;
}