package com.bank.PAYMENT_SYSTEM.account.controller;


import com.bank.PAYMENT_SYSTEM.account.dto.AccountResponse;
import com.bank.PAYMENT_SYSTEM.account.dto.CreateAccountRequest;
import com.bank.PAYMENT_SYSTEM.account.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(
            AccountService accountService) {

        this.accountService = accountService;
    }

    // CREATE ACCOUNT

    @PostMapping
    public AccountResponse createAccount(
            @RequestBody
            CreateAccountRequest request) {

        return accountService.createAccount(
                request);
    }

    // GET ACCOUNT

    @GetMapping("/{id}")
    public AccountResponse getAccount(
            @PathVariable UUID id) {

        return accountService.getAccountById(id);
    }

    // FREEZE ACCOUNT

    @PutMapping("/{id}/freeze")
    public String freezeAccount(
            @PathVariable UUID id) {

        return accountService.freezeAccount(id);
    }

    // UNFREEZE ACCOUNT

    @PutMapping("/{id}/unfreeze")
    public String unfreezeAccount(
            @PathVariable UUID id) {

        return accountService.unfreezeAccount(id);
    }
}