package com.AIExpense.ExpenseTracker.common.config;


import com.AIExpense.ExpenseTracker.report.dto.BudgetRemainingResponse;
import com.AIExpense.ExpenseTracker.report.dto.TopSpendingCategoryResponse;
import com.AIExpense.ExpenseTracker.util.CacheNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        ObjectMapper topSpendingMapper = new ObjectMapper();
        CollectionType topSpendingListType = topSpendingMapper.getTypeFactory()
                .constructCollectionType(List.class, TopSpendingCategoryResponse.class);
        Jackson2JsonRedisSerializer<List<TopSpendingCategoryResponse>> topSpendingSerializer =
                new Jackson2JsonRedisSerializer<>(topSpendingMapper, topSpendingListType);

        RedisCacheConfiguration topSpendingConfig = defaultConfig
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(topSpendingSerializer)
                );

        ObjectMapper budgetRemainingMapper = new ObjectMapper();
        CollectionType budgetRemainingListType = budgetRemainingMapper.getTypeFactory()
                .constructCollectionType(List.class, BudgetRemainingResponse.class);
        Jackson2JsonRedisSerializer<List<BudgetRemainingResponse>> budgetRemainingSerializer =
                new Jackson2JsonRedisSerializer<>(budgetRemainingMapper, budgetRemainingListType);

        RedisCacheConfiguration budgetRemainingConfig = defaultConfig
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(budgetRemainingSerializer)
                );


        // Map each cache name to its specific config
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CacheNames.TOP_SPENDING_CATEGORIES, topSpendingConfig);
        cacheConfigs.put(CacheNames.BUDGET_REMAINING, budgetRemainingConfig);


        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
