package com.aiexpense.notificationservice.consumer;


import com.aiexpense.notificationservice.entity.NotificationRecipient;
import com.aiexpense.notificationservice.event.UserRegisteredEvent;
import com.aiexpense.notificationservice.repository.NotificationRecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final NotificationRecipientRepository recipientRepository;

    @KafkaListener(topics = "user-events",
            groupId = "notification-user-group",
            containerFactory = "userEventListenerFactory")
    public void handleUserEvent(UserRegisteredEvent event) {
        if (event == null || !"USER_REGISTERED".equals(event.eventType())) {
            log.warn("[notification-service] Skipping unexpected event on user-events");
            return;
        }
        recipientRepository.save(new NotificationRecipient(event.userId(), event.email()));
        log.info("[notification-service] Stored email for userId: {}", event.userId());
    }
}
