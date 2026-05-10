package com.example.demo.auth;

import java.util.List;

public record UserResponse(Long id, String email, String firstName, String lastName, Long businessId, List<String> roles) {}
