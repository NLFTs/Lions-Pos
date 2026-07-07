package com.fts.twin.dto.request.inventory;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOpnameRequestDTO {

    @NotNull(message = "Partner ID tidak boleh kosong")
    @Column(name = "partner_id")
    private Long partnerId;

    @NotBlank(message = "Location type tidak boleh kosong")
    @Pattern(regexp = "branch|warehouse",
            message = "Location type harus 'branch' atau 'warehouse'")
    @Column(name = "location_type")
    private String locationType;

    @NotNull(message = "Location ID tidak boleh kosong")
    @Column(name = "location_id")
    private Long locationId;

    @NotNull(message = "Tanggal opname tidak boleh kosong")
    private LocalDateTime date;

    private String notes;

    @NotNull(message = "Items tidak boleh kosong")
    private List<StockOpnameItemDTO> items;
}