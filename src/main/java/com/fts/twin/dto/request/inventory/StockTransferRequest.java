package com.fts.twin.dto.request.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockTransferRequest {
    @NotNull(message = "Product ID tidak boleh kosong")
    @JsonProperty("product_id")
    private Long productId;

    @NotBlank(message = "Tipe lokasi asal wajib diisi (WH/BR)")
    @JsonProperty("from_location_type")
    private String fromLocationType;

    @NotNull(message = "ID lokasi asal wajib diisi")
    @JsonProperty("from_location_id")
    private Long fromLocationId;

    @NotBlank(message = "Tipe lokasi tujuan wajib diisi (WH/BR)")
    @JsonProperty("to_location_type")
    private String toLocationType;

    @NotNull(message = "ID lokasi tujuan wajib diisi")
    @JsonProperty("to_location_id")
    private Long toLocationId;

    @NotNull(message = "Quantity transfer tidak boleh kosong")
    @Min(value = 1, message = "Quantity transfer minimal 1")
    @JsonProperty("qty")
    private Long qty;
}