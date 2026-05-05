package com.dak.spravel.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for role listing and detail endpoints.
 * Permissions berisi list PermissionResponse yang sudah di-flat (tanpa ModuleResponse object)
 * untuk menghindari circular reference.
 */
@Data
public class RoleResponse {
    private Long id;
    private String slug;
    private String name;
    private List<PermissionResponse> permissions;
}
