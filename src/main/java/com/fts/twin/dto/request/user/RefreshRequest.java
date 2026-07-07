package com.fts.twin.dto.request.user;

import lombok.Data;

/**
 * Request DTO for refreshing JWT tokens.
 */
@Data
public class RefreshRequest {
    private String refreshToken;
}
