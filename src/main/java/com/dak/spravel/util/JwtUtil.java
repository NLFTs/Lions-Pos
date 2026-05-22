package com.dak.spravel.util;

import java.util.List;

/**
 * JWT token generation, validation, and claim extraction utilities interface.
 */
public interface JwtUtil {
    String generateAccessToken(String username);
    String generateAccessToken(String username, List<String> permissions);
    String generateRefreshToken(String username);
    String getUsernameFromToken(String token);
    List<String> getPermissionsFromToken(String token);
    boolean validateToken(String token);
}