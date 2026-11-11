package com.aiexpense.expenseservice.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventProducer {

    private final KafkaTemplate<String, ExpenseEvent> kafkaTemplate;

    @Value("${kafka.topics.expense-events:expense-events}")
    private String topicName;


    public void publishExpenseEvent(ExpenseEvent event) {
        try {
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(LocalDateTime.now());
            }

            kafkaTemplate.send(
                    topicName,
                    event.getUserId().toString(),
                    event
            );

            log.info("Published event: {} for user: {}", event.getEventType(), event.getUserId());

        } catch (Exception e) {
            log.error("Failed to publish expense event", e);
        }
    }


    public void publishExpenseCreated(ExpenseEvent event) {
        event.setEventType("EXPENSE_CREATED");
        publishExpenseEvent(event);
    }

    public void publishExpenseUpdated(ExpenseEvent event) {
        event.setEventType("EXPENSE_UPDATED");
        publishExpenseEvent(event);
    }

    public void publishExpenseDeleted(ExpenseEvent event) {
        event.setEventType("EXPENSE_DELETED");
        publishExpenseEvent(event);
    }
}
