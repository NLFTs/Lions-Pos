package com.dak.spravel.dto.response.catalogresponse;

import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.model.catalog.Voucher;
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
    private Voucher.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;
    private Integer quota;
    private Integer usedCount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private UserSimpleDto createdBy;
    private UserSimpleDto updatedBy;
    private UserSimpleDto deletedBy;
}