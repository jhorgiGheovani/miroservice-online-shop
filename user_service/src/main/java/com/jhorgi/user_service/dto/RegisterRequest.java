package com.jhorgi.user_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String username;
    private String password;
}
