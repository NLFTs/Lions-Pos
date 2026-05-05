package com.dak.spravel.dto.request.catalog;

import lombok.Data;

@Data
public class ProductPhotoRequestDTO {
    private Long productId;
    private String url;
    private Boolean isPrimary;
    private Integer sortOrder;

}
