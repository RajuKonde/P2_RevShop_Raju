package com.revshop.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.security.jwt-secret}")
    private String secretKey;

    @Value("${app.security.jwt-expiration-ms:86400000}")
    private long tokenExpirationMs;

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = decodeSecret(secretKey);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes long");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        String normalized = key.trim();
        try {
            byte[] decoded = Decoders.BASE64.decode(normalized);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (RuntimeException ignored) {
            // Fall back to raw UTF-8 bytes for non-base64 secrets.
        }

        return normalized.getBytes(StandardCharsets.UTF_8);
    }
}
