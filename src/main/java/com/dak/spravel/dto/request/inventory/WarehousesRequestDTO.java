package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class WarehousesRequestDTO {

    @NotBlank(message = "Name tidak boleh kosong")
    private String name;

    private String address;
}