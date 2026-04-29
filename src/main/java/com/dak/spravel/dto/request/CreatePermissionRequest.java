package com.dak.spravel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for creating a permission via POST endpoint.
 */
@Data
public class CreatePermissionRequest {
    @NotBlank(message = "Slug is required")
    @Pattern(
        regexp = "^[a-z0-9]+\\.[a-z0-9_]+$",
        message = "Slug must be in format 'module.action' (e.g. post.index)"
    )
    private String slug;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Module is required")
    private Long moduleId;
}
