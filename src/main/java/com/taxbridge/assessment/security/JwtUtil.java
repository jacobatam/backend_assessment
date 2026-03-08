package com.taxbridge.assessment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims extractClaims(String bearerToken) {

        String token = bearerToken.replace("Bearer ", "");

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractTenantId(String bearerToken) {

        Claims claims = extractClaims(bearerToken);

        return UUID.fromString(
                claims.get("tenantId", String.class)
        );
    }

    public UUID extractUserId(String bearerToken) {

        Claims claims = extractClaims(bearerToken);

        return UUID.fromString(
                claims.getSubject()
        );
    }

    public String extractRole(String bearerToken) {

        Claims claims = extractClaims(bearerToken);

        return claims.get("role", String.class);
    }

    public boolean isValid(String bearerToken) {

        try {
            extractClaims(bearerToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}