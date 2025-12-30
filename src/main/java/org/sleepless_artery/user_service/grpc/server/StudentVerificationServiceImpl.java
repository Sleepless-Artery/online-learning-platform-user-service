package org.sleepless_artery.user_service.grpc.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.sleepless_artery.user_service.*;
import org.sleepless_artery.user_service.service.UserService;


@GrpcService
@RequiredArgsConstructor
public class StudentVerificationServiceImpl extends StudentVerificationServiceGrpc.StudentVerificationServiceImplBase {

    private final UserService userService;


    @Override
    public void verifyStudentExistence(
            VerifyStudentExistenceRequest request, StreamObserver<VerifyStudentExistenceResponse> responseStreamObserver
    ) {
        try {
            responseStreamObserver.onNext(
                    VerifyStudentExistenceResponse.newBuilder()
                            .setExistence(userService.existsById(request.getStudentId()))
                            .build()
            );
            responseStreamObserver.onCompleted();
        } catch (Exception e) {

            responseStreamObserver.onError(Status.INTERNAL
                    .withDescription("Error verifying student's existence")
                    .asRuntimeException());
        }
    }
}
