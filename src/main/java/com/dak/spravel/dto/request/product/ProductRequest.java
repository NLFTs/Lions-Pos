package com.dak.spravel.dto.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest{
    private Long partnerId;
    private Long categoryId;

    private String name;
    private String sku;
    private BigDecimal basePrice;
}
