package com.AIExpense.ExpenseTracker.kafka.consumer;


import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;

import com.AIExpense.ExpenseTracker.client.ExpenseServiceClient;
import com.AIExpense.ExpenseTracker.dto.CategoryTotalResponse;
import com.AIExpense.ExpenseTracker.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.kafka.config.KafkaConfig;
import com.AIExpense.ExpenseTracker.kafka.event.BudgetAlertEvent;
import com.AIExpense.ExpenseTracker.kafka.event.ExpenseEvent;
import com.AIExpense.ExpenseTracker.util.AuthenticationUtils;
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

    private final ExpenseServiceClient expenseServiceClient;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final AuthenticationUtils authenticationUtils;

    @KafkaListener(
            topics = KafkaConfig.EXPENSE_CREATED_TOPIC,
            groupId = "budget-alert-group"
    )
    public void handleExpenseCreated(ExpenseEvent event) {


        log.info("Received expense event: type={} userId={} category={}",
                event.eventType(), event.userId(), event.category());

        if (!"EXPENSE_CREATED".equals(event.eventType())
                && !"EXPENSE_UPDATED".equals(event.eventType())) {
            return;
        }

        if (event.expenseDate() == null || event.monthlyCategoryTotal() == null) {
            log.warn("Skipping budget check, missing data in event for expenseId: {}",
                    event.expenseId());
            return;
        }

        ExpenseCategory category = ExpenseCategory.valueOf(event.category());
        int month = event.expenseDate().getMonthValue();
        int year = event.expenseDate().getYear();

        budgetRepository.findActiveBudget(event.userId(), category, month, year)
                .ifPresent(budget -> {

                    BigDecimal actualSpending = event.monthlyCategoryTotal();
                    BigDecimal limit = budget.getMonthlyLimit();

                    if (actualSpending.compareTo(limit) > 0) {
                        BigDecimal exceededBy = actualSpending.subtract(limit);

                        log.warn("Budget exceeded userId={} category={} limit={} actual={} exceededBy={}",
                                event.userId(), category, limit, actualSpending, exceededBy);

                        kafkaTemplate.send(
                                KafkaConfig.BUDGET_ALERT_TOPIC,
                                String.valueOf(event.userId()),
                                new BudgetAlertEvent(
                                        event.userId(),
                                        event.userEmail(),
                                        category.name(),
                                        limit,
                                        actualSpending,
                                        exceededBy,
                                        month,
                                        year,
                                        LocalDateTime.now()
                                ));
                    } else {
                        log.info("Budget check passed userId={} category={} limit={} actual={}",
                                event.userId(), category, limit, actualSpending);
                    }
                });
    }
}
