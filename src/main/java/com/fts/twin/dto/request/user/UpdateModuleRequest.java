package com.fts.twin.dto.request.user;

import lombok.Data;

/**
 * Request DTO for updating a module via PUT endpoint.
 */
@Data
public class UpdateModuleRequest {
    private String name;
    private String description;
}
