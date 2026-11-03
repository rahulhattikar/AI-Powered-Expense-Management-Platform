package com.aiexpense.notificationservice.config;


import com.aiexpense.notificationservice.event.BudgetAlertEvent;
import com.aiexpense.notificationservice.event.UserRegisteredEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseProps(String groupId, String defaultType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, defaultType);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.aiexpense.notificationservice.event");
        return props;
    }

    @Bean
    public ConsumerFactory<String, BudgetAlertEvent> budgetAlertConsumerFactory() {
        Map<String, Object> props = baseProps(
                "email-notification-group",
                BudgetAlertEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("budgetAlertListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, BudgetAlertEvent> budgetAlertListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BudgetAlertEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(budgetAlertConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> userEventConsumerFactory() {
        Map<String, Object> props = baseProps(
                "notification-user-group",
                UserRegisteredEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("userEventListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> userEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userEventConsumerFactory());
        return factory;
    }

}
