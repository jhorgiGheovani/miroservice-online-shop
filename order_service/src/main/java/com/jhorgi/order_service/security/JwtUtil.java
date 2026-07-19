package com.jhorgi.order_service.security;

import com.jhorgi.order_service.config.RsaKeyConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final RsaKeyConfig rsaKeyConfig;

    public JwtUtil(RsaKeyConfig rsaKeyConfig) {
        this.rsaKeyConfig = rsaKeyConfig;
    }

    public Claims validateAndExtract(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(rsaKeyConfig.loadPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new io.jsonwebtoken.JwtException("Invalid or expired token: " + e.getMessage(), e);
        }
    }
}
