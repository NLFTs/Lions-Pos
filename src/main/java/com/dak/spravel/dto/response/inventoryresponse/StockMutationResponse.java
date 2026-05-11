package com.dak.spravel.dto.response.inventoryresponse;

import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
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
public class StockMutationResponse {
    private Long id;
    private PartnerSimpleDto partner;
    private ProductSimpleDto product;
    private String type;

    @JsonProperty("from_location_type")
    private String fromLocationType;

    @JsonProperty("from_location_id")
    private Long fromLocationId;

    @JsonProperty("to_location_type")
    private String toLocationType;

    @JsonProperty("to_location_id")
    private Long toLocationId;

    private BigDecimal qty;

    @JsonProperty("reference_type")
    private String referenceType;

    @JsonProperty("reference_id")
    private Long referenceId;

    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("created_by")
    private UserSimpleDto createdBy;

    @Data
    public static class ProductSimpleDto {
        private Long id;
        private String name;
        private String sku;
    }
}