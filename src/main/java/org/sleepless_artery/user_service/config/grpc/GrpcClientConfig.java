package org.sleepless_artery.user_service.config.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.sleepless_artery.user_service.EmailVerificationServiceGrpc;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GrpcClientConfig {

    @GrpcClient("auth-service")
    private EmailVerificationServiceGrpc.EmailVerificationServiceBlockingStub emailVerificationServiceBlockingStub;

    public EmailVerificationServiceGrpc.EmailVerificationServiceBlockingStub emailVerificationServiceBlockingStub() {
        return emailVerificationServiceBlockingStub;
    }
}
