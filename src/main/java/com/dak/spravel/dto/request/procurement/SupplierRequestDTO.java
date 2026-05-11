package com.dak.spravel.dto.request.procurement;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequestDTO {
    private Long partnerId; // tidak perlu validasi, diambil dari user login

    @NotBlank(message = "Nama supplier tidak boleh kosong")
    private String name;

    private String phone;   // opsional
    private String address; // opsional
    private String notes;   // opsional
}