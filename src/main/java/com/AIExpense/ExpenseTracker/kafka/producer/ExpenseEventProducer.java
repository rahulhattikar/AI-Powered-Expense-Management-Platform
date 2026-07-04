package com.AIExpense.ExpenseTracker.kafka.producer;

import com.AIExpense.ExpenseTracker.kafka.config.KafkaConfig;
import com.AIExpense.ExpenseTracker.kafka.event.ExpenseCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishExpenseCreated(ExpenseCreatedEvent event) {
        log.info("Publishing ExpenseCreatedEvent for userId: {} category: {}",
                event.userId(), event.category());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaConfig.EXPENSE_CREATED_TOPIC,
                        String.valueOf(event.userId()),  // key = userId (ensures same user's events go to same partition)
                        event
                );

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to publish ExpenseCreatedEvent for userId: {} - {}",
                        event.userId(), throwable.getMessage());
            } else {
                log.info("ExpenseCreatedEvent published successfully - partition: {} offset: {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
