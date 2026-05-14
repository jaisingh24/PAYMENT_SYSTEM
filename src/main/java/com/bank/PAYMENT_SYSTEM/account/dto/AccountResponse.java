package com.bank.PAYMENT_SYSTEM.account.dto;



import com.bank.PAYMENT_SYSTEM.common.enums.AccountStatus;
import com.bank.PAYMENT_SYSTEM.common.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountResponse {

    private UUID id;

    private String accountNumber;

    private BigDecimal balance;

    private AccountType accountType;

    private AccountStatus accountStatus;

    public AccountResponse() {
    }

    // GETTERS + SETTERS


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(
            String accountNumber) {

        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(
            BigDecimal balance) {

        this.balance = balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(
            AccountType accountType) {

        this.accountType = accountType;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(
            AccountStatus accountStatus) {

        this.accountStatus = accountStatus;
    }
}
