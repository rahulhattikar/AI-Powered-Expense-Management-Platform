package com.AIExpense.ExpenseTracker.budget.service;

import com.AIExpense.ExpenseTracker.budget.dto.BudgetRequest;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetResponse;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetStatusResponse;
import com.AIExpense.ExpenseTracker.budget.entity.Budget;
import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;
import com.AIExpense.ExpenseTracker.common.exception.BudgetNotFoundException;
import com.AIExpense.ExpenseTracker.common.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.service.ExpenseService;
import com.AIExpense.ExpenseTracker.user.entity.Role;
import com.AIExpense.ExpenseTracker.user.entity.User;
import com.AIExpense.ExpenseTracker.util.AuthenticationUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private AuthenticationUtils authenticationUtils;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User testUser;
    private Budget testBudget;
    private BudgetRequest budgetRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Rahul")
                .email("rahul@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        testBudget = Budget.builder()
                .id(1L)
                .user(testUser)
                .category(ExpenseCategory.FOOD)
                .monthlyLimit(new BigDecimal("5000.00"))
                .month(6)
                .year(2026)
                .build();

        budgetRequest = new BudgetRequest(
                ExpenseCategory.FOOD,
                new BigDecimal("5000.00"),
                6,
                2026
        );
    }


    @Test
    @DisplayName("should return WITHIN_BUDGET status when spending is below monthly limit")
    void getBudgetStatus_ShouldReturnWithinBudget_WhenSpendingBelowLimit() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findActiveBudget(1L, ExpenseCategory.FOOD, 6, 2026))
                .thenReturn(Optional.of(testBudget));
        when(expenseService.getTotalSpendingByCategory(ExpenseCategory.FOOD))
                .thenReturn(new BigDecimal("3000.00")); // less than 5000 limit

        BudgetStatusResponse response = budgetService
                .getBudgetStatus(ExpenseCategory.FOOD, 6, 2026);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("WITHIN_BUDGET");
        assertThat(response.monthlyLimit()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(response.actualSpending()).isEqualTo(new BigDecimal("3000.00"));
        assertThat(response.remainingAmount()).isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("should return true when budget exists for given category, month and year")
    void isBudgetExists_ShouldReturnTrue_WhenBudgetExists() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.existsByUserIdAndCategoryAndMonthAndYear(
                1L, ExpenseCategory.FOOD, 6, 2026))
                .thenReturn(true);

        boolean exists = budgetService.isBudgetExists(ExpenseCategory.FOOD, 6, 2026);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should return BudgetResponse when create request is valid")
    void createBudget_ShouldReturnBudgetResponse_WhenValidRequest() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        BudgetResponse response = budgetService.createBudget(budgetRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.category()).isEqualTo(ExpenseCategory.FOOD);

        verify(authenticationUtils).getCurrentUser();
        verify(budgetRepository).save(any(Budget.class));
    }


    @Test
    @DisplayName("should return BudgetResponse when budget exists with given id")
    void getBudgetById_ShouldReturnBudgetResponse_WhenBudgetExists() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testBudget));

        BudgetResponse response = budgetService.getBudgetById(1L);

        assertThat(testBudget).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(response.month()).isEqualTo(6);
        assertThat(response.year()).isEqualTo(2026);

        verify(authenticationUtils).getCurrentUser();
        verify(budgetRepository).findByIdAndUserId(1L, 1L);

    }

    @Test
    @DisplayName("should throw BudgetNotFoundException when budget id does not exist")
    void getBudgetById_ShouldThrowBudgetNotFoundException_WhenNotFound() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByIdAndUserId(2L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetById(2L))
                .isInstanceOf(BudgetNotFoundException.class)
                .hasMessageContaining("Budget not found with id");

        verify(budgetRepository).findByIdAndUserId(2L, 1L);


    }

    @Test
    @DisplayName("should return paged response with budgets when budgets exist")
    void getAllBudgetsPaged_ShouldReturnPagedResponse_WhenBudgetsExist() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);

        Page<Budget> budgetPage = new PageImpl<>(
                List.of(testBudget),
                PageRequest.of(0, 20),
                1
        );

        when(budgetRepository.findAllByUserId(eq(1L) , any(Pageable.class)))
                .thenReturn(budgetPage);

        PagedResponse<BudgetResponse> response = budgetService.getAllBudgetsPaged(
                0,20, "createdAt" , "DESC"
        );

        assertThat(response).isNotNull();
        Assertions.assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(1L);
        assertThat(response.currentPage()).isEqualTo(0);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();

        verify(budgetRepository).findAllByUserId(eq(1L), any(Pageable.class));

    }

    @Test
    @DisplayName("should return empty paged response when no budgets exist")
    void getAllBudgetsPaged_ShouldReturnEmptyPagedResponse_WhenNoBudgetsExist() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);

        Page<Budget> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(budgetRepository.findAllByUserId(eq(1L) , any(Pageable.class)))
                .thenReturn(emptyPage);

        PagedResponse<BudgetResponse> response = budgetService.getAllBudgetsPaged(
                0,20, "createdAt" , "DESC"
        );

        Assertions.assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);

        verify(budgetRepository).findAllByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("should return updated BudgetResponse when budget exists")
    void updateBudget_ShouldReturnUpdatedBudgetResponse_WhenBudgetExists() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(testBudget);

        BudgetResponse response = budgetService.updateBudget(1L, budgetRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(response.monthlyLimit()).isEqualTo(new BigDecimal("5000.00"));

        verify(budgetRepository).findByIdAndUserId(1L, 1L);
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("should throw BudgetNotFoundException when budget id does not exist for update")
    void updateBudget_ShouldThrowBudgetNotFoundException_WhenNotFound() {


        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByIdAndUserId(2L, 1L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> budgetService.updateBudget(2L, budgetRequest))
                .isInstanceOf(BudgetNotFoundException.class)
                .hasMessageContaining("Budget not found");

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("should delete budget when budget exists")
    void deleteBudget_ShouldDeleteBudget_WhenBudgetExists() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testBudget));

        budgetService.deleteBudget(1L);

        verify(budgetRepository).delete(testBudget);
    }

    @Test
    @DisplayName("should throw BudgetNotFoundException when budget id does not exist for delete")
    void deleteBudget_ShouldThrowBudgetNotFoundException_WhenNotFound() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByIdAndUserId(2L, 1L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> budgetService.deleteBudget(2L))
                .isInstanceOf(BudgetNotFoundException.class)
                .hasMessageContaining("Budget not found");

        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    @Test
    @DisplayName("should return false when budget does not exist for given category, month and year")
    void isBudgetExists_ShouldReturnFalse_WhenBudgetNotExists() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.existsByUserIdAndCategoryAndMonthAndYear
                (1L, ExpenseCategory.HEALTH, 4, 2025))
                .thenReturn(false);

        Boolean result = budgetService.isBudgetExists(ExpenseCategory.HEALTH, 4, 2025);

        assertThat(result).isFalse();

        verify(budgetRepository).existsByUserIdAndCategoryAndMonthAndYear
                (1L, ExpenseCategory.HEALTH, 4, 2025);
    }


    @Test
    @DisplayName("should return EXCEEDED status when spending is above monthly limit")
    void getBudgetStatus_ShouldReturnExceeded_WhenSpendingAboveLimit() {

        when(authenticationUtils.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findActiveBudget(1L, ExpenseCategory.FOOD, 6, 2026))
                .thenReturn(Optional.of(testBudget));
        when(expenseService.getTotalSpendingByCategory(ExpenseCategory.FOOD))
                .thenReturn(new BigDecimal("6000.00")); // less than 5000 limit

        BudgetStatusResponse response = budgetService
                .getBudgetStatus(ExpenseCategory.FOOD, 6, 2026);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("EXCEEDED");
        assertThat(response.monthlyLimit()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(response.actualSpending()).isEqualTo(new BigDecimal("6000.00"));
        assertThat(response.remainingAmount()).isEqualTo(new BigDecimal("-1000.00"));
    }

}
