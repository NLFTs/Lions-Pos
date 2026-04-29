package com.dak.spravel.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for post listing and detail endpoints with category data.
 * Uses flat structure (categoryId, categoryName) to avoid circular reference
 * with CategoryResponse.
 */
@Data
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String status;
    private Long categoryId;
    private String categoryName;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
