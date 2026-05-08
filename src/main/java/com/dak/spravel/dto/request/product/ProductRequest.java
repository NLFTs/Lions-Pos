package com.dak.spravel.dto.request.product;

import lombok.Data;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ProductRequest {

    @JsonProperty("partner_id")
    private Long partnerId;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("base_price")
    private BigDecimal basePrice;

    @JsonProperty("track_stock")
    private Boolean trackStock;
}