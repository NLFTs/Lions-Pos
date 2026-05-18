package com.dak.spravel.dto.response.common;

import java.time.LocalDateTime;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PartnerResponse {
    private Long id;

    private String name;

    private String slug;

    private String plan;

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
