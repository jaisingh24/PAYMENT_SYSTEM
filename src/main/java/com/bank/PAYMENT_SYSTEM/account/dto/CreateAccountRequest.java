package com.bank.PAYMENT_SYSTEM.account.dto;





import com.bank.PAYMENT_SYSTEM.common.enums.AccountType;

import java.util.UUID;

public class CreateAccountRequest {

    private UUID userId;

    private AccountType accountType;

    public CreateAccountRequest() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(
            AccountType accountType) {

        this.accountType = accountType;
    }
}