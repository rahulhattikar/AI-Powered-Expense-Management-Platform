package com.AIExpense.ExpenseTracker.expense.service;


import com.AIExpense.ExpenseTracker.common.exception.ExpenseNotFoundException;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseRequest;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.common.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.Expense;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.repository.ExpenseRepository;
import com.AIExpense.ExpenseTracker.kafka.event.ExpenseCreatedEvent;
import com.AIExpense.ExpenseTracker.kafka.producer.ExpenseEventProducer;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private AuthenticationUtils authenticationUtils;

    @Mock
    private ExpenseEventProducer expenseEventProducer;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense testExpense;
    private ExpenseRequest expenseRequest;
    private ExpenseCreatedEvent expenseCreatedEvent;

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

        expenseRequest = new ExpenseRequest(
                new BigDecimal("500.00"),
                "Grocery shopping",
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 6, 1)
        );
    }

    @Test
    @DisplayName("Should return ExpenseResponse when valid request")
    void createExpense_ShouldReturnExpenseResponse_WhenValidRequest() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        ExpenseResponse response = expenseService.createExpense(expenseRequest);

        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualTo(new BigDecimal("500.00"));
        assertThat(response.category()).isEqualTo(ExpenseCategory.FOOD);

        verify(authenticationUtils).getCurrentUserId();
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseEventProducer).publishExpenseCreated(any(ExpenseCreatedEvent.class));
    }


    @Test
    @DisplayName("Should return ExpenseResponse when expense exists with given id")
    void getExpenseById_ShouldReturnExpenseResponse_WhenExpenseExists() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testExpense));

        ExpenseResponse response = expenseService.getExpenseById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.amount()).isEqualTo(new BigDecimal("500.00"));
        assertThat(response.category()).isEqualTo(ExpenseCategory.FOOD);

        verify(expenseRepository).findByIdAndUserId(1L, 1L);

    }

    @Test
    @DisplayName("Should throw ExpenseNotFoundException when expense id does not exist")
    void getExpenseById_ShouldThrowExpenseNotFoundException_WhenNotFound() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(2L, 1L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> expenseService.getExpenseById(2L))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessageContaining("Expense not found");


        verify(expenseRepository).findByIdAndUserId(2L, 1L);
    }

    @Test
    @DisplayName("should return paged response with expenses when expenses exist")
    void getAllExpensesPaged_ShouldReturnPagedResponse_WhenExpensesExist() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

        Page<Expense> expensePage = new PageImpl<>(
                List.of(testExpense),
                PageRequest.of(0, 20),
                1
        );

        when(expenseRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(expensePage);

        PagedResponse<ExpenseResponse> response = expenseService
                .getAllExpensesPaged(0, 20, "expenseDate", "DESC");

        assertThat(response).isNotNull();
        Assertions.assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(1L);
        assertThat(response.currentPage()).isEqualTo(0);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();

        verify(expenseRepository).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("should return empty paged response when no expenses exist")
    void getAllExpensesPaged_ShouldReturnEmptyPagedResponse_WhenNoExpensesExist() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);

        Page<Expense> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(expenseRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(emptyPage);


        PagedResponse<ExpenseResponse> response = expenseService
                .getAllExpensesPaged(0, 20, "expenseDate", "DESC");

        Assertions.assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);

        // Verify
        verify(expenseRepository).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return updated ExpenseResponse when expense exists")
    void updateExpense_ShouldReturnUpdatedExpenseResponse_WhenExpenseExists() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(testExpense);

        ExpenseResponse response = expenseService.updateExpense(1L, expenseRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.category()).isEqualTo(expenseRequest.category());
        assertThat(response.amount()).isEqualTo(expenseRequest.amount());

        verify(expenseRepository).findByIdAndUserId(1L, 1L);
        verify(expenseRepository).save(any(Expense.class));

    }

    @Test
    @DisplayName("Should throw ExpenseNotFoundException when expense not found for update")
    void updateExpense_ShouldThrowExpenseNotFoundException_WhenNotFound() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(2L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(2L, expenseRequest))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessageContaining("Expense not found");


        verify(expenseRepository, never()).save(any(Expense.class));

    }

    @Test
    @DisplayName("Should delete expense when expense exists")
    void deleteExpense_ShouldDeleteExpense_WhenExpenseExists() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testExpense));

        expenseService.deleteExpense(1L);

        verify(expenseRepository).delete(testExpense);
    }

    @Test
    @DisplayName("Should throw ExpenseNotFoundException when expense not found for delete")
    void deleteExpense_ShouldThrowExpenseNotFoundException_WhenNotFound() {

        when(authenticationUtils.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(2L, 1L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> expenseService.deleteExpense(2L))
                .isInstanceOf(ExpenseNotFoundException.class)
                .hasMessageContaining("Expense not found");

        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
