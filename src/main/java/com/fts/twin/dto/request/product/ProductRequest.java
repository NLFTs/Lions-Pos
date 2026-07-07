package com.fts.twin.dto.request.product;

import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ProductRequest {

    @JsonProperty("partner_id")
    private Long partnerId;

    @JsonProperty("category_id")
    private Long categoryId;

    @NotBlank(message = "Nama produk tidak boleh kosong")
    @JsonProperty("name")
    private String name;

    @JsonProperty("sku")
    private String sku;

    @NotNull(message = "Harga dasar tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "999999999999999.99")
    @JsonProperty("base_price")
    private BigDecimal basePrice;

    @JsonProperty("track_stock")
    private Boolean trackStock;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("imageUrl")
    public void setImageUrlCamel(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

