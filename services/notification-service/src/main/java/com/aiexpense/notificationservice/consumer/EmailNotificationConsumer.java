package com.aiexpense.notificationservice.consumer;

import com.aiexpense.notificationservice.event.BudgetAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @KafkaListener(
            topics = "budget-alert",
            groupId = "email-notification-group"
    )
    public void handleBudgetAlert(BudgetAlertEvent event) {
        log.info("[notification-service] Received BudgetAlertEvent for userId: {} category: {}",
                event.userId(), event.category());

        String subject = String.format("Budget Alert: %s exceeded by %s",
                event.category(), event.exceededBy());

        String body = String.format(
                "Your %s budget of %s has been exceeded.%n" +
                        "Actual spending: %s%n" +
                        "Exceeded by: %s%n" +
                        "Period: %d/%d",
                event.category(), event.budgetLimit(),
                event.actualSpending(), event.exceededBy(),
                event.month(), event.year()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(event.userEmail());
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("[notification-service] Budget alert email sent successfully to {} for category {}",
                    event.userEmail(), event.category());
        } catch (Exception e) {
            log.error("[notification-service] Failed to send budget alert email for userId: {} - {}",
                    event.userId(), e.getMessage());
        }
    }
}