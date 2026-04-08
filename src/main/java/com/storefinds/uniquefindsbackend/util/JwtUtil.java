package com.storefinds.uniquefindsbackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expireSeconds;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Initialize JWT secret key and expiration settings from configuration.
     * Params:
     * - secret: JWT signing secret
     * - expireSeconds: JWT expiration seconds
     * Returns: None
     * Throws: None
     */
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expire-seconds:7200}") long expireSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Generate signed JWT for authenticated user.
     * Params:
     * - userId: user id
     * - username: username
     * - role: role value
     * Returns:
     * - String: signed JWT token
     * Throws: None
     */
    public String generateToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Parse and validate JWT token, then return claims payload.
     * Params:
     * - token: bearer token string
     * Returns:
     * - Claims: parsed JWT claims
     * Throws:
     * - io.jsonwebtoken.JwtException: when token is invalid/expired
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
