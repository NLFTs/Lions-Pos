package com.dak.spravel.dto.request.user;

import lombok.Data;

/**
 * Request DTO for updating a permission via PUT endpoint.
 */
@Data
public class UpdatePermissionRequest {
    // Slug is intentionally NOT updatable to avoid breaking @PreAuthorize references.
    // Only display fields (name) and parent module can be changed.

    private String name;
    private Long moduleId;
}
