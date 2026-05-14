package com.bank.PAYMENT_SYSTEM.exception;

public class InsufficientBalanceException
        extends RuntimeException {

    public InsufficientBalanceException(
            String message) {

        super(message);
    }
}