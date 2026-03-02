package com.revshop.security.jwt;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class JwtServiceTest {

    @Test
    public void generateAndValidateToken_withConfiguredSecret() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "RevShopJwtSecretChangeThisToAtLeast32Chars");
        ReflectionTestUtils.setField(jwtService, "tokenExpirationMs", 60000L);

        String token = jwtService.generateToken("buyer@test.com", "BUYER");

        assertNotNull(token);
        assertEquals("buyer@test.com", jwtService.extractEmail(token));
        assertEquals("BUYER", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, "buyer@test.com"));
    }

    @Test
    public void generateToken_throwsWhenSecretTooShort() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "short-secret");
        ReflectionTestUtils.setField(jwtService, "tokenExpirationMs", 60000L);

        assertThrows(IllegalStateException.class,
                () -> jwtService.generateToken("buyer@test.com", "BUYER"));
    }
}
