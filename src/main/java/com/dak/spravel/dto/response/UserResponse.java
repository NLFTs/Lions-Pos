package com.dak.spravel.dto.response;

import lombok.Data;

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
    private java.time.LocalDateTime createdAt;
    private List<RoleData> roles;
    private Long branchId;
    private String branchName;

    @Data
    public static class RoleData {
        private Long id;
        private String slug;
        private String name;
    }
}
