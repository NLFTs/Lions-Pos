package com.fts.twin.model.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * JPA entity for JWT token storage with revocation tracking.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tokens")
public class Token implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_type")
    private String tokenType = "Bearer";

    private String username;

    @Column(nullable = false)
    private Boolean revoked = false;

    @Column(name = "expiry_date")
    private Date expiryDate;

    public boolean isRevoked() {
        return Boolean.TRUE.equals(revoked);
    }
}

