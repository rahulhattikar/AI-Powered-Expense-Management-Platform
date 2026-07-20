package com.AIExpense.ExpenseTracker.ai.client;

import java.util.List;

public record GeminiResponse(
        List<Candidate> candidates
) {
}


