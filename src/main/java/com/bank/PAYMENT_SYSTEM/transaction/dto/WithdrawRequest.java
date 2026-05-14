package com.bank.PAYMENT_SYSTEM.transaction.dto;

import java.math.BigDecimal;

public class WithdrawRequest {

    private String accountNumber;

    private BigDecimal amount;

    // GETTERS + SETTERS


    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(
            String accountNumber) {

        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(
            BigDecimal amount) {

        this.amount = amount;
    }
}