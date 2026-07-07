package com.fts.twin.dto.request.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockBalanceInitRequest {

    @NotBlank(message = "locationType wajib diisi")
    private String locationType;

    @NotNull(message = "locationId wajib diisi")
    private Long locationId;

    @Valid
    @NotEmpty(message = "items tidak boleh kosong")
    private List<StockBalanceItemRequest> items;
}