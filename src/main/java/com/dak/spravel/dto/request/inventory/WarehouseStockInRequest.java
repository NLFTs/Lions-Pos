package com.dak.spravel.dto.request.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseStockInRequest {
    @NotNull(message = "Warehouse ID tidak boleh kosong")
    @JsonProperty("warehouse_id")
    private Long warehouseId;

    @NotNull(message = "Product ID tidak boleh kosong")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "Quantity tidak boleh kosong")
    @Min(value = 1, message = "Quantity minimal harus 1")
    private Long qty;
}