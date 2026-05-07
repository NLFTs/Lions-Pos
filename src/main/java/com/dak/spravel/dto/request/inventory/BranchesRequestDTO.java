package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BranchesRequestDTO {

    @NotNull(message = "Partner ID tidak boleh kosong")
    private Long partnersId;

    @NotBlank(message = "Nama branch tidak boleh kosong")
    private String name;

    private String address; // opsional

    private Boolean isActive; // opsional, default true di service
}