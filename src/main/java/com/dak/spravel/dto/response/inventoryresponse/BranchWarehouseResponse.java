package com.dak.spravel.dto.response.inventoryresponse;

import java.time.LocalDateTime;

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
public class BranchWarehouseResponse {
    private Long id;
    private BranchResponse branch;
    private WarehouseResponse warehouse;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty("created_by")
    private UserSimpleDto createdBy;

    @Data
    public static class BranchResponse {
        private Long id;
        private String name;
        private String address;
    }
    
    @Data
    public static class WarehouseResponse {
        private Long id;
        private String name;
        private String address; 
    }
}