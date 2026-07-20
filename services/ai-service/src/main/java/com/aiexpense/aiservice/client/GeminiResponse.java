package com.aiexpense.aiservice.client;

import java.util.List;

public record GeminiResponse(
        List<Candidate> candidates
) {
}


