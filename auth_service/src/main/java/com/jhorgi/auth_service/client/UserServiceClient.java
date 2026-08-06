package com.jhorgi.auth_service.client;

import com.jhorgi.auth_service.dto.TokenRequest;
import com.jhorgi.auth_service.dto.ValidateUserResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import com.jhorgi.auth_service.grpc.UserServiceGrpc;
import com.jhorgi.auth_service.grpc.ValidateRequest;
import com.jhorgi.auth_service.grpc.ValidateResponse;

@Component
public class UserServiceClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public ValidateUserResponse validateCredentials(TokenRequest request) {
        ValidateResponse response = userServiceStub.validateCredentials(
                ValidateRequest.newBuilder()
                        .setUsername(request.getUsername())
                        .setPassword(request.getPassword())
                        .build());

        if (!response.getValid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        ValidateUserResponse result = new ValidateUserResponse();
        result.setValid(true);
        result.setEmail(response.getEmail());
        return result;
    }
}
