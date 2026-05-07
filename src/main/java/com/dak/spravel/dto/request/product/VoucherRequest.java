package com.dak.spravel.dto.request.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class VoucherRequest {

    @JsonProperty("partner_id")
    private Long partnerId;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("discount_type")
    private String discountType; // "PERCENT" / "FIXED"

    @JsonProperty("discount_value")
    private BigDecimal discountValue;

    @JsonProperty("min_purchase")
    private BigDecimal minPurchase;

    @JsonProperty("max_discount")
    private BigDecimal maxDiscount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("valid_from")
    private LocalDateTime validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("valid_until")
    private LocalDateTime validUntil;
}
