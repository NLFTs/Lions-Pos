package com.dak.spravel.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "app.jwt.secret=dGhpc19pc19hX3Zlcnlfc2VjdXJlX3NlY3JldF9mb3Jfand0IQ==",
    "app.jwt.access-token-expiration=3600000",
    "app.jwt.refresh-token-expiration=604800000"
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAccessToken_createsValidTokenWithUsernameAndPermissions() {
        String token = jwtUtil.generateAccessToken("testuser", List.of("READ", "WRITE"));
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
        assertEquals(List.of("READ", "WRITE"), jwtUtil.getPermissionsFromToken(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void generateAccessToken_withNoPermissions_createsValidToken() {
        String token = jwtUtil.generateAccessToken("testuser");
        assertNotNull(token);
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
        assertTrue(jwtUtil.getPermissionsFromToken(token).isEmpty());
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void generateRefreshToken_createsValidToken() {
        String token = jwtUtil.generateRefreshToken("testuser");
        assertNotNull(token);
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
        assertTrue(jwtUtil.validateToken(token));
        assertTrue(jwtUtil.getPermissionsFromToken(token).isEmpty());
    }

    @Test
    void getUsernameFromToken_extractsCorrectly() {
        String token = jwtUtil.generateAccessToken("alice@example.com", List.of("ADMIN"));
        assertEquals("alice@example.com", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void getPermissionsFromToken_extractsCorrectly() {
        String token = jwtUtil.generateAccessToken("user", List.of("READ", "WRITE", "DELETE"));
        List<String> extracted = jwtUtil.getPermissionsFromToken(token);
        assertEquals(3, extracted.size());
        assertTrue(extracted.containsAll(List.of("READ", "WRITE", "DELETE")));
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtUtil.generateAccessToken("user", List.of("READ"));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateAccessToken("user", List.of("READ"));
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    void validateToken_returnsFalseForEmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void validateToken_returnsFalseForNullToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_returnsFalseForMalformedToken() {
        assertFalse(jwtUtil.validateToken("this.is.not.a.valid.jwt"));
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() throws InterruptedException {
        JwtUtil shortLivedUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedUtil, "jwtSecret",
                "dGhpc19pc19hX3Zlcnlfc2VjdXJlX3NlY3JldF9mb3Jfand0IQ==");
        ReflectionTestUtils.setField(shortLivedUtil, "accessTokenExpirationMs", 1L);
        ReflectionTestUtils.setField(shortLivedUtil, "refreshTokenExpirationMs", 604800000L);

        String token = shortLivedUtil.generateAccessToken("user", List.of("READ"));
        Thread.sleep(50);
        assertFalse(shortLivedUtil.validateToken(token));
    }

    @Test
    void validateToken_returnsFalseForTokenFromDifferentSecret() {
        JwtUtil otherUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherUtil, "jwtSecret",
                "YW5vdGhlcl9zZWNyZXRfa2V5X3RoYXRfaXNfdmVyeV9sb25nX2VuYXVnaA==");
        ReflectionTestUtils.setField(otherUtil, "accessTokenExpirationMs", 3600000L);
        ReflectionTestUtils.setField(otherUtil, "refreshTokenExpirationMs", 604800000L);

        String tokenFromOther = otherUtil.generateAccessToken("user", List.of("READ"));
        assertFalse(jwtUtil.validateToken(tokenFromOther));
    }

    @Test
    void accessAndRefreshTokensAreDifferent() {
        String access = jwtUtil.generateAccessToken("user", List.of("READ"));
        String refresh = jwtUtil.generateRefreshToken("user");
        assertNotEquals(access, refresh);
    }
}
