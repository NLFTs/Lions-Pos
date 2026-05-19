package com.dak.spravel.dto.response.catalogresponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;

    @JsonProperty("partner_id")
    private PartnerSimpleDto partnerId;

    @JsonProperty("category_id")
    private CategoryProductSimpleDto categoryId;

    private String name;
    
    private String sku;

    @JsonProperty("base_price")
    private BigDecimal basePrice;

    @JsonProperty("track_stock")
    private boolean trackStock;

    @JsonProperty("is_active")
    private boolean isActive;

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


    @Data
    public static class CategoryProductSimpleDto {
        private Long id;
        private String name;
    }
}
