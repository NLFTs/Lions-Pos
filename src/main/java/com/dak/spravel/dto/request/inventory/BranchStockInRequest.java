package com.dak.spravel.dto.request.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;
import lombok.Data;

@Data
public class BranchStockInRequest {
    @NonNull
    @NotNull(message = "Branch ID tidak boleh kosong")
    @JsonProperty("branch_id")
    private Long branchId;

    @NonNull
    @NotNull(message = "Product ID tidak boleh kosong")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "Quantity tidak boleh kosong")
    @Min(value = 1, message = "Quantity minimal harus 1")
    private Long qty;
}