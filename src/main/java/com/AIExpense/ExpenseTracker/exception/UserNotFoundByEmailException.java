package com.AIExpense.ExpenseTracker.exception;

public class UserNotFoundByEmailException extends RuntimeException{
    public UserNotFoundByEmailException(String message) {
        super(message);
    }
}
