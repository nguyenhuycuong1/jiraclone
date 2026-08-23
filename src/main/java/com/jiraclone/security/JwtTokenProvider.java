package com.jiraclone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

@Component
public class JwtTokenProvider {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(CustomUserDetails userDetails, String orgId) {
        var builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs));
        if (orgId != null) {
            builder.claim("org_id", orgId);
        }
        return builder.signWith(getSigningKey()).compact();
    }

    public JwtPayload extractJwtPayload(String token) {
        JwtPayload payload = new JwtPayload();
        Claims claims = parseClaims(token);
        payload.setUsername(claims.getSubject());
        payload.setOrgId(claims.get("org_id", String.class));
        return payload;
    }

    public boolean validateToken(String token, CustomUserDetails userDetails) {
        JwtPayload jwtPayload = extractJwtPayload(token);
        if (!jwtPayload.getUsername().equals(userDetails.getUsername())) return false;
        if (isTokenExpired(token)) return false;
        return true;
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
