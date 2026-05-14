package com.bank.PAYMENT_SYSTEM.account.mapper;


import com.bank.PAYMENT_SYSTEM.account.dto.AccountResponse;
import com.bank.PAYMENT_SYSTEM.account.entity.Account;

public class AccountMapper {

    public static AccountResponse toResponse(
            Account account) {

        AccountResponse response =
                new AccountResponse();

        response.setId(account.getId());

        response.setAccountNumber(
                account.getAccountNumber());

        response.setBalance(
                account.getBalance());

        response.setAccountType(
                account.getAccountType());

        response.setAccountStatus(
                account.getAccountStatus());

        return response;
    }
}