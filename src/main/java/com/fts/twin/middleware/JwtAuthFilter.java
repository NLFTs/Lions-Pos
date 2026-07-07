package com.fts.twin.middleware;

import com.fts.twin.service.auth.PermissionCacheService;
import com.fts.twin.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * JWT authentication filter for extracting and validating Bearer tokens.
 * <p>
 * Permissions are no longer read from the JWT claims — they are resolved
 * server-side via {@link PermissionCacheService} (Caffeine cache-aside).
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PermissionCacheService permissionCacheService;
    private final List<String> excludedPaths;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return excludedPaths.stream()
                .anyMatch(pattern -> new AntPathMatcher().match(pattern, path));
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("[🔒] JWT Filter");
        String token = extractTokenFromHeader(request);
        try {
            if (token != null) {
                if (jwtUtil.validateToken(token)) {
                    setAuthenticationContext(token, request);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 1. Tangkap kalau tokennya expired!
            log.warn("[⚠️] JWT Token Expired: {}", e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"TOKEN_EXPIRED\", \"message\": \"Access Token sudah kedaluwarsa\"}");
            return; 
            
        } catch (Exception e) {
            log.error("[❌] JWT Authentication Failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"INVALID_TOKEN\", \"message\": \"Token tidak valid\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String[] tokens = authHeader != null ? authHeader.split(" ") : new String[0];
        if (tokens.length == 2 && tokens[0].equals("Bearer")) {
            return tokens[1];
        }
        return null;
    }

    private void setAuthenticationContext(String token, HttpServletRequest request) {
        String username = jwtUtil.getUsernameFromToken(token);
        
        List<String> rawAuthorities = new java.util.ArrayList<>(permissionCacheService.getPermissions(username));
    
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        for (String authStr : rawAuthorities) {
            authorities.add(new SimpleGrantedAuthority(authStr));
            if (!authStr.startsWith("ROLE_")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + authStr));
            }
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("") 
                .authorities(authorities)
                .build();

        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}