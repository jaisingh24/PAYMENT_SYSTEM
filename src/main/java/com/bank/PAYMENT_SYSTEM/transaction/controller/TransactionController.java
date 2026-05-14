package com.bank.PAYMENT_SYSTEM.transaction.controller;

import com.bank.PAYMENT_SYSTEM.transaction.dto.*;
import com.bank.PAYMENT_SYSTEM.transaction.service.TransactionService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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

            @RequestBody
            TransferRequest request,

            @RequestHeader(
                    "Idempotency-Key")
            String idempotencyKey
    ) {

        return transactionService
                .transferMoney(
                        request,
                        idempotencyKey
                );
    }
    @PostMapping("/withdraw")
    public TransactionResponse withdrawMoney(
            @RequestBody WithdrawRequest request) {

        return transactionService
                .withdrawMoney(request);
    }
    @GetMapping("/account/{accountNumber}")
    public List<TransactionHistoryResponse>
    getTransactionHistory(
            @PathVariable String accountNumber) {

        return transactionService
                .getTransactionHistory(
                        accountNumber
                );
    }
}