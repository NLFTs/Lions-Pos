package com.dak.spravel.dto.request.procurement;

<<<<<<< HEAD
=======
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.sql.Date;
import java.util.List;

@Data
public class PurchaseOrderRequestDTO {
<<<<<<< HEAD
    private Long partnerId;
    private Long supplierId;
    private String locationType;
    private Long locationId;
    private Date orderDate;
    private Date expectedDate;
    private String notes;
=======
    private Long partnerId; // tidak perlu validasi, diambil dari user login

    @NotNull(message = "Supplier ID tidak boleh kosong")
    private Long supplierId;

    @NotBlank(message = "Location type tidak boleh kosong")
    @Pattern(regexp = "branch|warehouse",
            message = "Location type harus 'branch' atau 'warehouse'")
    private String locationType;

    @NotNull(message = "Location ID tidak boleh kosong")
    private Long locationId;

    private Date orderDate;   // opsional
    private Date expectedDate; // opsional
    private String notes;      // opsional

    @NotNull(message = "Items tidak boleh kosong")
    @NotEmpty(message = "Minimal harus ada 1 item")
    @Valid
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    private List<PurchaseOrderItemDTO> items;
}