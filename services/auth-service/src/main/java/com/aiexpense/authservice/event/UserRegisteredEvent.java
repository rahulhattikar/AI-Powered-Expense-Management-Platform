package com.aiexpense.authservice.event;

public record UserRegisteredEvent(
        String eventType,
        Long userId,
        String email,
        String name
) {}
