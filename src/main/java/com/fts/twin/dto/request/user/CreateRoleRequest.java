package com.fts.twin.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating a role via POST endpoint.
 */
@Data
public class CreateRoleRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "Slug may only contain lowercase letters, numbers, hyphens and underscores")
    @Size(min = 2, max = 50)
    private String slug;
    
    private List<Long> permissionIds;
}
