package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StockBalanceRequestDTO {

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotBlank(message = "Location type tidak boleh kosong")
    @Pattern(regexp = "branch|warehouse", message = "Location type harus 'branch' atau 'warehouse'")
    private String locationType;

    @NotNull(message = "Location ID tidak boleh kosong")
    private Long locationId;

    @NotNull(message = "Qty tidak boleh kosong")
    private Long qty;
}