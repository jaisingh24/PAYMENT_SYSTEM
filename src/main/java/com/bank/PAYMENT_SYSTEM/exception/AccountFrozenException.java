package com.bank.PAYMENT_SYSTEM.exception;

public class AccountFrozenException
        extends RuntimeException {

    public AccountFrozenException(
            String message) {

        super(message);
    }
}