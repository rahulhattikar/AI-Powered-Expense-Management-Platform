package com.AIExpense.ExpenseTracker.common.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {}
