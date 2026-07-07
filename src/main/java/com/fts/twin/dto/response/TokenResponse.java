package com.fts.twin.dto.response;

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
