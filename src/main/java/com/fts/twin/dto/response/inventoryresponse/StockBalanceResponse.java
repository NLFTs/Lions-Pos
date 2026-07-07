package com.fts.twin.dto.response.inventoryresponse;

import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockBalanceResponse {
    private Long id;
    private ProductSimpleDto product;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("location_id")
    private Long locationId;

    private Long qty;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("updated_by")
    private UserSimpleDto updatedBy;

    @Data
    public static class ProductSimpleDto {
        private Long id;
        private String name;
        private String sku;

        private Long categoryId;
        private String categoryName;
    }
}