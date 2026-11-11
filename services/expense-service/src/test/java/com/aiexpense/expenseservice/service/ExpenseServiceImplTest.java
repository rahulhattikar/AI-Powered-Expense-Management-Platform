package com.aiexpense.expenseservice.service;

import com.aiexpense.expenseservice.dto.CreateExpenseRequest;
import com.aiexpense.expenseservice.dto.ExpenseMapper;
import com.aiexpense.expenseservice.dto.ExpenseResponse;
import com.aiexpense.expenseservice.dto.PagedResult;
import com.aiexpense.expenseservice.entity.Expense;
import com.aiexpense.expenseservice.entity.ExpenseCategory;
import com.aiexpense.expenseservice.event.ExpenseEvent;
import com.aiexpense.expenseservice.event.ExpenseEventProducer;
import com.aiexpense.expenseservice.exception.ExpenseNotFoundException;
import com.aiexpense.expenseservice.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private ExpenseEventProducer eventProducer;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @MockitoBean
    private KafkaTemplate<String, ExpenseEvent> kafkaTemplate;

    private Long userId = 1L;
    private CreateExpenseRequest createRequest;
    private Expense mockExpense;
    private ExpenseResponse mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createRequest = CreateExpenseRequest.builder()
                .description("Coffee")
                .amount(new BigDecimal("50.00"))
                .category("FOOD")
                .expenseDate(LocalDate.now())
                .build();

        mockExpense = Expense.builder()
                .id(1L)
                .userId(userId)
                .description("Coffee")
                .amount(new BigDecimal("50.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now())
                .build();

        mockResponse = ExpenseResponse.builder()
                .id(1L)
                .userId(userId)
                .description("Coffee")
                .amount(new BigDecimal("50.00"))
                .category("FOOD")
                .expenseDate(LocalDate.now())
                .build();
    }

    @Test
    void testCreateExpense_Success() {
        when(expenseMapper.toEntity(any(), any())).thenReturn(mockExpense);
        when(expenseRepository.save(any())).thenReturn(mockExpense);
        when(expenseMapper.toResponse(any())).thenReturn(mockResponse);

        ExpenseResponse response = expenseService.createExpense(userId, createRequest);

        assertNotNull(response);
        assertEquals("Coffee", response.getDescription());
        verify(expenseRepository, times(1)).save(any());
        verify(eventProducer, times(1)).publishExpenseCreated(any());
    }

    @Test
    void testGetExpenseById_Success() {
        when(expenseRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(mockExpense));
        when(expenseMapper.toResponse(any())).thenReturn(mockResponse);

        ExpenseResponse response = expenseService.getExpenseById(userId, 1L);

        assertNotNull(response);
        assertEquals("Coffee", response.getDescription());
    }

    @Test
    void testGetExpenseById_NotFound() {
        when(expenseRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () ->
                expenseService.getExpenseById(userId, 1L)
        );
    }

    @Test
    void testGetAllExpenses_Success() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Expense> mockPage = new PageImpl<>(List.of(mockExpense), pageable, 1);

        when(expenseRepository.findByUserIdAndIsDeletedFalse(userId, pageable))
                .thenReturn(mockPage);
        when(expenseMapper.toResponse(any())).thenReturn(mockResponse);

        PagedResult<ExpenseResponse> response =
                expenseService.getAllExpenses(userId, pageable);

        assertEquals(1, response.totalElements());
        assertEquals(1, response.content().size());
    }

    @Test
    void testDeleteExpense_Success() {
        when(expenseRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(mockExpense));
        when(expenseRepository.save(any())).thenReturn(mockExpense);

        expenseService.deleteExpense(userId, 1L);

        verify(expenseRepository, times(1)).save(any());
        verify(eventProducer, times(1)).publishExpenseDeleted(any());
    }
}