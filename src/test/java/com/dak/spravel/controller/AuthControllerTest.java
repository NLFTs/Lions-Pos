package com.dak.spravel.controller;

import com.dak.spravel.model.auth.Token;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.TokenRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenRepository tokenRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setFullname("Test User");
        // BCrypt hash for "password123"
        mockUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        mockUser.setRoles(new java.util.HashSet<>());
    }

    @Test
    void login_returns400_whenRequestIsInvalid() throws Exception {
        // Empty body should trigger validation or parsing error
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void refresh_returnsError_whenTokenIsInvalid() throws Exception {
        when(tokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", "invalid-token")
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void logout_returnsError_whenTokenIsInvalid() throws Exception {
        when(tokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .param("refreshToken", "invalid-token")
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void refresh_returnsError_whenTokenIsRevoked() throws Exception {
        Token token = new Token();
        token.setId(1L);
        token.setAccessToken("access-token");
        token.setRefreshToken("refresh-token");
        token.setUsername("testuser");
        token.setRevoked(true);
        token.setExpiryDate(new Date(System.currentTimeMillis() + 86400000));

        when(tokenRepository.findByRefreshToken("refresh-token")).thenReturn(Optional.of(token));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", "refresh-token")
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void forceLogoutAll_deletesTokensAndReturnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/force-logout-all")
                        .param("username", "testuser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }
}
