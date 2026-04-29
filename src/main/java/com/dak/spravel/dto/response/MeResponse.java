package com.dak.spravel.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for current authenticated user profile.
 */
@Data
public class MeResponse {
    private Long id;
    private String username;
    private String fullname;
    private List<String> roles;
    private List<String> permissions;
}
