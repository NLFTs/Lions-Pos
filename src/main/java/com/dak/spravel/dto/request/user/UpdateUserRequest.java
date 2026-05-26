package com.dak.spravel.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for updating a user via PUT endpoint.
 */
@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullname;

    private String email;

    private String avatar;

    private String password;

    private List<Long> roleIds;

    private Long branchId;

    private Long warehouseId;
}
