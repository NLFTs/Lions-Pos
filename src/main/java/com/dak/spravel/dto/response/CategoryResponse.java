package com.dak.spravel.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for category listing and detail endpoints.
 */
@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
