package com.dak.spravel.dto.response.catalogresponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
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
    private PartnerSimpleDto partnerId;
    private CategoryProductSimpleDto categoryId;
    private String name;
    private String sku;
    private BigDecimal basePrice;
    private boolean trackStock;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private UserSimpleDto createdBy;
    private UserSimpleDto updatedBy;
    private UserSimpleDto deletedBy;


    @Data
    public static class CategoryProductSimpleDto {
        private Long id;
        private String name;
    }
}
