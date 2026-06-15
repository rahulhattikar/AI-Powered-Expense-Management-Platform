package com.AIExpense.ExpenseTracker.report.controller;


import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.report.dto.*;
import com.AIExpense.ExpenseTracker.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    // GET /api/v1/reports/monthly-summary?month=6&year=2026
    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get monthly summary for month: {} year: {}",
                month, year);
        return ResponseEntity.ok(reportService.getMonthlySummary(month, year));
    }

    // GET /api/v1/reports/category-summary?category=FOOD&month=6&year=2026
    @GetMapping("/category-summary")
    public ResponseEntity<CategorySummaryResponse> getCategorySummary(
            @RequestParam ExpenseCategory category,
            @RequestParam int month,
            @RequestParam int year
    ) {
        log.info("REST request to get category summary for category: {} month: {} year: {}",
                category, month, year);
        return ResponseEntity.ok(reportService.getCategorySummary(category, month, year));
    }

    // GET /api/v1/reports/top-spending-categories?month=6&year=2026&limit=5
    @GetMapping("/top-spending-categories")
    public ResponseEntity<List<TopSpendingCategoryResponse>> getTopSpendingCategories(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("REST request to get top {} spending categories for month: {} year: {}",
                limit, month, year);
        return ResponseEntity.ok(reportService.getTopSpendingCategories(month, year, limit));
    }

    // GET /api/v1/reports/budget-remaining?month=6&year=2026
    @GetMapping("/budget-remaining")
    public ResponseEntity<List<BudgetRemainingResponse>> getBudgetRemaining(
            @RequestParam int month,
            @RequestParam int year
    ) {
        log.info("REST request to get Budget remaining for month: {} year: {}",
                month, year);
        return ResponseEntity.ok(reportService.getBudgetRemaining(month, year));
    }

    // GET /api/v1/reports/monthly-trend?months=6
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        log.info("REST request to get monthly trend for last {} months", months);
        return ResponseEntity.ok(reportService.getMonthlyTrend(months));
    }


}
