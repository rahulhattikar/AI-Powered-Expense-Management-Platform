package com.aiexpense.aiservice.client;

import java.util.List;

public record GeminiRequest(
        List<Content> contents,
        GenerationConfig generationConfig
) {
}






