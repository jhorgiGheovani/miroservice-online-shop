package com.jhorgi.auth_service.service;

import com.jhorgi.auth_service.client.UserServiceClient;
import com.jhorgi.auth_service.config.RsaKeyConfig;
import com.jhorgi.auth_service.dto.TokenRequest;
import com.jhorgi.auth_service.dto.TokenResponse;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    private final RsaKeyConfig rsaKeyConfig;
    private final UserServiceClient userServiceClient;
    private final long expirationMs;

    public TokenService(RsaKeyConfig rsaKeyConfig,
                        UserServiceClient userServiceClient,
                        @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.rsaKeyConfig = rsaKeyConfig;
        this.userServiceClient = userServiceClient;
        this.expirationMs = expirationMs;
    }

    public TokenResponse generateToken(TokenRequest request) {
        userServiceClient.validateCredentials(request);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(request.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(rsaKeyConfig.getPrivateKey())
                .compact();

        return new TokenResponse(token, "Bearer", expirationMs / 1000);
    }
}
