package com.AIExpense.ExpenseTracker.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaConfig {

    public static final String EXPENSE_CREATED_TOPIC = "expense-created";
    public static final String BUDGET_ALERT_TOPIC = "budget-alert";

    @Bean
    public NewTopic expenseCreatedTopic() {
        return TopicBuilder.name(EXPENSE_CREATED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic budgetAlertTopic() {
        return TopicBuilder.name(BUDGET_ALERT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
