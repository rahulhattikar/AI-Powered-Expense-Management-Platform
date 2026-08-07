package com.AIExpense.ExpenseTracker.report.service;

import com.AIExpense.ExpenseTracker.budget.entity.Budget;
import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;
import com.AIExpense.ExpenseTracker.expense.entity.Expense;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.repository.ExpenseRepository;
import com.AIExpense.ExpenseTracker.report.dto.*;
import com.AIExpense.ExpenseTracker.util.AuthenticationUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private AuthenticationUtils authenticationUtils;

    @InjectMocks
    private ReportServiceImpl reportService;


    private Expense testExpense;
    private Budget testBudget;

    @BeforeEach
    void setUp() {


        testExpense = Expense.builder()
                .id(1L)
                .userId(1L)
                .amount(new BigDecimal("500.00"))
                .description("Grocery shopping")
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.of(2026, 6, 1))
                .build();

        testBudget = Budget.builder()
                .id(1L)
                .userId(1L)
                .category(ExpenseCategory.FOOD)
                .monthlyLimit(new BigDecimal("5000.00"))
                .month(6)
                .year(2026)
                .build();


    }

    @Test
    @DisplayName("should return monthly summary with category breakdown when expenses exist")
    void getMonthlySummary_ShouldReturnSummary_WhenExpensesExist() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);


        List<Object[]> categoryData = List.of(
                new Object[]{ExpenseCategory.FOOD, new BigDecimal("3000.00")},
                new Object[]{ExpenseCategory.TRAVEL, new BigDecimal("2000.00")}
        );

        when(expenseRepository.getSpendingByCategoryForMonth(1L, 6, 2026))
                .thenReturn(categoryData);

        when(expenseRepository.getTotalSpentByMonth(1L, 6, 2026))
                .thenReturn(new BigDecimal("5000.00"));

        when(expenseRepository.countTransactionsByMonth(1L, 6, 2026))
                .thenReturn(10);

        when(expenseRepository.getHighestExpenseByMonth(1L, 6, 2026))
                .thenReturn(new BigDecimal("1500.00"));


        MonthlySummaryResponse response = reportService.getMonthlySummary(6, 2026);


        assertThat(response).isNotNull();
        assertThat(response.totalSpent()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(response.totalTransactions()).isEqualTo(10);
        assertThat(response.highestExpense()).isEqualTo(new BigDecimal("1500.00"));
        Assertions.assertThat(response.categoryBreakdown()).hasSize(2);
    }

    @Test
    @DisplayName("should return NO_BUDGET status when budget is not set for category")
    void getCategorySummary_ShouldReturnNoBudgetStatus_WhenBudgetNotSet() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

        List<Expense> expenses = List.of(testExpense);
        when(expenseRepository.findByCategoryAndUserIdAndMonthAndYear(
                ExpenseCategory.FOOD, 1L, 6, 2026))
                .thenReturn(expenses);

        when(budgetRepository.findActiveBudget(1L, ExpenseCategory.FOOD, 6, 2026))
                .thenReturn(Optional.empty());

        CategorySummaryResponse response = reportService
                .getCategorySummary(ExpenseCategory.FOOD, 6, 2026);

        assertThat(response.status()).isEqualTo("NO_BUDGET");
        assertThat(response.budgetLimit()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should return trend with percentage change when data exists for multiple months")
    void getMonthlyTrend_ShouldReturnTrendWithPercentageChange_WhenDataExistsForMultipleMonths() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

        List<Object[]> trendData = List.of(
                new Object[]{1, 2026, new BigDecimal("10000.00"), 15L},
                new Object[]{2, 2026, new BigDecimal("12000.00"), 18L}
        );
        when(expenseRepository.getMonthlyTrend(eq(1L), any(LocalDate.class)))
                .thenReturn(trendData);


        List<MonthlyTrendResponse> response = reportService.getMonthlyTrend(2);


        Assertions.assertThat(response).hasSize(2);
        assertThat(response.get(0).changePercentage()).isEqualTo(0.0); // first month
        assertThat(response.get(1).changeFromLastMonth())
                .isEqualTo(new BigDecimal("2000.00")); // 12000 - 10000
    }


    @Test
    @DisplayName("should return zero values when no expenses exist")
    void getMonthlySummary_ShouldReturnZeroValues_WhenNoExpensesExist() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

        when(expenseRepository.getTotalSpentByMonth(1L,3,2026))
                .thenReturn(BigDecimal.ZERO);

        when(expenseRepository.getSpendingByCategoryForMonth(1L, 3, 2026))
                .thenReturn(List.of());

        when(expenseRepository.countTransactionsByMonth(1L, 3, 2026))
                .thenReturn(0);

        when(expenseRepository.getHighestExpenseByMonth(1L, 3, 2026))
                .thenReturn(BigDecimal.ZERO);


        MonthlySummaryResponse response = reportService.getMonthlySummary(3, 2026);

        Assertions.assertThat(response).isNotNull();

        Assertions.assertThat(response.categoryBreakdown()).isEmpty();

        assertThat(response).isNotNull();
        assertThat(response.totalSpent()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.totalTransactions()).isEqualTo(0);
        assertThat(response.highestExpense()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.averagePerDay()).isEqualTo(BigDecimal.ZERO);
        Assertions.assertThat(response.categoryBreakdown()).isEmpty();

        verify(expenseRepository).getTotalSpentByMonth(1L, 3, 2026);
        verify(expenseRepository).getSpendingByCategoryForMonth(1L, 3, 2026);
        verify(expenseRepository).countTransactionsByMonth(1L, 3, 2026);
        verify(expenseRepository).getHighestExpenseByMonth(1L, 3, 2026);

    }

    @Test
    @DisplayName("should return WITHIN_BUDGET status when spending is below budget limit")
   void  getCategorySummary_ShouldReturnWithinBudgetStatus_WhenSpendingBelowLimit(){

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

        List<Expense> expenses = List.of(testExpense);

        when(expenseRepository.findByCategoryAndUserIdAndMonthAndYear(
                ExpenseCategory.FOOD, 1L, 6, 2026))
                .thenReturn(expenses);

        when(budgetRepository.findActiveBudget(
                1L, ExpenseCategory.FOOD, 6, 2026))
                .thenReturn(Optional.of(testBudget));


        CategorySummaryResponse response = reportService
                .getCategorySummary(ExpenseCategory.FOOD, 6, 2026);

        assertThat(response.status()).isEqualTo("WITHIN_BUDGET");
        assertThat(response.budgetLimit()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(response.category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(response.totalSpent()).isEqualTo(new BigDecimal("500.00"));
        assertThat(response.remainingAmount()).isEqualTo(new BigDecimal("4500.00"));

        verify(expenseRepository).findByCategoryAndUserIdAndMonthAndYear
                (ExpenseCategory.FOOD, 1L,6,2026);

        verify(budgetRepository).findActiveBudget(1L,ExpenseCategory.FOOD,6,2026);


    }

    @Test
    @DisplayName("should return top spending categories when categories exists")
   void getTopSpendingCategories_ShouldReturnRankedList_WhenCategoriesExist(){

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

        List<Object[]> categoryData = List.of(
                new Object[]{ExpenseCategory.FOOD, new BigDecimal("3000.00")},
                new Object[]{ExpenseCategory.TRAVEL, new BigDecimal("2000.00")}
        );

        when(expenseRepository.getSpendingByCategoryForMonth(1L, 6, 2026))
                .thenReturn(categoryData);

        List<TopSpendingCategoryResponse> response = reportService
                .getTopSpendingCategories(6,2026,2);


        Assertions.assertThat(response).hasSize(2);
        assertThat(response.get(0).rank()).isEqualTo(1);
        assertThat(response.get(0).category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(response.get(1).rank()).isEqualTo(2);
        assertThat(response.get(1).category()).isEqualTo(ExpenseCategory.TRAVEL);

        assertThat(response.get(0).percentageOfTotalSpending()).isEqualTo(60.0);
        assertThat(response.get(1).percentageOfTotalSpending()).isEqualTo(40.0);

        verify(expenseRepository).getSpendingByCategoryForMonth(1L, 6, 2026);
   }

   @Test
   @DisplayName("should return empty top spending category list")
  void getTopSpendingCategories_ShouldReturnEmptyList_WhenNoExpensesExist(){

       when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

       when(expenseRepository.getSpendingByCategoryForMonth(1L, 2, 2026))
               .thenReturn(List.of());

       List<TopSpendingCategoryResponse> response = reportService
               .getTopSpendingCategories(2,2026,2);


       Assertions.assertThat(response).hasSize(0);

       verify(expenseRepository).getSpendingByCategoryForMonth(1L, 2, 2026);
  }

  @Test
  @DisplayName("should return budget remaining for all categories when budget exists")
  void getBudgetRemaining_ShouldReturnAllCategories_WhenBudgetAndExpensesExist(){

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

      List<Object[]> categoryData = List.of(
              new Object[]{ExpenseCategory.FOOD, new BigDecimal("3000.00")},
              new Object[]{ExpenseCategory.TRAVEL, new BigDecimal("2000.00")}
      );

      when(expenseRepository.getSpendingByCategoryForMonth(1L,6,2026))
              .thenReturn(categoryData);

      when(budgetRepository.findAllByUserIdAndMonthAndYear(1L,6,2026))
              .thenReturn(List.of(testBudget));


      List<BudgetRemainingResponse> response = reportService
              .getBudgetRemaining(6,2026);

      Assertions.assertThat(response).hasSize(2);

      assertThat(response.get(0).category()).isEqualTo(ExpenseCategory.FOOD);
      assertThat(response.get(0).budgetLimit()).isEqualTo(new BigDecimal("5000.00"));
      assertThat(response.get(0).totalSpent()).isEqualTo(new BigDecimal("3000.00"));
      assertThat(response.get(0).utilizationPercentage()).isEqualTo(60.0);
      assertThat(response.get(0).status()).isEqualTo("WITHIN_BUDGET");

      assertThat(response.get(1).category()).isEqualTo(ExpenseCategory.TRAVEL);
      assertThat(response.get(1).budgetLimit()).isEqualTo(BigDecimal.ZERO);
      assertThat(response.get(1).totalSpent()).isEqualTo(new BigDecimal("2000.00"));
      assertThat(response.get(1).utilizationPercentage()).isEqualTo(0.0);
      assertThat(response.get(1).status()).isEqualTo("NO_BUDGET");

      verify(expenseRepository).getSpendingByCategoryForMonth(1L, 6, 2026);
      verify(budgetRepository).findAllByUserIdAndMonthAndYear(1L, 6, 2026);

  }


}
