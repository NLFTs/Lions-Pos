package com.dak.spravel.dto.request.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryProductCreate {

    @JsonProperty("partner_id")
    private Long partnerId;

    @JsonProperty("parent_id")
    private Long parentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sort_order")
    private Integer sortOrder;
}