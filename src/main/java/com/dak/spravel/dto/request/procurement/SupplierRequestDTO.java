package com.dak.spravel.dto.request.procurement;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotBlank;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;

@Data
public class SupplierRequestDTO {
<<<<<<< HEAD
    private Long partnerId;
    private String name;
    private String phone;
    private String address;
    private String notes;
=======
    private Long partnerId; // tidak perlu validasi, diambil dari user login

    @NotBlank(message = "Nama supplier tidak boleh kosong")
    private String name;

    private String phone;   // opsional
    private String address; // opsional
    private String notes;   // opsional
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
}