package com.dak.spravel.dto.request.procurement;

<<<<<<< HEAD
=======
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseReceiptRequestDTO {
<<<<<<< HEAD
    private Long purchaseOrderId;
    private LocalDateTime receivedDate;
    private String notes;
=======

    @NotNull(message = "Purchase Order ID tidak boleh kosong")
    private Long purchaseOrderId;

    @NotNull(message = "Tanggal terima tidak boleh kosong")
    private LocalDateTime receivedDate;

    private String notes; // opsional

    @NotNull(message = "Items tidak boleh kosong")
    @NotEmpty(message = "Minimal harus ada 1 item")
    @Valid
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    private List<PurchaseReceiptItemDTO> items;
}