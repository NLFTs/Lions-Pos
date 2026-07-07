package com.fts.twin.dto.response.inventoryresponse;

import java.time.LocalDateTime;

import com.fts.twin.dto.response.components.PartnerSimpleDto;
import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private Long id;
    private PartnerSimpleDto partner;
    private String name;
    private String address;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("created_by")
    private UserSimpleDto createdBy;
    @JsonProperty("updated_by")
    private UserSimpleDto updatedBy;
}