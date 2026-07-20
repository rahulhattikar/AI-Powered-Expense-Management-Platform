package com.aiexpense.aiservice.exception;

public class ReportServiceUnavailableException extends RuntimeException {
    public ReportServiceUnavailableException(String message) { super(message); }
    public ReportServiceUnavailableException(String message, Throwable cause) { super(message, cause); }
}
