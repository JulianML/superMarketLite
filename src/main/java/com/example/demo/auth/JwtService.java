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

    public String generateAccessToken(String subject) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(subject)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(accessExpMinutes, ChronoUnit.MINUTES)))
                .withJWTId(UUID.randomUUID().toString())
                .sign(Algorithm.HMAC256(secret));
    }

    public String extractSubject(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token).getSubject();
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        return jwt.getSubject().equals(expectedSubject) && jwt.getExpiresAt().toInstant().isAfter(Instant.now());
    }
}
