package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOpnameRequestDTO {

    @NotNull(message = "Partner ID tidak boleh kosong")
    private Long partnerId;

    @NotBlank(message = "Location type tidak boleh kosong")
    @Pattern(regexp = "branch|warehouse",
            message = "Location type harus 'branch' atau 'warehouse'")
    private String location;

    @NotNull(message = "Location ID tidak boleh kosong")
    private Long locationId;

    @NotNull(message = "Tanggal opname tidak boleh kosong")
    private LocalDateTime date;

    private String notes; // opsional

    @NotNull(message = "Items tidak boleh kosong")
    private List<StockOpnameItemDTO> items;
}