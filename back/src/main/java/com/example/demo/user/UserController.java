package com.example.demo.user;

import com.example.demo.auth.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        String email = auth.getName();
        Long businessId = (Long) auth.getDetails();
        List<String> roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList();
        return ResponseEntity.ok(new UserResponse(null, email, null, null, businessId, roles));
    }
}
