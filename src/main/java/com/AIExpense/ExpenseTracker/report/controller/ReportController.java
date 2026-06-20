package com.AIExpense.ExpenseTracker.report.controller;


import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.report.dto.*;
import com.AIExpense.ExpenseTracker.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Reports", description = "Endpoints for retrieving monthly-summary report," +
        " category-summary report,top-spending-categories report," +
        " budget-remaining, monthly-trend reports")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "Get a monthly-summary",
            description = "Fetches a monthly-summary report for given month and year"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly-summary report fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    // GET /api/v1/reports/monthly-summary?month=6&year=2026
    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get monthly summary for month: {} year: {}",
                month, year);
        return ResponseEntity.ok(reportService.getMonthlySummary(month, year));
    }


    @Operation(
            summary = "Get a category-summary",
            description = "Fetches a category-summary report for a given category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category-summary report fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
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


    @Operation(
            summary = "Get a top-spending-category",
            description = "Fetches a given number of top-spending-category report " +
                    "for given month and year"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top-spending-category report fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
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


    @Operation(
            summary = "Get a budget-remaining",
            description = "Fetches a budget-remaining for various categories "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget-remaining fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
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


    @Operation(
            summary = "Get monthly spending trend",
            description = "Fetches spending trend with month-over-month percentage change for the last N months"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly trend fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    // GET /api/v1/reports/monthly-trend?months=6
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        log.info("REST request to get monthly trend for last {} months", months);
        return ResponseEntity.ok(reportService.getMonthlyTrend(months));
    }


}
