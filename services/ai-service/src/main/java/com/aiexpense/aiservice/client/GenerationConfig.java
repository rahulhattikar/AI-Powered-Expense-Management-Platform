package com.AIExpense.ExpenseTracker.ai.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationConfig(
        @JsonProperty("response_mime_type") String responseMimeType,
        Double temperature
) {
}
