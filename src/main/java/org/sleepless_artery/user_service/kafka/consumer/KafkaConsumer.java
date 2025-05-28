package org.sleepless_artery.user_service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.sleepless_artery.user_service.service.EmailChangeService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final EmailChangeService emailChangeService;

    @KafkaListener(topics = "auth.users.email-changed", groupId = "user-service")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        emailChangeService.confirmEmailAddressChange(key, message);
    }
}
