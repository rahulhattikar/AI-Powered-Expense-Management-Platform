package com.AIExpense.ExpenseTracker.expense.controller;


import com.AIExpense.ExpenseTracker.expense.dto.ExpenseRequest;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    // POST /api/v1/expenses
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest expenseRequest) {
        log.info("REST request to create expense");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseService.createExpense(expenseRequest));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        log.info("REST request to get expense by id: {}", id);
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest expenseRequest) {
        log.info("REST request to update expense with id: {}", id);
        return ResponseEntity.ok(expenseService.updateExpense(id, expenseRequest));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        log.info("REST request to delete expense with id: {}", id);
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/expenses
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        log.info("REST request to get all expenses");
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }


    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByCategory(
            @PathVariable ExpenseCategory category) {
        log.info("REST request to get expenses by category: {}", category);
        return ResponseEntity.ok(expenseService.getExpensesByCategory(category));
    }

    // /filter?month=6&year=2026
    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get expenses for month: {} year: {}", month, year);
        return ResponseEntity.ok(expenseService.getExpensesByMonthAndYear(month, year));

    }


    @GetMapping("/total/category/{category}")
    public ResponseEntity<BigDecimal> getTotalExpenseByCategory(
            @PathVariable ExpenseCategory category) {
        log.info("REST request to get total spending by category: {}", category);
        return ResponseEntity.ok(expenseService.getTotalSpendingByCategory(category));
    }

    // GET /api/v1/expenses/total
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalSpending() {
        log.info("REST request to get total spending");
        return ResponseEntity.ok(expenseService.getTotalSpending());
    }

}
