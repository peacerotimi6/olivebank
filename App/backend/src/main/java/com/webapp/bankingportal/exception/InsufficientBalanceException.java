package com.webapp.bankingportal.exception;

public class InsufficientBalanceException extends IllegalArgumentException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
