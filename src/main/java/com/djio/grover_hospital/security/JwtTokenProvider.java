package com.djio.grover_hospital.security;


import com.djio.grover_hospital.model.enums.Role;
import io.jsonwebtoken.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private static final String DOWNLOAD_TOKEN_TYPE = "result-download";
    @Getter
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final long resultDownloadExpirationMs;


    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${app.result-download.token-expiry-minutes:30}") long resultDownloadExpiryMinutes
    ) {
        byte[] secretBytes = Base64.getEncoder().encodeToString(jwtSecret.getBytes()).getBytes();
        byte[] keyBytes = secretBytes.length >= 64 ? secretBytes : Arrays.copyOf(secretBytes, 64);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.resultDownloadExpirationMs = resultDownloadExpiryMinutes * 60_000L;
    }

    public String generateAccessToken(Long userId, String email, Role role) {
        return buildToken(userId, email, role, accessExpirationMs, "access");
    }

    public String generateRefreshToken(Long userId, String email, Role role) {
        return buildToken(userId, email, role, refreshExpirationMs, "refresh");
    }

    public String buildToken(Long userId, String email, Role role, long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** Mint a short-lived download token bound to a specific patient + result file. */
    public String generateResultDownloadToken(Long patientId, Long resultId, Long fileId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + resultDownloadExpirationMs);

        return Jwts.builder()
                .subject(patientId.toString())
                .claim("type", DOWNLOAD_TOKEN_TYPE)
                .claim("rid", resultId)
                .claim("fid", fileId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** Parse a download token. Throws if signature invalid, expired, or wrong type. */
    public ResultDownloadToken parseResultDownloadToken(String token) {
        Claims claims = getClaims(token);
        if (!DOWNLOAD_TOKEN_TYPE.equals(claims.get("type", String.class))) {
            throw new io.jsonwebtoken.JwtException("Invalid token type");
        }
        return new ResultDownloadToken(
                Long.parseLong(claims.getSubject()),
                ((Number) claims.get("rid")).longValue(),
                ((Number) claims.get("fid")).longValue()
        );
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    public Role getRoleFromToken(String token) {
        return Role.valueOf(getClaims(token).get("role", String.class));
    }

    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.warn("Unsupported JWT: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.warn("Malformed JWT: {}", ex.getMessage());
        } catch (SecurityException ex) {
            logger.warn("Invalid JWT exception: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("JWT claims string is empty: {}", ex.getMessage());
        }

        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}