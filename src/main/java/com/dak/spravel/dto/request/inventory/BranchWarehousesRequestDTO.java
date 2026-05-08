package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BranchWarehousesRequestDTO {

    @NotNull(message = "Branch ID tidak boleh kosong")
    private Long branchesId;

    @NotNull(message = "Warehouse ID tidak boleh kosong")
    private Long warehousesId;
}