package com.dak.spravel.dto.response;

import lombok.Data;

/**
 * Response DTO for permission listing and detail endpoints.
 * Module diwakili oleh moduleSlug (String) untuk menghindari circular reference
 * dengan ModuleResponse dan RoleResponse.
 */

@Data
public class PermissionResponse {
    private Long id;
    private String slug;
    private String name;
    private String moduleSlug;
}
