package com.jhorgi.auth_service.dto;

import lombok.Data;

@Data
public class ValidateUserResponse {
    private boolean valid;
    private String email;
}
