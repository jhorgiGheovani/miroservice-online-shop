package com.jhorgi.auth_service.dto;

import lombok.Data;

@Data
public class TokenRequest {
    private String username;
    private String password;
}
