package com.aiexpense.expenseservice.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventConsumer {


    @KafkaListener(
            topics = "expense-events",
            groupId = "expense-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeExpenseEvent(ExpenseEvent event) {
        log.info("Received event: {} for expense: {}", event.getEventType(), event.getExpenseId());

        switch (event.getEventType()) {
            case "EXPENSE_CREATED":
                handleExpenseCreated(event);
                break;
            case "EXPENSE_UPDATED":
                handleExpenseUpdated(event);
                break;
            case "EXPENSE_DELETED":
                handleExpenseDeleted(event);
                break;
        }
    }

    private void handleExpenseCreated(ExpenseEvent event) {
        log.info("Processing created expense: {} amount: {}", event.getExpenseId(), event.getAmount());

    }

    private void handleExpenseUpdated(ExpenseEvent event) {
        log.info("Processing updated expense: {} amount changed from {} to {}",
                event.getExpenseId(),
                event.getPreviousAmount(),
                event.getAmount()
        );
    }

    private void handleExpenseDeleted(ExpenseEvent event) {
        log.info("Processing deleted expense: {}", event.getExpenseId());
    }
}
