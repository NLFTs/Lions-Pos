package com.dak.spravel.dto.request.procurement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseReceiptRequestDTO {

    @NotNull(message = "Purchase Order ID tidak boleh kosong")
    private Long purchaseOrderId;

    @NotNull(message = "Tanggal terima tidak boleh kosong")
    private LocalDateTime receivedDate;

    private String notes;

    @NotNull(message = "Items tidak boleh kosong")
    @NotEmpty(message = "Minimal harus ada 1 item")
    @Valid
    private List<PurchaseReceiptItemDTO> items;
}