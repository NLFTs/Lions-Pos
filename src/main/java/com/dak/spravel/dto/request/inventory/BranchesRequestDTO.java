package com.dak.spravel.dto.request.inventory;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BranchesRequestDTO {

    @NotNull(message = "Partner ID tidak boleh kosong")
    private Long partnersId;

    @NotBlank(message = "Nama branch tidak boleh kosong")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address; // opsional
}