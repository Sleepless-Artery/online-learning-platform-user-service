package org.sleepless_artery.user_service.grpc.client;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sleepless_artery.user_service.EmailAddressAvailabilityRequest;
import org.sleepless_artery.user_service.config.grpc.GrpcClientConfig;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceGrpcClient {

    private final GrpcClientConfig grpcClientConfig;


    public boolean changeEmailAddress(String oldEmailAddress, String newEmailAddress) {
        log.info("Sending gRPC request to auth-service for changing email address");

        EmailAddressAvailabilityRequest request = EmailAddressAvailabilityRequest.newBuilder()
                .setOldEmailAddress(oldEmailAddress)
                .setNewEmailAddress(newEmailAddress)
                .build();
        try {
            return grpcClientConfig.emailVerificationServiceBlockingStub()
                    .withDeadlineAfter(30, TimeUnit.SECONDS)
                    .isEmailAddressAvailable(request)
                    .getAvailability();
        } catch (StatusRuntimeException e) {
            log.error("gRPC error: {}", e.getStatus());
            return false;
        }
    }
}
