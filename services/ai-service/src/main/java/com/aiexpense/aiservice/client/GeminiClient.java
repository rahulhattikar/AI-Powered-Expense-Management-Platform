package com.AIExpense.ExpenseTracker.ai.client;


import com.AIExpense.ExpenseTracker.common.exception.AiServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiClient {

    private final RestClient geminiRestClient;


    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String modelName;

    public String generateContent(String prompt) {

        GeminiResponse response;

        GeminiRequest request = new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt)))),
                new GenerationConfig("application/json", 0.2));

        try {
            response = geminiRestClient.post()
                    .uri("/v1beta/models/{model}:generateContent", modelName)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

        } catch (RestClientException e) {
            log.error("Error calling Gemini API for model: {} - {}",
                    modelName, e.getMessage());
            throw new AiServiceUnavailableException("Gemini service is unavailable", e);
        }

        if (response == null || response.candidates() == null || response.candidates()
                .isEmpty()) {
            log.error("Gemini API returned no candidates for model: {}", modelName);
            throw new AiServiceUnavailableException(
                    "Gemini API returned no response candidates - possibly " +
                            "blocked by safety filters");
        }

        Candidate candidate = response.candidates().get(0);
        if (candidate.content() == null
                || candidate.content().parts() == null
                || candidate.content().parts().isEmpty()) {
            log.error("Gemini API returned empty content for model: {}", modelName);
            throw new AiServiceUnavailableException("Gemini API returned empty content");
        }

        String text = candidate.content().parts().get(0).text();
        log.info("Gemini API call successful for model: {}", modelName);
        return text;
    }
}
