package com.AIExpense.ExpenseTracker.kafka.consumer;


import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;
import com.AIExpense.ExpenseTracker.expense.repository.ExpenseRepository;
import com.AIExpense.ExpenseTracker.kafka.config.KafkaConfig;
import com.AIExpense.ExpenseTracker.kafka.event.BudgetAlertEvent;
import com.AIExpense.ExpenseTracker.kafka.event.ExpenseCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetAlertConsumer {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = KafkaConfig.EXPENSE_CREATED_TOPIC,
            groupId = "budget-alert-group"
    )
    public void handleExpenseCreated(ExpenseCreatedEvent event) {

        log.info("Received ExpenseCreatedEvent for userId: {} category: {}",
                event.userId(), event.category());

        int month = event.expenseDate().getMonthValue();
        int year = event.expenseDate().getYear();


        budgetRepository.findActiveBudget(event.userId(), event.category(), month, year)
                .ifPresent(budget -> {

                    BigDecimal actualSpending = expenseRepository
                            .getTotalAmountByUserIdAndCategory(event.userId(), event.category());

                    if (actualSpending == null) actualSpending = BigDecimal.ZERO;

                    if (actualSpending.compareTo(budget.getMonthlyLimit()) > 0) {
                        BigDecimal exceededBy = actualSpending.subtract(budget.getMonthlyLimit());

                        log.warn("Budget exceeded for userId: {} category: {} " +
                                        "limit: {} actual: {} exceededBy: {}",
                                event.userId(), event.category(),
                                budget.getMonthlyLimit(), actualSpending, exceededBy);


                        kafkaTemplate.send(
                                KafkaConfig.BUDGET_ALERT_TOPIC,
                                String.valueOf(event.userId()),
                                new BudgetAlertEvent(
                                        event.userId(),
                                        event.userEmail(),
                                        event.category(),
                                        budget.getMonthlyLimit(),
                                        actualSpending,
                                        exceededBy,
                                        month,
                                        year,
                                        LocalDateTime.now()
                                )
                        );
                    } else {
                        log.info("Budget check passed for userId: {} category: {} " +
                                        "limit: {} actual: {}",
                                event.userId(), event.category(),
                                budget.getMonthlyLimit(), actualSpending);
                    }
                });
    }
}
