package com.aiexpense.expenseservice.controller;

import com.aiexpense.expenseservice.dto.CreateExpenseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private static final String TEST_JWT = "Bearer eyJhbGc...";

    @Test
    void testCreateExpense_Success() throws Exception {
        CreateExpenseRequest request = CreateExpenseRequest.builder()
                .description("Lunch")
                .amount(new BigDecimal("200.00"))
                .category("FOOD")
                .expenseDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/expenses")
                        .header("Authorization", TEST_JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    void testGetAllExpenses_WithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/expenses?page=0&size=20")
                        .header("Authorization", TEST_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.content").isArray());
    }
}
