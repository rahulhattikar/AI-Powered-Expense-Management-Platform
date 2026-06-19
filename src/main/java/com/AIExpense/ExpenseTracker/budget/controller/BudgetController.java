package com.AIExpense.ExpenseTracker.budget.controller;


import com.AIExpense.ExpenseTracker.budget.dto.BudgetRequest;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetResponse;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetStatusResponse;
import com.AIExpense.ExpenseTracker.budget.service.BudgetService;
import com.AIExpense.ExpenseTracker.common.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Budgets",description = "Endpoints for creating,updating,retrieving and deleting budget")
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;


    @Operation(
            summary = "Create a new budget",
            description = "Creates a new budget entry for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Budget created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest budgetRequest) {
        log.info("REST request to create budget for category: {}", budgetRequest.category());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(budgetService.createBudget(budgetRequest));
    }

    @Operation(
            summary = "Get a Budget",
            description = "Fetches a budget for given id for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Budget fetched"),
            @ApiResponse(responseCode = "404",description = "Not found"),
            @ApiResponse(responseCode = "401",description ="Unauthorized - missing or invalid token" )
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @PathVariable Long id) {
        log.info("REST request to get budget by id: {}", id);
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @Operation(
            summary = "Get all budgets (paginated)",
            description = "Fetches a paginated list of budgets for the authenticated user, sortable by field and direction"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budgets fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<BudgetResponse>> getAllBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "year") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("REST request to get budgets page: {} size: {}", page, size);
        return ResponseEntity.ok(
                budgetService.getAllBudgetsPaged(page, size, sortBy, sortDirection));
    }


    @Operation(
            summary = "Update a budget",
            description = "Updates a budget entry for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget update successfully"),
            @ApiResponse(responseCode = "404", description = "Invalid id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest budgetRequest) {
        log.info("REST request to update budget with id: {}", id);
        return ResponseEntity.ok(budgetService.updateBudget(id, budgetRequest));
    }


    @Operation(
            summary = "Delete a budget",
            description = "Delete a budget entry for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Invalid id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        log.info("REST request to delete budget with id: {}", id);
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get list of budget category",
            description = "Fetches a list budget for given category for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget list fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByCategory(
            @PathVariable ExpenseCategory category) {
        log.info("REST request to get budgets by category: {}", category);
        return ResponseEntity.ok(budgetService.getBudgetsByCategory(category));
    }


    @Operation(
            summary = "Get a list of budget",
            description = "Fetches a list of budget for given month and year for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget list fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get budgets for month: {} year: {}", month, year);
        return ResponseEntity.ok(budgetService.getBudgetsByMonthAndYear(month, year));
    }


    @Operation(
            summary = "Get a list of budget",
            description = "Fetches a list of budget for given category,month and year " +
                    "for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget list fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
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

    @Operation(
            summary = "check budget existence",
            description = "check for budget existence for given category, month and year"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget Exists"),
            @ApiResponse(responseCode = "404", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/exists")
    public ResponseEntity<Boolean> isBudgetExists(
            @RequestParam ExpenseCategory category,
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to check budget existence for category: {}", category);
        return ResponseEntity.ok(budgetService.isBudgetExists(category, month, year));
    }


    @Operation(
            summary = "Get budget status",
            description = "Compares actual spending against budget limit and returns WITHIN_BUDGET, EXCEEDED or NO_BUDGET status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget status fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Budget not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/status")
    public ResponseEntity<BudgetStatusResponse> getBudgetStatus(
            @RequestParam ExpenseCategory category,
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get budget status for category: {}", category);
        return ResponseEntity.ok(budgetService.getBudgetStatus(category, month, year));
    }
}
