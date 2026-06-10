package com.AIExpense.ExpenseTracker.budget.controller;


import com.AIExpense.ExpenseTracker.budget.dto.BudgetRequest;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetResponse;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetStatusResponse;
import com.AIExpense.ExpenseTracker.budget.service.BudgetService;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest budgetRequest) {
        log.info("REST request to create budget for category: {}", budgetRequest.category());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(budgetService.createBudget(budgetRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @PathVariable Long id) {
        log.info("REST request to get budget by id: {}", id);
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }


    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {
        log.info("REST request to get all budgets");
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }


    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest budgetRequest) {
        log.info("REST request to update budget with id: {}", id);
        return ResponseEntity.ok(budgetService.updateBudget(id, budgetRequest));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        log.info("REST request to delete budget with id: {}", id);
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/category/{category}")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByCategory(
            @PathVariable ExpenseCategory category) {
        log.info("REST request to get budgets by category: {}", category);
        return ResponseEntity.ok(budgetService.getBudgetsByCategory(category));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get budgets for month: {} year: {}", month, year);
        return ResponseEntity.ok(budgetService.getBudgetsByMonthAndYear(month, year));
    }

    @GetMapping("/filter/category")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByCategoryAndMonthAndYear(
            @RequestParam ExpenseCategory category,
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get budgets for category: {} month: {} year: {}",
                category, month, year);
        return ResponseEntity.ok(
                budgetService.getBudgetsByCategoryAndMonthAndYear(category, month, year));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> isBudgetExists(
            @RequestParam ExpenseCategory category,
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to check budget existence for category: {}", category);
        return ResponseEntity.ok(budgetService.isBudgetExists(category, month, year));
    }

    @GetMapping("/status")
    public ResponseEntity<BudgetStatusResponse> getBudgetStatus(
            @RequestParam ExpenseCategory category,
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get budget status for category: {}", category);
        return ResponseEntity.ok(budgetService.getBudgetStatus(category, month, year));
    }
}
