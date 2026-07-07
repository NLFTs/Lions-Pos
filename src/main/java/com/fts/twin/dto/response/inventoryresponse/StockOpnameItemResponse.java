package com.fts.twin.dto.response.inventoryresponse;

import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockOpnameItemResponse {

    private Long id;

    @JsonProperty("opname_id")
    private Long opnameId;

    private ProductDto product;

    @JsonProperty("qty_system")
    private BigDecimal qtySystem;

    @JsonProperty("qty_physical")
    private BigDecimal qtyPhysical;

    @JsonProperty("qty_difference")
    private BigDecimal qtyDifference;

    private String notes;

    @JsonProperty("counted_by")
    private UserSimpleDto countedBy;

    @JsonProperty("counted_at")
    private LocalDateTime countedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDto {
        private Long id;
        private String name;
        private String sku;
    }
}