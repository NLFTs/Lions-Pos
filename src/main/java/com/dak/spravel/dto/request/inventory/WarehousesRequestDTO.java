package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

@Data
public class WarehousesRequestDTO {

    @NotBlank(message = "Name tidak boleh kosong")
    private String name;

    private String address;

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Password tidak boleh kosong")
    private String password;
    
    private List<Long> roleIds;
}