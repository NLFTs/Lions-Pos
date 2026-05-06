package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.util.List;

@Data
public class TransferRequestDTO {
    private Long partnerId;
    private String fromLocationType; // "branch" | "warehouse"
    private Long fromLocationId;
    private String toLocationType; // "branch" | "warehouse"
    private Long toLocationId;
    private String notes;
    private List<TransferRequestItemDTO> items;
}