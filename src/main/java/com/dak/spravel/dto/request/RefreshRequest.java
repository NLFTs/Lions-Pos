package com.dak.spravel.dto.request;

import lombok.Data;

/**
 * Request DTO for refreshing JWT tokens.
 */
@Data
public class RefreshRequest {
    private String refreshToken;
}
