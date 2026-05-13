package com.dak.spravel.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for user listing with flattened role data.
 */
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String fullname;
    private String email;
    private java.time.LocalDateTime createdAt;
    private List<RoleData> roles;

    @Data
    public static class RoleData {
        private Long id;
        private String slug;
        private String name;
    }
}
