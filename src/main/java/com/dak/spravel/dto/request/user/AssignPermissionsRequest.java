package com.dak.spravel.dto.request.user;

import lombok.Data;

import java.util.List;

/**
 * Request DTO for bulk permission assignment to roles.
 */
@Data
public class AssignPermissionsRequest {
    // Replaces ALL current permissions for the role.
    // Send an empty list to revoke all permissions.
    private List<Long> permissionIds;
}
