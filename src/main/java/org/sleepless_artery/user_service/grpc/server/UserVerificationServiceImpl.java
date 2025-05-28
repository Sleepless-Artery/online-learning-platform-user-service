package org.sleepless_artery.user_service.grpc.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.sleepless_artery.user_service.UserVerificationServiceGrpc;
import org.sleepless_artery.user_service.VerifyUserExistenceRequest;
import org.sleepless_artery.user_service.VerifyUserExistenceResponse;
import org.sleepless_artery.user_service.service.UserService;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserVerificationServiceImpl extends UserVerificationServiceGrpc.UserVerificationServiceImplBase {

    private final UserService userService;


    @Override
    public void verifyUserExistence(
            VerifyUserExistenceRequest request, StreamObserver<VerifyUserExistenceResponse> responseStreamObserver
    ) {
        try {
            responseStreamObserver.onNext(
                    VerifyUserExistenceResponse.newBuilder()
                            .setExistence(userService.existsById(request.getId()))
                            .build()
            );
            responseStreamObserver.onCompleted();
        } catch (Exception e) {
            log.error("An error occurred on the grpc server side while verifying user's existence: {}", e.getMessage());

            responseStreamObserver.onError(Status.INTERNAL
                    .withDescription("Error verifying user's existence")
                    .asRuntimeException());
        }
    }

}
