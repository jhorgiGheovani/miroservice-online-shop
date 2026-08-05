package com.jhorgi.user_service.controller;

import com.jhorgi.user_service.dto.RegisterRequest;
import com.jhorgi.user_service.dto.ValidateRequest;
import com.jhorgi.user_service.entity.User;
import com.jhorgi.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody ValidateRequest request) {
        Optional<User> user = userService.validate(request.getUsername(), request.getPassword());
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of("valid", true, "email", user.get().getEmail()));
        }
        return ResponseEntity.ok(Map.of("valid", false));
    }
}
