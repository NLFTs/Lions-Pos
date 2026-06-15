package com.dak.spravel.dto.request.procurement;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequestDTO {
    private Long partnerId; 

    @NotBlank(message = "Nama supplier tidak boleh kosong")
    private String name;

    
    private String address;
    
    private String notes;
}