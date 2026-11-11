package com.aiexpense.expenseservice.controller;

import com.aiexpense.expenseservice.dto.CreateExpenseRequest;
import com.aiexpense.expenseservice.event.ExpenseEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ImportAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseEventProducer expenseEventProducer;


    @Test
    @WithMockUser(username = "1")
    void testCreateExpense_Success() throws Exception {
        CreateExpenseRequest request = CreateExpenseRequest.builder()
                .description("Lunch")
                .amount(new BigDecimal("200.00"))
                .category("FOOD")
                .expenseDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    @WithMockUser(username = "1")
    void testGetAllExpenses_WithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/expenses")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.content").isArray());
    }
}
