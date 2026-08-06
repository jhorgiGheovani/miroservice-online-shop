package com.jhorgi.user_service.grpc;

import com.jhorgi.user_service.entity.User;
import com.jhorgi.user_service.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class GrpcUserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void validateCredentials(ValidateRequest request, StreamObserver<ValidateResponse> responseObserver) {
        Optional<User> user = userService.validate(request.getUsername(), request.getPassword());

        ValidateResponse response = user
                .map(u -> ValidateResponse.newBuilder()
                        .setValid(true)
                        .setEmail(u.getEmail())
                        .build())
                .orElse(ValidateResponse.newBuilder()
                        .setValid(false)
                        .build());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
