package com.shebadesk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String DEFAULT_SECRET = "change-me-to-a-long-random-secret-at-least-32-chars";
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${JWT_SECRET:" + DEFAULT_SECRET + "}") String secret,
            @Value("${JWT_EXPIRATION_MS:86400000}") long expirationMs) {
        if (DEFAULT_SECRET.equals(secret)) {
            log.warn("JWT_SECRET is not set - using insecure dev default. Set JWT_SECRET in production!");
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters (256 bits) for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
