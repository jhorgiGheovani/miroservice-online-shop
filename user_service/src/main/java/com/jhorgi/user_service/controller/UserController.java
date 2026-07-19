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
                    "username", user.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validate(@RequestBody ValidateRequest request) {
        boolean valid = userService.validate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
