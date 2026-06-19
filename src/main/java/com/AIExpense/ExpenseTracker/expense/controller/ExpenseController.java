package com.AIExpense.ExpenseTracker.expense.controller;


import com.AIExpense.ExpenseTracker.expense.dto.ExpenseRequest;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.expense.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.service.ExpenseService;
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

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Expenses", description = "Endpoints for creating, updating, " +
        "deleting and retrieving expenses")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;


    @Operation(
            summary = "Create a new expense",
            description = "Creates a new expense entry for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    // POST /api/v1/expenses
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest expenseRequest) {
        log.info("REST request to create expense");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseService.createExpense(expenseRequest));
    }


    @Operation(
            summary = "Get an expense by id",
            description = "Get an Expense by id for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense fetched"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        log.info("REST request to get expense by id: {}", id);
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }


    @Operation(
            summary = "Update a Expense",
            description = "Update a expense for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update Expense successfully"),
            @ApiResponse(responseCode = "404", description = "Invalid id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest expenseRequest) {
        log.info("REST request to update expense with id: {}", id);
        return ResponseEntity.ok(expenseService.updateExpense(id, expenseRequest));
    }

    @Operation(
            summary = "Delete a Expense",
            description = "Delete a Expense for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete a Expense"),
            @ApiResponse(responseCode = "404", description = "Invalid id"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        log.info("REST request to delete expense with id: {}", id);
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get all expenses (paginated)",
            description = "Fetches a paginated list of expenses for the authenticated user, sortable by field and direction"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ExpenseResponse>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("REST request to get expenses page: {} size: {}", page, size);
        return ResponseEntity.ok(
                expenseService.getAllExpensesPaged(page, size, sortBy, sortDirection));
    }


    @Operation(
            summary = "Get expenses by category",
            description = "Fetches all expenses for a given category for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByCategory(
            @PathVariable ExpenseCategory category) {
        log.info("REST request to get expenses by category: {}", category);
        return ResponseEntity.ok(expenseService.getExpensesByCategory(category));
    }

    @Operation(
            summary = "Get expenses by month and year",
            description = "Fetches all expenses filtered by month and year for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    // /filter?month=6&year=2026
    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        log.info("REST request to get expenses for month: {} year: {}", month, year);
        return ResponseEntity.ok(expenseService.getExpensesByMonthAndYear(month, year));

    }


    @Operation(
            summary = "Get total expense by category",
            description = "Fetches the total amount spent in a given category for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/total/category/{category}")
    public ResponseEntity<BigDecimal> getTotalExpenseByCategory(
            @PathVariable ExpenseCategory category) {
        log.info("REST request to get total spending by category: {}", category);
        return ResponseEntity.ok(expenseService.getTotalSpendingByCategory(category));
    }

    @Operation(
            summary = "Get total spending",
            description = "Fetches the total amount spent across all categories for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    // GET /api/v1/expenses/total
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalSpending() {
        log.info("REST request to get total spending");
        return ResponseEntity.ok(expenseService.getTotalSpending());
    }

}
