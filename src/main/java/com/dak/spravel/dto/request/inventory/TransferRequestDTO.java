package com.dak.spravel.dto.request.inventory;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.util.List;

@Data
public class TransferRequestDTO {
<<<<<<< HEAD
    private Long partnerId;
    private String fromLocationType; // "branch" | "warehouse"
    private Long fromLocationId;
    private String toLocationType; // "branch" | "warehouse"
    private Long toLocationId;
    private String notes;
=======

    @NotNull(message = "Partner ID tidak boleh kosong")
    private Long partnerId;

    @NotBlank(message = "From location type tidak boleh kosong")
    @Pattern(regexp = "branch|warehouse",
            message = "From location type harus 'branch' atau 'warehouse'")
    private String fromLocationType;

    @NotNull(message = "From location ID tidak boleh kosong")
    private Long fromLocationId;

    @NotBlank(message = "To location type tidak boleh kosong")
    @Pattern(regexp = "branch|warehouse",
            message = "To location type harus 'branch' atau 'warehouse'")
    private String toLocationType;

    @NotNull(message = "To location ID tidak boleh kosong")
    private Long toLocationId;

    private String notes; // opsional

    @NotNull(message = "Items tidak boleh kosong")
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    private List<TransferRequestItemDTO> items;
}