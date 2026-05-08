package com.dak.spravel.dto.request.inventory;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOpnameRequestDTO {
<<<<<<< HEAD
    private Long partnerId;
    private String location; // "branch" | "warehouse"
    private Long locationId;
    private LocalDateTime date;
    private String notes;
=======

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
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    private List<StockOpnameItemDTO> items;
}