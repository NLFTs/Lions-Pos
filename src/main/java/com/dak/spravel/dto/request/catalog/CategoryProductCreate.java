package com.dak.spravel.dto.request.catalog;

import lombok.Data;

@Data
public class CategoryProductCreate {

    private Long id;
    private Long partnerId;
    private Long parentId;
    private String name;
    private String description;
    private Integer sortOrder;
}