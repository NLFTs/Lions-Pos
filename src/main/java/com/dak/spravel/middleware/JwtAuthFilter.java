package com.dak.spravel.middleware;

import com.dak.spravel.service.auth.PermissionCacheService;
import com.dak.spravel.util.JwtUtil;
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
        if (token != null && jwtUtil.validateToken(token)) {
            setAuthenticationContext(token, request);
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
        // 1. Ambil semua string authority (bisa Permission + Role) dari Cache
        
        List<String> rawAuthorities = new java.util.ArrayList<>(permissionCacheService.getPermissions(username));
        
        // log.info("[DEBUG] Raw Authorities dari Cache untuk {}: {}", username, rawAuthorities);

        // 2. Map ke SimpleGrantedAuthority
        // Kita tambahin logic: kalau authority-nya 'admin', kita daftarin dua kali (pake ROLE_ dan nggak) biar aman
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        for (String authStr : rawAuthorities) {
            authorities.add(new SimpleGrantedAuthority(authStr));
            // Spring Security @PreAuthorize("hasRole('admin')") nyari "ROLE_admin"
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
        
        // log.info("[DEBUG] Final Authorities di SecurityContext: {}", authorities);
        

        // // Resolve permissions from Caffeine cache (falls back to DB on cache miss)
        // var authorities = permissionCacheService.getPermissions(username).stream()
        //         .map(SimpleGrantedAuthority::new)
        //         .toList();

        // // BUAT OBJEK USER DETAILS DISINI (dari org.springframework.security.core.userdetails.User)
        // UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
        //         .username(username)
        //         .password("") // Kosongkan karena sudah terautentikasi via JWT
        //         .authorities(authorities)
        //         .build();

        // var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        // auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
