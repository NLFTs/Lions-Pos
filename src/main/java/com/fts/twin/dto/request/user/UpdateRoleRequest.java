package com.fts.twin.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating a role via PUT endpoint.
 */
@Data
public class UpdateRoleRequest {
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = "^[a-z0-9_-]+$", message = "Slug may only contain lowercase letters, numbers, hyphens and underscores")
    @Size(min = 2, max = 50)
    private String slug;
}
