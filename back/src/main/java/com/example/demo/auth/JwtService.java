package com.example.demo.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${security.jwt.secret:change-me}")
    private String secret;

    @Value("${security.jwt.access-expiration-minutes:15}")
    private long accessExpMinutes;

    @Value("${security.jwt.issuer:market-api}")
    private String issuer;

    public String generateAccessToken(String subject, Long businessId, java.util.List<String> roles) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(subject)
                .withClaim("businessId", businessId)
                .withClaim("roles", roles)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(accessExpMinutes, ChronoUnit.MINUTES)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(secret));
    }

    public String extractSubject(String token) {
        return decode(token).getSubject();
    }

    public Long extractBusinessId(String token) {
        return decode(token).getClaim("businessId").asLong();
    }

    public java.util.List<String> extractRoles(String token) {
        java.util.List<String> roles = decode(token).getClaim("roles").asList(String.class);
        return roles != null ? roles : java.util.List.of();
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        DecodedJWT jwt = decode(token);
        return jwt.getSubject().equals(expectedSubject) && jwt.getExpiresAt().toInstant().isAfter(Instant.now());
    }

    private DecodedJWT decode(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).withIssuer(issuer).build().verify(token);
    }
}
