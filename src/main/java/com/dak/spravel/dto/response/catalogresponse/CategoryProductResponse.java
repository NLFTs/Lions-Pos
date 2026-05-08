package com.dak.spravel.dto.response.catalogresponse;

import java.time.LocalDateTime;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
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
    private PartnerSimpleDto partner;
    private ParentDto parent;
    private String name;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private UserSimpleDto createdBy;
    private UserSimpleDto updatedBy;
    private UserSimpleDto deletedBy;

    @Data
    public static class ParentDto {
        private Long id;
        private String name;
    }
}
