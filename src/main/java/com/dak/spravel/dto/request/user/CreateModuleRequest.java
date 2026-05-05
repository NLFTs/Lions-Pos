package com.dak.spravel.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for creating a module via POST endpoint.
 */
@Data
public class CreateModuleRequest {
    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Slug must be lowercase alphanumeric (a-z, 0-9, _)")
    private String slug;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
