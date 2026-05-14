package com.bank.PAYMENT_SYSTEM.transaction.service;

import com.bank.PAYMENT_SYSTEM.account.entity.Account;
import com.bank.PAYMENT_SYSTEM.account.repository.AccountRepository;

import com.bank.PAYMENT_SYSTEM.common.enums.AccountStatus;
import com.bank.PAYMENT_SYSTEM.common.enums.TransactionStatus;
import com.bank.PAYMENT_SYSTEM.common.enums.TransactionType;

import com.bank.PAYMENT_SYSTEM.common.util.
        TransactionReferenceGenerator;

import com.bank.PAYMENT_SYSTEM.exception.
        AccountFrozenException;

import com.bank.PAYMENT_SYSTEM.exception.
        InsufficientBalanceException;

import com.bank.PAYMENT_SYSTEM.exception.
        ResourceNotFoundException;

import com.bank.PAYMENT_SYSTEM.transaction.dto.DepositRequest;
import com.bank.PAYMENT_SYSTEM.transaction.dto.
        TransactionResponse;

import com.bank.PAYMENT_SYSTEM.transaction.dto.
        TransferRequest;

import com.bank.PAYMENT_SYSTEM.transaction.entity.
        Transaction;

import com.bank.PAYMENT_SYSTEM.transaction.mapper.
        TransactionMapper;

import com.bank.PAYMENT_SYSTEM.transaction.repository.
        TransactionRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository
            transactionRepository;

    private final AccountRepository
            accountRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository) {

        this.transactionRepository =
                transactionRepository;

        this.accountRepository =
                accountRepository;
    }

    // TRANSFER MONEY

    @Transactional
    public TransactionResponse transferMoney( TransferRequest request) {

        // FETCH ACCOUNTS

        Account sender =
                accountRepository
                        .findByAccountNumber(
                                request
                                        .getSenderAccountNumber()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sender account not found"
                                ));

        Account receiver =
                accountRepository
                        .findByAccountNumber(
                                request
                                        .getReceiverAccountNumber()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Receiver account not found"
                                ));

        // CHECK ACCOUNT STATUS

        if (sender.getAccountStatus()
                == AccountStatus.FROZEN) {

            throw new AccountFrozenException(
                    "Sender account is frozen"
            );
        }

        if (receiver.getAccountStatus()
                == AccountStatus.FROZEN) {

            throw new AccountFrozenException(
                    "Receiver account is frozen"
            );
        }

        // CHECK BALANCE

        if (sender.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }

        // UPDATE BALANCES

        sender.setBalance(
                sender.getBalance()
                        .subtract(request.getAmount())
        );

        receiver.setBalance(
                receiver.getBalance()
                        .add(request.getAmount())
        );

        // SAVE UPDATED ACCOUNTS

        accountRepository.save(sender);

        accountRepository.save(receiver);

        // CREATE TRANSACTION

        Transaction transaction =
                new Transaction();

        transaction.setSenderAccount(sender);

        transaction.setReceiverAccount(receiver);

        transaction.setAmount(
                request.getAmount());

        transaction.setTransactionType(
                TransactionType.TRANSFER
        );

        transaction.setTransactionStatus(
                TransactionStatus.SUCCESS
        );

        transaction.setTransactionReference(
                TransactionReferenceGenerator
                        .generateReference()
        );

        Transaction savedTransaction =
                transactionRepository
                        .save(transaction);

        return TransactionMapper.toResponse(
                savedTransaction);
    }

    @Transactional
    public TransactionResponse depositMoney(
            DepositRequest request) {

        // FETCH ACCOUNT

        Account account =
                accountRepository
                        .findByAccountNumber(
                                request.getAccountNumber()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found"
                                ));

        // CHECK ACCOUNT STATUS

        if (account.getAccountStatus()
                == AccountStatus.FROZEN) {

            throw new AccountFrozenException(
                    "Account is frozen"
            );
        }

        // ADD MONEY

        account.setBalance(
                account.getBalance()
                        .add(request.getAmount())
        );

        accountRepository.save(account);

        // CREATE TRANSACTION

        Transaction transaction =
                new Transaction();

        transaction.setReceiverAccount(account);

        transaction.setAmount(
                request.getAmount());

        transaction.setTransactionType(
                TransactionType.DEPOSIT
        );

        transaction.setTransactionStatus(
                TransactionStatus.SUCCESS
        );

        transaction.setTransactionReference(
                TransactionReferenceGenerator
                        .generateReference()
        );

        Transaction savedTransaction =
                transactionRepository
                        .save(transaction);

        return TransactionMapper.toResponse(
                savedTransaction);
    }
}