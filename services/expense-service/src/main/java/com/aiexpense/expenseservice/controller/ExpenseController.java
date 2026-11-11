package com.aiexpense.expenseservice.controller;


import com.aiexpense.expenseservice.util.AuthenticationUtils;
import com.aiexpense.expenseservice.dto.*;
import com.aiexpense.expenseservice.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expenses", description = "Expense management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ExpenseController {


    private final ExpenseService expenseService;
    private final AuthenticationUtils authUtils;


    @PostMapping
    @Operation(summary = "Create expense")
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        Long userId = authUtils.getCurrentUserId();
        ExpenseResponse response = expenseService.createExpense(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{expenseId}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable Long expenseId
    ) {
        Long userId = authUtils.getCurrentUserId();
        ExpenseResponse response = expenseService.getExpenseById(userId, expenseId);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation(summary = "Get all expenses (paginated)")
    public ResponseEntity<PagedResult<ExpenseResponse>> getAllExpenses(
            @PageableDefault(size = 20, sort = "expenseDate",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = authUtils.getCurrentUserId();
        return ResponseEntity.ok(expenseService.getAllExpenses(userId, pageable));
    }


    @GetMapping("/filter")
    @Operation(summary = "Filter expenses by criteria")
    public ResponseEntity<Page<ExpenseResponse>> filterExpenses(

            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @PageableDefault(size = 20, page = 0) Pageable pageable
    ) {
        Long userId = authUtils.getCurrentUserId();
        Page<ExpenseResponse> response = expenseService.filterExpenses(
                userId, category, startDate, endDate, pageable
        );
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{expenseId}")
    @Operation(summary = "Update expense")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request
    ) {
        Long userId = authUtils.getCurrentUserId();
        ExpenseResponse response = expenseService.updateExpense(userId, expenseId, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{expenseId}")
    @Operation(summary = "Delete expense")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long expenseId
    ) {
        Long userId = authUtils.getCurrentUserId();
        expenseService.deleteExpense(userId, expenseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary/{month}/{year}")
    @Operation(summary = "Get spending summary for month")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @PathVariable Integer month,
            @PathVariable Integer year
    ) {
        Long userId = authUtils.getCurrentUserId();
        MonthlySummaryResponse response = expenseService.getMonthlySummary(userId, month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/breakdown/{month}/{year}")
    @Operation(summary = "Get spending breakdown by category")
    public ResponseEntity<List<CategorySpendingResponse>> getCategoryBreakdown(
            @PathVariable Integer month,
            @PathVariable Integer year
    ) {
        Long userId = authUtils.getCurrentUserId();
        List<CategorySpendingResponse> response = expenseService.getCategoryBreakdown(userId, month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-category/{category}/{month}/{year}")
    @Operation(summary = "Get expenses for specific category and month")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByMonthAndCategory(
            @PathVariable String category,
            @PathVariable Integer month,
            @PathVariable Integer year
    ) {
        Long userId = authUtils.getCurrentUserId();
        List<ExpenseResponse> response = expenseService
                .getExpensesByMonthAndCategory(userId, category, month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trend")
    @Operation(summary = "Get spending trend (last 12 months)")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend(
            @RequestParam(required = false) String startDate
    ) {
        Long userId = authUtils.getCurrentUserId();

        LocalDate start = startDate != null
                ? LocalDate.parse(startDate)
                : LocalDate.now().minusMonths(12);

        List<MonthlyTrendResponse> response = expenseService.getMonthlyTrend(userId, start);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category-total/{category}")
    @Operation(summary = "Get total spending for category")
    public ResponseEntity<CategoryTotalResponse> getCategoryTotal(
            @PathVariable String category
    ) {
        Long userId = authUtils.getCurrentUserId();
        BigDecimal total = expenseService.getTotalSpendingByCategory(userId, category);

        CategoryTotalResponse response = CategoryTotalResponse.builder()
                .category(category)
                .totalAmount(total)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "UP");
        map.put("service", "expense-service");
        map.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(map);
    }


}
