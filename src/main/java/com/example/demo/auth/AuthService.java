package com.example.demo.auth;

import com.example.demo.auth.dto.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        // Minimal stub: In a future iteration, persist the user and hash password.
        String access = jwtService.generateAccessToken(req.email());
        String refresh = generateRefreshToken();
        return new AuthResponse(access, refresh);
    }

    public AuthResponse login(LoginRequest req) {
        // Minimal stub: accept any credentials (no-op). Replace with AuthenticationManager.
        String access = jwtService.generateAccessToken(req.email());
        String refresh = generateRefreshToken();
        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        // Minimal stub simply issues new tokens; in real impl, validate/rotate stored token.
        String subject = "user@example.com"; // unknown from refresh only; placeholder
        String access = jwtService.generateAccessToken(subject);
        String rotated = generateRefreshToken();
        return new AuthResponse(access, rotated);
    }

    public void logout(LogoutRequest req, String subject) {
        // Minimal stub: no-op
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}
