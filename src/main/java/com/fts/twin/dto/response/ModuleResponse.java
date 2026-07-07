package com.fts.twin.dto.response;

import lombok.Data;

/**
 * Response DTO for module listing and detail endpoints.
 */
@Data
public class ModuleResponse {
    private Long id;
    private String slug;
    private String name;
    private String description;
}
