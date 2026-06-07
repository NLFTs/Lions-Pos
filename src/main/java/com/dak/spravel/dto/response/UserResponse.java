package com.dak.spravel.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.dak.spravel.dto.response.components.PartnerSimpleDto;

/**
 * Response DTO for user listing with flattened role data.
 */
@Data
public class UserResponse {
    private Long id;
    private PartnerSimpleDto partner;
    private String username;
    private String fullname;
    private String email;
    private String avatar;
    private LocalDateTime createdAt;
    private List<RoleData> roles;
    private Long branchId;
    private String branchName;
    private Long warehouseId;
    private String warehouseName;

    @JsonProperty("is_active")
    private Boolean isActive;

    @Data
    public static class RoleData {
        private Long id;
        private String slug;
        private String name;
    }
}
