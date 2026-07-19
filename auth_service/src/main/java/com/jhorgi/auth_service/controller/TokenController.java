package com.jhorgi.auth_service.controller;

import com.jhorgi.auth_service.dto.TokenRequest;
import com.jhorgi.auth_service.dto.TokenResponse;
import com.jhorgi.auth_service.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(tokenService.generateToken(request));
    }
}
