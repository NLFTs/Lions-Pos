package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TransferRequestDTO {
    @JsonProperty("partner_id")
    private Long partnerId;

    @NotNull(message = "From location ID tidak boleh kosong")
    @JsonProperty("from_location_id")
    private Long fromLocationId; 

    @NotNull(message = "To location ID tidak boleh kosong")
    @JsonProperty("to_location_id")
    private Long toLocationId;

    private String notes; // opsional

    @NotNull(message = "Items tidak boleh kosong")
    private List<TransferRequestItemDTO> items;
}