package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockBalanceRequestDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long product;

    @NotBlank(message = "Location type tidak boleh kosong")
    @Pattern(regexp = "(?i)branch|warehouse", message = "Location type harus 'branch' atau 'warehouse'")
    private String locationType;

    @NotNull(message = "Location ID tidak boleh kosong")
    private Long locationId;

    @NotNull(message = "Qty tidak boleh kosong")
    @Positive(message = "qty minimal harus 1")
    private Long qty;
}