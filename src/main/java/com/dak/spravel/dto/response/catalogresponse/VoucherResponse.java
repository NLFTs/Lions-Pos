package com.dak.spravel.dto.response.catalogresponse;

import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.model.catalog.Voucher;
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
public class VoucherResponse {
    private Long id;
    
    private PartnerSimpleDto partner;
    
    private String code;
    
    private String name;

    @JsonProperty("discount_type")
    private Voucher.DiscountType discountType;

    @JsonProperty("discount_value")
    private BigDecimal discountValue;
    
    @JsonProperty("min_purchase")
    private BigDecimal minPurchase;

    @JsonProperty("max_discount")
    private BigDecimal maxDiscount;
    
    private Integer quota;

    @JsonProperty("used_count")
    private Integer usedCount;
    
    @JsonProperty("valid_from")
    private LocalDateTime validFrom;
    
    @JsonProperty("valid_until")
    private LocalDateTime validUntil;

    @JsonProperty("is_active")
    private Boolean isActive;
    
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
}