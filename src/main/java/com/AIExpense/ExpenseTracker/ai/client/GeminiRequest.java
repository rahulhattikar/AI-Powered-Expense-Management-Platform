package com.AIExpense.ExpenseTracker.ai.client;

import java.util.List;

public record GeminiRequest(
        List<Content> contents,
        GenerationConfig generationConfig
) {
}






