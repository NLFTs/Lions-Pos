package com.dak.spravel.dto.request;

import lombok.Data;

/**
 * Request DTO for user authentication.
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
