package org.sleepless_artery.user_service.grpc.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.devh.boot.grpc.server.service.GrpcService;
import org.sleepless_artery.user_service.CreateUserRequest;
import org.sleepless_artery.user_service.CreateUserResponse;
import org.sleepless_artery.user_service.UserCreationServiceGrpc;
import org.sleepless_artery.user_service.dto.UserRequestDto;
import org.sleepless_artery.user_service.exception.UserAlreadyExistsException;
import org.sleepless_artery.user_service.service.UserService;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserCreationServiceImpl extends UserCreationServiceGrpc.UserCreationServiceImplBase {

    private final UserService userService;


    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseStreamObserver) {
        try {
            userService.createUser(
                    new UserRequestDto(request.getEmailAddress(), "User", "")
            );

            responseStreamObserver.onNext(CreateUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User created successfully")
                    .build()
            );

            responseStreamObserver.onCompleted();
        } catch (UserAlreadyExistsException e) {
            log.warn("User with email address '{}' already exists", request.getEmailAddress());

            responseStreamObserver.onNext(CreateUserResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("User already exists")
                    .build()
            );
            
            responseStreamObserver.onCompleted();
        } catch (Exception e) {
            log.error("An error occurred on the grpc server side while creating user: {}", e.getMessage());

            responseStreamObserver.onError(Status.INTERNAL
                    .withDescription("Error creating user")
                    .asRuntimeException()
            );
        }
    }
}
