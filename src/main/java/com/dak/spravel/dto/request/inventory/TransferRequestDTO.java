package com.dak.spravel.dto.request.inventory;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequestDTO {
    @JsonProperty("partner_id")
    private Long partnerId;

    @NotBlank(message = "From location type tidak boleh kosong")
    private String fromLocationType;


    @NotNull(message = "From location ID tidak boleh kosong")
    private Long fromLocationId;

    @NotBlank(message = "To location type tidak boleh kosong")
    private String toLocationType;

    @NotNull(message = "To location ID tidak boleh kosong")
    private Long toLocationId;

    private String notes; // opsional

    @NotNull(message = "Items tidak boleh kosong")
    private List<TransferRequestItemDTO> items;
}