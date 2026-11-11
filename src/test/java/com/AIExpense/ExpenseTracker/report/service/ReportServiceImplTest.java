package com.AIExpense.ExpenseTracker.report.service;

import com.AIExpense.ExpenseTracker.budget.entity.Budget;
import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;
import com.AIExpense.ExpenseTracker.client.ExpenseServiceClient;
import com.AIExpense.ExpenseTracker.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.entity.ExpenseCategory;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ExpenseServiceClient expenseServiceClient;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private AuthenticationUtils authenticationUtils;


    @InjectMocks
    private ReportServiceImpl reportService;


    private Budget testBudget;

    @BeforeEach
    void setUp() {

        testBudget = Budget.builder()
                .id(1L)
                .userId(1L)
                .category(ExpenseCategory.FOOD)
                .monthlyLimit(new BigDecimal("5000.00"))
                .month(6)
                .year(2026)
                .build();

        when(authenticationUtils.getTokenFromRequest()).thenReturn("token");
    }

    @Test
    @DisplayName("should return monthly summary with category breakdown when expenses exist")
    void getMonthlySummary_ShouldReturnSummary_WhenExpensesExist() {

        when(expenseServiceClient.getMonthlySummary(6, 2026, "Bearer token"))
                .thenReturn(MonthlySummaryResponse.builder()
                        .totalSpent(new BigDecimal("5000.00"))
                        .totalTransactions(10)
                        .highestExpense(new BigDecimal("1500.00"))
                        .build());


        MonthlySummaryResponse response = reportService.getMonthlySummary(6, 2026);


        assertThat(response).isNotNull();
        assertThat(response.getTotalSpent()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(response.getTotalTransactions()).isEqualTo(10);
        assertThat(response.getHighestExpense()).isEqualTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("should return NO_BUDGET status when budget is not set for category")
    void getCategorySummary_ShouldReturnNoBudgetStatus_WhenBudgetNotSet() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(authenticationUtils.getTokenFromRequest()).thenReturn("token");

        when(expenseServiceClient.getExpensesByMonthAndCategory("FOOD", 6, 2026, "Bearer token"))
                .thenReturn(List.of(
                       ExpenseResponse.builder()
                               .id(1L)
                               .amount(new BigDecimal("3000.00"))
                               .category(ExpenseCategory.FOOD.toString())
                               .description("Groceries")
                               .expenseDate(LocalDate.of(2026, 6, 15))
                               .build()

                ));


        when(budgetRepository.findActiveBudget(1L, ExpenseCategory.FOOD, 6, 2026))
                .thenReturn(Optional.empty());

        CategorySummaryResponse response = reportService
                .getCategorySummary(ExpenseCategory.FOOD, 6, 2026);

        assertThat(response.status()).isEqualTo("NO_BUDGET");
        assertThat(response.budgetLimit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should return trend with percentage change when data exists for multiple months")
    void getMonthlyTrend_ShouldReturnTrendWithPercentageChange_WhenDataExistsForMultipleMonths() {


        when(expenseServiceClient.getMonthlyTrend(anyString(),eq("Bearer token")))
                .thenReturn(List.of(
                        MonthlyTrendResponse.builder()
                                .month(1)
                                .year(2026)
                                .totalSpent(new BigDecimal("10000.00"))
                                .totalTransactions(15)
                                .changePercentage(0.0)
                                .build(),
                        MonthlyTrendResponse.builder()
                                .month(2)
                                .year(2026)
                                .totalSpent(new BigDecimal("12000.00"))
                                .totalTransactions(18)
                                .changeFromLastMonth(new BigDecimal("2000.00"))
                                .build()
                ));

        List<MonthlyTrendResponse> response = reportService.getMonthlyTrend(2);


        Assertions.assertThat(response).hasSize(2);
        assertThat(response.get(0).getChangePercentage()).isEqualTo(0.0); // first month
        assertThat(response.get(1).getChangeFromLastMonth())
                .isEqualByComparingTo(new BigDecimal("2000.00")); // 12000 - 10000
    }


    @Test
    @DisplayName("should return zero values when no expenses exist")
    void getMonthlySummary_ShouldReturnZeroValues_WhenNoExpensesExist() {


        when(expenseServiceClient.getMonthlySummary(3, 2026, "Bearer token"))
                .thenReturn(MonthlySummaryResponse.builder()
                        .totalSpent(BigDecimal.ZERO)
                        .totalTransactions(0)
                        .highestExpense(BigDecimal.ZERO)
                        .build());


        MonthlySummaryResponse response = reportService.getMonthlySummary(3, 2026);

        Assertions.assertThat(response).isNotNull();

        assertThat(response).isNotNull();
        assertThat(response.getTotalSpent()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getTotalTransactions()).isEqualTo(0);
        assertThat(response.getHighestExpense()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getAveragePerDay()).isEqualTo(BigDecimal.ZERO);


        verify(expenseServiceClient).getMonthlySummary(3, 2026,
                "Bearer token");


    }

    @Test
    @DisplayName("should return WITHIN_BUDGET status when spending is below budget limit")
   void  getCategorySummary_ShouldReturnWithinBudgetStatus_WhenSpendingBelowLimit(){

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(authenticationUtils.getTokenFromRequest()).thenReturn("token");

        when(expenseServiceClient.getExpensesByMonthAndCategory("FOOD", 6, 2026, "Bearer token"))
                .thenReturn(List.of(
                       ExpenseResponse.builder()
                                .id(1L)
                               .amount(new BigDecimal("500.00"))
                               .category(ExpenseCategory.FOOD.toString())
                               .description("Groceries")
                               .expenseDate(LocalDate.of(2026, 6, 15))
                               .build()

                ));



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

        verify(expenseServiceClient).getExpensesByMonthAndCategory("FOOD", 6, 2026, "Bearer token");

        verify(budgetRepository).findActiveBudget(1L,ExpenseCategory.FOOD,6,2026);


    }

    @Test
    @DisplayName("should return top spending categories when categories exists")
   void getTopSpendingCategories_ShouldReturnRankedList_WhenCategoriesExist(){

        when(expenseServiceClient.getCategoryBreakdown(6, 2026, "Bearer token"))
                .thenReturn(List.of(
                        CategorySpendingResponse.builder()
                                .category("FOOD")
                                .amount(new BigDecimal("3000.00"))
                                .build(),
                        CategorySpendingResponse.builder()
                                .category("TRAVEL")
                                .amount(new BigDecimal("2000.00"))
                                .build()
                ));

        List<TopSpendingCategoryResponse> response = reportService
                .getTopSpendingCategories(6,2026,2);


        Assertions.assertThat(response).hasSize(2);
        assertThat(response.get(0).rank()).isEqualTo(1);
        assertThat(response.get(0).category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(response.get(1).rank()).isEqualTo(2);
        assertThat(response.get(1).category()).isEqualTo(ExpenseCategory.TRAVEL);

        assertThat(response.get(0).percentageOfTotalSpending()).isEqualTo(60.0);
        assertThat(response.get(1).percentageOfTotalSpending()).isEqualTo(40.0);

        verify(expenseServiceClient).getCategoryBreakdown(6, 2026,
                "Bearer token");

   }

   @Test
   @DisplayName("should return empty top spending category list")
  void getTopSpendingCategories_ShouldReturnEmptyList_WhenNoExpensesExist(){

       when(expenseServiceClient.getCategoryBreakdown(2, 2026, "Bearer token"))
               .thenReturn(List.of());

       List<TopSpendingCategoryResponse> response = reportService
               .getTopSpendingCategories(2,2026,2);


       Assertions.assertThat(response).hasSize(0);

       verify(expenseServiceClient).getCategoryBreakdown(2, 2026,
               "Bearer token");
  }

  @Test
  @DisplayName("should return budget remaining for all categories when budget exists")
  void getBudgetRemaining_ShouldReturnAllCategories_WhenBudgetAndExpensesExist(){

      when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

      when(expenseServiceClient.getCategoryBreakdown(6, 2026, "Bearer token"))
              .thenReturn(List.of(
                      CategorySpendingResponse.builder()
                              .category("FOOD")
                              .amount(new BigDecimal("3000.00"))
                              .build(),
                      CategorySpendingResponse.builder()
                              .category("TRAVEL")
                              .amount(new BigDecimal("2000.00"))
                              .build()
              ));

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

      verify(expenseServiceClient).getCategoryBreakdown(6, 2026,
              "Bearer token");
      verify(budgetRepository).findAllByUserIdAndMonthAndYear(1L, 6, 2026);

  }


}
