package com.dak.spravel.dto.request.user;

import lombok.Data;

/**
 * Request DTO for user authentication.
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
