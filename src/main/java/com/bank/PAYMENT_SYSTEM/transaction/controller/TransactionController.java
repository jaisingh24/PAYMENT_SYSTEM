package com.bank.PAYMENT_SYSTEM.transaction.controller;

import com.bank.PAYMENT_SYSTEM.transaction.dto.DepositRequest;
import com.bank.PAYMENT_SYSTEM.transaction.dto.TransactionResponse;
import com.bank.PAYMENT_SYSTEM.transaction.dto.TransferRequest;
import com.bank.PAYMENT_SYSTEM.transaction.service.TransactionService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }
    @GetMapping("/test")
    public String test() {

        return "TRANSACTION WORKING";
    }
    @PostMapping("/deposit")
    public TransactionResponse depositMoney(
            @RequestBody DepositRequest request) {

        return transactionService
                .depositMoney(request);
    }

    @PostMapping("/transfer")
    public TransactionResponse transferMoney(
            @RequestBody TransferRequest request) {

        return transactionService
                .transferMoney(request);
    }
}