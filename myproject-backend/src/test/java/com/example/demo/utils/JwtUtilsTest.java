package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "my-secret-key-for-jwt-signing-must-be-at-least-256-bits-long-enough");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400000L);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String token = jwtUtils.generateToken(1L, "admin");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void parseToken_shouldReturnCorrectClaims() {
        String token = jwtUtils.generateToken(1L, "admin");
        Claims claims = jwtUtils.parseToken(token);

        assertEquals("1", claims.getSubject());
        assertEquals("admin", claims.get("username", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtUtils.generateToken(1L, "admin");
        Long userId = jwtUtils.getUserIdFromToken(token);
        assertEquals(1L, userId);
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtUtils.generateToken(1L, "admin");
        String username = jwtUtils.getUsernameFromToken(token);
        assertEquals("admin", username);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtils.generateToken(1L, "admin");
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid-token"));
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyToken() {
        assertFalse(jwtUtils.validateToken(""));
    }
}
