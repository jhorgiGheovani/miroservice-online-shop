package com.jhorgi.auth_service.client;

import com.jhorgi.auth_service.dto.TokenRequest;
import com.jhorgi.auth_service.dto.ValidateUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${app.user-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ValidateUserResponse validateCredentials(TokenRequest request) {
        ValidateUserResponse response = restClient.post()
                .uri("/api/users/validate")
                .body(request)
                .retrieve()
                .body(ValidateUserResponse.class);

        if (response == null || !response.isValid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return response;
    }
}
