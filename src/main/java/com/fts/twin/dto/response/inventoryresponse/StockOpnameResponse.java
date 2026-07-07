package com.fts.twin.dto.response.inventoryresponse;

import com.fts.twin.dto.response.components.PartnerSimpleDto;
import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fts.twin.model.inventory.StockOpname;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockOpnameResponse {
    private Long id;
    private PartnerSimpleDto partner;
    private String location;

    @JsonProperty("location_id")
    private Long locationId;

    private LocalDateTime date;
    private StockOpname.Status status;
    private String notes;

    @JsonProperty("reviewed_at")
    private LocalDateTime reviewedAt;

    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @JsonProperty("created_by")
    private UserSimpleDto createdBy;

    @JsonProperty("updated_by")
    private UserSimpleDto updatedBy;

    @JsonProperty("deleted_by")
    private UserSimpleDto deletedBy;

    @JsonProperty("reviewed_by")
    private UserSimpleDto reviewedBy;

    @JsonProperty("approved_by")
    private UserSimpleDto approvedBy;

    private List<StockOpnameItemResponse> items;

    @Data
    public static class StockOpnameItemResponse {
        private Long id;
        private ProductSimpleDto product;

        @JsonProperty("qty_system")
        private Long qtySystem;

        @JsonProperty("qty_physical")
        private Long qtyPhysical;

        @JsonProperty("qty_difference")
        private Long qtyDifference;

        private String notes;

        @JsonProperty("counted_by")
        private UserSimpleDto countedBy;

        @JsonProperty("counted_at")
        private LocalDateTime countedAt;
    }

    @Data
    public static class ProductSimpleDto {
        private Long id;
        private String name;
        private String sku;
    }
}