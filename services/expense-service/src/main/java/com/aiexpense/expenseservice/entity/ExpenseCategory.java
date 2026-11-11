package com.aiexpense.expenseservice.entity;

public enum ExpenseCategory {

    FOOD,
    TRANSPORT,
    ENTERTAINMENT,
    UTILITIES,
    HEALTHCARE,
    EDUCATION,
    SHOPPING,
    PERSONAL_CARE,
    INSURANCE,
    OTHER;


    public static boolean isValid(String category) {
        try {
            ExpenseCategory.valueOf(category.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
