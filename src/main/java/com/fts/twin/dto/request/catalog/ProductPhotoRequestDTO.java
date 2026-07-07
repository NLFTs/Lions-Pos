package com.fts.twin.dto.request.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProductPhotoRequestDTO {

    @JsonProperty("product_id")
    private Long productId;

    private String url;

    @JsonProperty("is_primary")
    private Boolean isPrimary;

    @JsonProperty("sort_order")
    private Integer sortOrder;

}
