package com.ridelink.ride.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    private SecretKey signingKey;

    /**
     * Initialize JWT signing key when application starts.
     */
    @PostConstruct
    void initializeSigningKey() {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret must not be null or blank"
            );
        }

        byte[] secretBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        /*
         * HS256 requires at least 256 bits = 32 bytes.
         */
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes"
            );
        }

        signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    /**
     * Parse and verify JWT token.
     */
    public Claims parseClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token must not be null or blank"
            );
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract subject from JWT.
     *
     * Usually this is the Account ID / user ID.
     */
    public String extractSubject(String token) {

        return parseClaims(token)
                .getSubject();
    }

    /**
     * Extract role from JWT.
     *
     * Expected examples:
     *
     * PASSENGER
     * DRIVER
     * ADMIN
     *
     * or:
     *
     * ROLE_PASSENGER
     * ROLE_DRIVER
     * ROLE_ADMIN
     */
    public String extractRole(String token) {

        return parseClaims(token)
                .get("role", String.class);
    }

    /**
     * Extract email from JWT.
     */
    public String extractEmail(String token) {

        return parseClaims(token)
                .get("email", String.class);
    }

    /**
     * Validate JWT signature and claims.
     */
    public boolean isTokenValid(String token) {

        try {

            parseClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException exception) {

            System.out.println(
                    "JWT VALIDATION FAILED: "
                            + exception.getMessage()
            );

            return false;
        }
    }
}