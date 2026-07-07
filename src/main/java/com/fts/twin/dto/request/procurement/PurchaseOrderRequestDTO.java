package com.fts.twin.dto.request.procurement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.sql.Date;
import java.util.List;

@Data
public class PurchaseOrderRequestDTO {
    private Long partnerId;

    @NotNull(message = "Supplier ID tidak boleh kosong")
    private Long supplierId;

    @NotBlank(message = "Location type tidak boleh kosong")
    @Pattern(regexp = "branch|warehouse",
            message = "Location type harus 'branch' atau 'warehouse'")
    private String locationType;

    @NotNull(message = "Location ID tidak boleh kosong")
    private Long locationId;

    private Date orderDate;   
    private Date expectedDate; 
    private String notes; 

    @NotNull(message = "Items tidak boleh kosong")
    @NotEmpty(message = "Minimal harus ada 1 item")
    @Valid
    private List<PurchaseOrderItemDTO> items;
}