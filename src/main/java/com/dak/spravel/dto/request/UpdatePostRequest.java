package com.dak.spravel.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating a post via PUT endpoint.
 */
@Data
public class UpdatePostRequest {
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    private String content;

    @Pattern(regexp = "^(DRAFT|PUBLISHED)$", message = "Status must be DRAFT or PUBLISHED")
    private String status;

    private Long categoryId;
}
