package com.AIExpense.ExpenseTracker.kafka.consumer;


import com.AIExpense.ExpenseTracker.common.exception.UserNotFoundException;
import com.AIExpense.ExpenseTracker.kafka.config.KafkaConfig;
import com.AIExpense.ExpenseTracker.kafka.event.BudgetAlertEvent;
import com.AIExpense.ExpenseTracker.user.entity.User;
import com.AIExpense.ExpenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final JavaMailSender mailSender;

    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @KafkaListener(
            topics = KafkaConfig.BUDGET_ALERT_TOPIC,
            groupId = "email-notification-group"
    )
    public void handleBudgetAlert(BudgetAlertEvent event) {
        log.info("Received BudgetAlertEvent for userId: {} category: {}",
                event.userId(), event.category());

        Optional<User> userOptional = userRepository.findById(event.userId());

        if (userOptional.isEmpty()) {
            log.warn("Cannot send budget alert email - user not found for userId: {}",
                    event.userId());
            return;
        }
        User recipient = userOptional.get();

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
        message.setTo(recipient.getEmail());
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Budget alert email sent successfully to {} for category {}",
                    recipient.getEmail(), event.category());
        } catch (Exception e) {
            log.error("Failed to send budget alert email for userId: {} - {}",
                    event.userId(), e.getMessage());
        }
    }
}

