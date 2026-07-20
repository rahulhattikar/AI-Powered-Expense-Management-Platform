package com.aiexpense.aiservice.service;

import com.aiexpense.aiservice.dto.ExpenseAnalysisResponse;

public interface AiAnalysisService {
    ExpenseAnalysisResponse analyzeExpenses(int month, int year, String authorizationHeader);
}