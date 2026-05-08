package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOpnameRequestDTO {
    private Long partnerId;
    private String location; // "branch" | "warehouse"
    private Long locationId;
    private LocalDateTime date;
    private String notes;
    private List<StockOpnameItemDTO> items;
}