package com.example.demo.auth;

import com.example.demo.auth.dto.*;
import com.example.demo.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest req) {
        String access = jwtService.generateAccessToken(req.email(), null, List.of());
        return new AuthResponse(access, generateRefreshToken());
    }

    public AuthResponse login(LoginRequest req) {
        var user = userRepository.findByEmail(req.usernameOrEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        Long businessId = userRepository.findOwnedBusinessId(user.getId()).orElse(null);
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();

        String access = jwtService.generateAccessToken(user.getEmail(), businessId, roles);
        return new AuthResponse(access, generateRefreshToken());
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        String access = jwtService.generateAccessToken("user@example.com", null, List.of());
        return new AuthResponse(access, generateRefreshToken());
    }

    public void logout(LogoutRequest req, Principal principal) {
        // no-op: JWT es stateless; el cliente debe descartar el token
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}
