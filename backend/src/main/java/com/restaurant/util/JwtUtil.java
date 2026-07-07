package com.restaurant.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT utility class using jjwt 0.12.6 API.
 *
 * <p>Provides token generation, parsing, and validation for both
 * admin and customer (temporary user) authentication.</p>
 */
@Slf4j
@Component
public class JwtUtil {

    /** JWT secret key from configuration */
    @Value("${jwt.secret}")
    private String secret;

    /** Default token expiration in hours */
    @Value("${jwt.expiration}")
    private int expiration;

    /**
     * Get the signing key derived from the secret string.
     *
     * @return the HMAC-SHA key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token.
     *
     * @param subject     the subject (typically user ID as string)
     * @param claims      additional claims to include (e.g., role)
     * @param expiryHours token validity duration in hours
     * @return the signed JWT token string
     */
    public String generateToken(String subject, Map<String, Object> claims, int expiryHours) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) expiryHours * 3600 * 1000);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a JWT token with default expiration.
     *
     * @param subject the subject (typically user ID as string)
     * @param claims  additional claims to include
     * @return the signed JWT token string
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, expiration);
    }

    /**
     * Parse a JWT token and return the claims.
     *
     * @param token the JWT token string
     * @return the parsed Jws containing claims, or null if invalid
     */
    public Jws<Claims> parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (Exception e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate a JWT token.
     *
     * @param token the JWT token string
     * @return true if the token is valid and not expired
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * Get a specific claim from the token.
     *
     * @param token the JWT token string
     * @param key   the claim key
     * @return the claim value, or null if token is invalid or claim not found
     */
    public Object getClaim(String token, String key) {
        Jws<Claims> jws = parseToken(token);
        if (jws == null) {
            return null;
        }
        Claims claims = jws.getPayload();
        return claims.get(key);
    }

    /**
     * Get the subject from the token.
     *
     * @param token the JWT token string
     * @return the subject, or null if token is invalid
     */
    public String getSubject(String token) {
        Jws<Claims> jws = parseToken(token);
        if (jws == null) {
            return null;
        }
        return jws.getPayload().getSubject();
    }

    /**
     * Extract token from Authorization header value.
     *
     * @param authHeader the Authorization header value (e.g., "Bearer xxx")
     * @return the raw token, or null if format is invalid
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
