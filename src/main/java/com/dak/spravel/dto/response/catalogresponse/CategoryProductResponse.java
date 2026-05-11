package com.dak.spravel.dto.response.catalogresponse;

import java.time.LocalDateTime;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProductResponse {
    private Long id;

    @JsonProperty("partner_id")
    private PartnerSimpleDto partnerId;

    private ParentDto parent;
    
    private String name;
    
    private String description;

    @JsonProperty("sort_order")
    private Integer sortOrder;

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

    @Data
    public static class ParentDto {
        private Long id;
        private String name;
    }
}
