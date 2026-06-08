package com.dak.spravel.controller.auth;

import com.dak.spravel.dto.request.user.LoginRequest;
import com.dak.spravel.dto.response.MeResponse;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.TokenResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Token;
import com.dak.spravel.repository.auth.TokenRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.service.auth.PermissionCacheService;
import com.dak.spravel.util.JwtUtil;
import com.dak.spravel.util.ResponseBuilder;
import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles authentication endpoints including login, logout, token refresh,
 * force-logout-all, and retrieving the current user profile.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PermissionCacheService permissionCacheService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    /**
     * GET /api/v1/auth/me
     * Get current authenticated user details.
     */
    @GetMapping("/me")
    public ResponseEntity<ResData<MeResponse>> me(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        var user = userRepository
            .findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MeResponse me = new MeResponse();
        me.setId(user.getId());
        me.setUsername(user.getUsername());
        me.setFullname(user.getFullname());
        me.setAvatar(user.getAvatar());
        me.setRoles(
            user
                .getRoles()
                .stream()
                .map(r -> r.getSlug())
                .toList()
        );
        me.setPermissions(
            user
                .getRoles()
                .stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getSlug())
                .distinct()
                .sorted()
                .toList()
        );
        if (user.getBranch() != null) {
            me.setBranchId(user.getBranch().getId());
            me.setBranchName(user.getBranch().getName());
        }

        if (user.getWarehouse() != null) {
            me.setWarehouseId(user.getWarehouse().getId());
            me.setWarehouseName(user.getWarehouse().getName());
        }

        if (user.getPartner() != null) {
            me.setPartnerId(user.getPartner().getId());
            me.setPartnerName(user.getPartner().getName());
            if (user.getPartner().getPlan() != null) {
                me.setPlan(user.getPartner().getPlan().name().toLowerCase());
            }
        }

        log.info(
            "[AUTH] User '{}' has permissions: {}",
            user.getUsername(),
            me.getPermissions()
        );
        return ResponseBuilder.ok(me);
    }

    /**
     * POST /api/v1/auth/login
     * Authenticate user and return JWT tokens.
     * Permissions are stored in the server-side Caffeine cache (not in the JWT).
     */
    @PostMapping("/login")
    public ResponseEntity<ResData<TokenResponse>> login(
        @RequestBody LoginRequest request
    ) {
        log.info("[POST]: /auth/login");
        var user = userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("Username atau password salah")
            );

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (
            !passwordEncoder.matches(request.getPassword(), user.getPassword())
        ) {
            throw new IllegalArgumentException("Username atau password salah");
        }

        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new IllegalArgumentException("Akun Anda ditangguhkan. Hubungi pemilik mitra.");
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new ResourceNotFoundException("User tidak mempunyai role");
        }

        boolean isAdmin = user
            .getRoles()
            .stream()
            .anyMatch(role -> role.getSlug().equals("admin"));

        boolean isPartnerInactive = (user.getPartner() != null &&
            !user.getPartner().getIsActive());

        if (isPartnerInactive && !isAdmin) {
            throw new ResourceNotFoundException("Partner tidak aktif");
        }

        // Collect roles and permissions to warm the server-side cache
        Set<String> auths = new java.util.HashSet<>();
        user.getRoles().forEach(role -> {
            auths.add(role.getSlug());
            role.getPermissions().forEach(p -> auths.add(p.getSlug()));
        });
        permissionCacheService.putPermissions(user.getUsername(), auths);

        // Revoke all existing tokens for this user
        tokenRepository.findAllByUsername(user.getUsername()).forEach(t -> {
            t.setRevoked(true);
            tokenRepository.save(t);
        });

        // Access token carries permissions claim for frontend to decode on refresh
        String accessToken = jwtUtil.generateAccessToken(
            user.getUsername(),
            auths.stream().toList()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        Date expiryDate = new Date(
            System.currentTimeMillis() + refreshTokenExpirationMs
        );

        Token tokenEntity = new Token();
        tokenEntity.setAccessToken(accessToken);
        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setTokenType("Bearer");
        tokenEntity.setUsername(user.getUsername());
        tokenEntity.setExpiryDate(expiryDate);
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);

        return ResponseBuilder.ok(new TokenResponse(accessToken, refreshToken));
    }

    /**
     * POST /api/v1/auth/refresh
     * Refresh access token using a valid refresh token.
     * Re-loads permissions from the DB and refreshes the cache.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResData<TokenResponse>> refresh(
        @RequestParam String refreshToken
    ) {
        Token tokenEntity = tokenRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow(() ->
                new ResourceNotFoundException("Refresh token tidak valid")
            );

        if (
            tokenEntity.isRevoked() ||
            tokenEntity.getExpiryDate().before(new Date())
        ) {
            throw new IllegalArgumentException(
                "Refresh token sudah kedaluwarsa atau dicabut"
            );
        }

        // Reload permissions from DB and refresh the cache
        var freshUser = userRepository
            .findByUsername(tokenEntity.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("Pengguna tidak ditemukan")
            );

        if (freshUser.getIsActive() != null && !freshUser.getIsActive()) {
            tokenEntity.setRevoked(true);
            tokenRepository.save(tokenEntity);
            permissionCacheService.evict(freshUser.getUsername());
            throw new IllegalArgumentException("Akun Anda ditangguhkan. Hubungi pemilik mitra.");
        }

        // Reload roles and permissions from DB and refresh the cache
        Set<String> auths = new java.util.HashSet<>();
        freshUser.getRoles().forEach(role -> {
            auths.add(role.getSlug());
            role.getPermissions().forEach(p -> auths.add(p.getSlug()));
        });
        permissionCacheService.putPermissions(freshUser.getUsername(), auths);

        String newAccessToken = jwtUtil.generateAccessToken(
            tokenEntity.getUsername(),
            auths.stream().toList()
        );
        tokenEntity.setAccessToken(newAccessToken);
        tokenRepository.save(tokenEntity);

        return ResponseBuilder.ok(
            new TokenResponse(newAccessToken, refreshToken)
        );
    }

    /**
     * POST /api/v1/auth/logout
     * Revoke tokens and invalidate permission cache.
     */
    @PostMapping("/logout")
    public ResponseEntity<ResData<Void>> logout(
        @RequestParam String refreshToken
    ) {
        Token tokenEntity = tokenRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow(() ->
                new ResourceNotFoundException("Refresh token tidak valid")
            );
        tokenEntity.setRevoked(true);
        tokenRepository.save(tokenEntity);
        permissionCacheService.evict(tokenEntity.getUsername());
        return ResponseBuilder.ok();
    }

    /**
     * POST /api/v1/auth/force-logout-all
     * Revoke all tokens for a specific user and invalidate their cache.
     */
    @PostMapping("/force-logout-all")
    public ResponseEntity<ResData<Void>> forceLogoutAll(
        @RequestParam String username
    ) {
        tokenRepository.deleteAllByUsername(username);
        permissionCacheService.evict(username);
        return ResponseBuilder.ok();
    }
}
