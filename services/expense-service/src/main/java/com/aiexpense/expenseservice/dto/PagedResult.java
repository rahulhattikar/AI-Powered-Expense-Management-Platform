package com.aiexpense.expenseservice.dto;

import lombok.Builder;

import java.util.List;



public record PagedResult<T>(

        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
