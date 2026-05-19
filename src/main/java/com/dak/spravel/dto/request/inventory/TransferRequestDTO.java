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
    @JsonProperty("from_location_type")
    private String fromLocationType; // "branch" atau "warehouse"


    @NotNull(message = "From location ID tidak boleh kosong")
    @JsonProperty("from_location_id")
    private Long fromLocationId; 

    @NotBlank(message = "To location type tidak boleh kosong")
    @JsonProperty("to_location_type")
    private String toLocationType; // "branch" atau "warehouse"

    @NotNull(message = "To location ID tidak boleh kosong")
    @JsonProperty("to_location_id")
    private Long toLocationId;

    private String notes; // opsional

    
    @NotNull(message = "Items tidak boleh kosong")
    private List<TransferRequestItemDTO> items;
}