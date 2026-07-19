package com.jhorgi.auth_service.controller;

import com.jhorgi.auth_service.service.KeyRotationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class KeyRotationController {

    private final KeyRotationService keyRotationService;

    public KeyRotationController(KeyRotationService keyRotationService) {
        this.keyRotationService = keyRotationService;
    }

    @PostMapping("/rotate-keys")
    public ResponseEntity<String> rotateKeys() throws Exception {
        keyRotationService.rotateKeys();
        return ResponseEntity.ok("Key rotation completed. New public key published to Kafka.");
    }
}
