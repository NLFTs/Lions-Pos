package com.fts.twin.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating a category via PUT endpoint.
 */
@Data
public class UpdateCategoryRequest {
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String description;
}
