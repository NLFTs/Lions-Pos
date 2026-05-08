package com.dak.spravel.dto.request.partner;

import com.dak.spravel.model.common.Partners;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePartnerRequest {

    @NotBlank(message = "Nama partner tidak boleh kosong")
    private String name;

    @NotNull(message = "Plan tidak boleh kosong")
    private Partners.Plan plan;

    @NotNull(message = "Status aktif tidak boleh kosong")
    private Boolean isActive;
}