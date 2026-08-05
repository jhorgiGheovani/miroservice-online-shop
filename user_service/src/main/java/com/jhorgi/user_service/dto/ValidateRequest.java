package com.jhorgi.user_service.dto;

import lombok.Data;

@Data
public class ValidateRequest {
    private String username;
    private String password;
}
