package com.AIExpense.ExpenseTracker.ai.service;

import com.AIExpense.ExpenseTracker.ai.dto.ExpenseAnalysisResponse;

public interface AiAnalysisService {

    ExpenseAnalysisResponse analyzeExpenses(int month, int year);

}
