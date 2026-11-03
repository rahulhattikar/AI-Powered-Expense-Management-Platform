package com.aiexpense.notificationservice.event;

public record UserRegisteredEvent(
        String eventType,
        Long userId,
        String email,
        String name
) {}
