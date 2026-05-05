package com.dak.spravel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for JWT token pairs.
 */
@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}
