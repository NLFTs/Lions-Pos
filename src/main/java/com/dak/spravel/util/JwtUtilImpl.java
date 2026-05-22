package com.dak.spravel.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Concrete implementation of the JwtUtil interface.
 */
@Component
public class JwtUtilImpl implements JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    @Override
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateAccessToken(String username, List<String> permissions) {
        return Jwts.builder()
                .subject(username)
                .claim("perms", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = getClaims(token);
        // Baca dari "perms" (claim utama), fallback ke "permissions" untuk kompatibilitas token lama
        List<String> permissions = (List<String>) claims.get("perms");
        if (permissions == null) {
            permissions = (List<String>) claims.get("permissions");
        }
        return permissions != null ? permissions : List.of();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e; 
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
