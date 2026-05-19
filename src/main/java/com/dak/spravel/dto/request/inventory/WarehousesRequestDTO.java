package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehousesRequestDTO {

    private Long partnersId;

    @NotBlank(message = "Nama warehouse tidak boleh kosong")
    private String name;

    private String address; // opsional
}