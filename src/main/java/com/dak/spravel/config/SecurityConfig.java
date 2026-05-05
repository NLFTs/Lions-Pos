package com.dak.spravel.config;

import com.dak.spravel.handler.CustomAccessDeniedHandler;
import com.dak.spravel.handler.CustomAuthEntryPoint;
import com.dak.spravel.middleware.JwtAuthFilter;
import com.dak.spravel.service.auth.PermissionCacheService;
import com.dak.spravel.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Spring Security configuration with JWT filter and public path definitions.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final PermissionCacheService permissionCacheService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8090"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Public endpoints — no JWT required
        String[] publicApiPaths = {
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/logout",
                "/api/v1/auth/force-logout-all",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml/**",
                "/docs",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/scalar-ui/**",
        };

        // Vue SPA — all under /_/ prefix, Spring just serves the assets
        // Access control is handled by Vue Router on the client.
        String[] webPaths = {
                "/_/**",
                "/favicon.ico",
                "/error",
        };

        String[] jwtExcludedPaths = merge(publicApiPaths, webPaths);

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(jwtExcludedPaths).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint(new CustomAuthEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtil, permissionCacheService, java.util.Arrays.asList(jwtExcludedPaths)),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private String[] merge(String[]... arrays) {
        int len = 0;
        for (String[] a : arrays)
            len += a.length;
        String[] result = new String[len];
        int pos = 0;
        for (String[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }
}
