package com.aiexpense.aiservice.service;

import com.aiexpense.aiservice.client.GeminiClient;
import com.aiexpense.aiservice.client.ReportServiceClient;
import com.aiexpense.aiservice.dto.ExpenseAnalysisResponse;
import com.aiexpense.aiservice.dto.MonthlySummaryResponse;
import com.aiexpense.aiservice.exception.AiServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final ReportServiceClient reportServiceClient;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
             You are Monthly Summary Reporter, an AI that summarizes Monthly Response Java object.\s
             You will be given a monthly summary response object, including total spent,\s
             number of transactions, average per day, highest expense, highest spending\s
             category, and a breakdown of spending by category. Your task is to generate\s
             a concise summary of the user's spending habits for that month.
            \s
             Here are some examples:
             MonthlySummaryResponse object:
             MonthlySummaryResponse(totalSpent=1000.00, numberOfTransactions=50, averagePerDay=33.33,\s
             highestExpense=200.00, highestSpendingCategory=FOOD,\s
             categoryBreakdown=[CategoryBreakdown(category=FOOD, totalSpent=400.00,\s
             percentageOfTotal=40.0), CategoryBreakdown(category=TRANSPORTATION, totalSpent=300.00, percentageOfTotal=30.0),\s
             CategoryBreakdown(category=ENTERTAINMENT, totalSpent=200.00, percentageOfTotal=20.0),\s
             CategoryBreakdown(category=OTHER, totalSpent=100.00, percentageOfTotal=10.0)])
            \s
             Your Response:               \s
             {"summary": "In this month, you spent a total of $1000.00 across 50 transactions, averaging $33.33 per day.
               Your highest expense was $200.00, and you spent the most on FOOD. Here's the breakdown: FOOD: $400.00 (40%),\s
               TRANSPORTATION: $300.00 (30%), ENTERTAINMENT: $200.00 (20%), OTHER: $100.00 (10%)."}
              \s
               instruction for you:
               use ONLY the numbers provided below, do not calculate or estimate any additional figures
            """;

    private record GeminiSummaryPayload(String summary) {}

    @Override
    public ExpenseAnalysisResponse analyzeExpenses(int month, int year, String authorizationHeader) {
        log.info("Generating AI expense analysis for month: {} year: {}", month, year);

        MonthlySummaryResponse monthlySummaryResponse =
                reportServiceClient.getMonthlySummary(month, year, authorizationHeader);

        String fullPrompt = SYSTEM_PROMPT + "\n\nNow summarize this MonthlySummaryResponse object:\n"
                + monthlySummaryResponse.toString();

        String rawJson = geminiClient.generateContent(fullPrompt);

        GeminiSummaryPayload payload;
        try {
            payload = objectMapper.readValue(rawJson, GeminiSummaryPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response for month: {} year: {} - {}", month, year, e.getMessage());
            throw new AiServiceUnavailableException("Gemini returned an unparseable response", e);
        }

        log.info("AI expense analysis generated successfully for month: {} year: {}", month, year);
        return new ExpenseAnalysisResponse(payload.summary(), monthlySummaryResponse, LocalDateTime.now());
    }
}