package com.aiexpense.aiservice.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationConfig(
        @JsonProperty("response_mime_type") String responseMimeType,
        Double temperature
) {
}
