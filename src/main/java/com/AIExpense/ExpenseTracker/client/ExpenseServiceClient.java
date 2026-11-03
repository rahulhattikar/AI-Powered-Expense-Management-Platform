package com.AIExpense.ExpenseTracker.client;


import com.AIExpense.ExpenseTracker.dto.CategoryTotalResponse;
import com.AIExpense.ExpenseTracker.report.dto.CategorySpendingResponse;
import com.AIExpense.ExpenseTracker.report.dto.MonthlyTrendResponse;
import com.AIExpense.ExpenseTracker.report.dto.MonthlySummaryResponse;
import com.AIExpense.ExpenseTracker.dto.ExpenseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "expense-service",
        url = "${expense-service.url:http://localhost:8083}",
        fallback = ExpenseServiceClientFallback.class
)
public interface ExpenseServiceClient {


    @GetMapping("/api/v1/expenses/{expenseId}")
    ExpenseResponse getExpenseById(@PathVariable("expenseId") Long expenseId);


    @GetMapping("/api/v1/expenses/category-total/{category}")
    CategoryTotalResponse getCategoryTotal(
            @PathVariable("category") String category,
            @RequestHeader("Authorization") String token
    );


    @GetMapping("/api/v1/expenses/summary/{month}/{year}")
    MonthlySummaryResponse getMonthlySummary(
            @PathVariable Integer month,
            @PathVariable Integer year,
            @RequestHeader("Authorization") String token
    );


    @GetMapping("/api/v1/expenses/breakdown/{month}/{year}")
    List<CategorySpendingResponse> getCategoryBreakdown(
            @PathVariable Integer month,
            @PathVariable Integer year,
            @RequestHeader("Authorization") String token
    );


    @GetMapping("/api/v1/expenses/by-category/{category}/{month}/{year}")
    List<ExpenseResponse> getExpensesByMonthAndCategory(
            @PathVariable String category,
            @PathVariable Integer month,
            @PathVariable Integer year,
            @RequestHeader("Authorization") String token
    );


    @GetMapping("/api/v1/expenses/trend")
    List<MonthlyTrendResponse> getMonthlyTrend(
            @RequestParam(required = false) String startDate,
            @RequestHeader("Authorization") String token
    );

}
