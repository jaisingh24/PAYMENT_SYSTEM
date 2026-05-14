package com.bank.PAYMENT_SYSTEM.service;



import com.bank.PAYMENT_SYSTEM.account.entity.Account;
import com.bank.PAYMENT_SYSTEM.account.repository.AccountRepository;

import com.bank.PAYMENT_SYSTEM.common.enums.AccountStatus;

import com.bank.PAYMENT_SYSTEM.common.enums.TransactionStatus;
import com.bank.PAYMENT_SYSTEM.common.enums.TransactionType;
import com.bank.PAYMENT_SYSTEM.exception.AccountFrozenException;
import com.bank.PAYMENT_SYSTEM.exception.InsufficientBalanceException;
import com.bank.PAYMENT_SYSTEM.transaction.dto.DepositRequest;
import com.bank.PAYMENT_SYSTEM.transaction.dto.TransactionHistoryResponse;
import com.bank.PAYMENT_SYSTEM.transaction.dto.
        TransferRequest;

import com.bank.PAYMENT_SYSTEM.transaction.dto.WithdrawRequest;
import com.bank.PAYMENT_SYSTEM.transaction.entity.Transaction;
import com.bank.PAYMENT_SYSTEM.transaction.repository.IdempotencyRepository;
import com.bank.PAYMENT_SYSTEM.transaction.repository.
        TransactionRepository;

import com.bank.PAYMENT_SYSTEM.transaction.service.RedisIdempotencyService;
import com.bank.PAYMENT_SYSTEM.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceTest {

    @Mock
    private TransactionRepository
            transactionRepository;

    @Mock
    private AccountRepository
            accountRepository;

    @Mock
    private IdempotencyRepository
            idempotencyRepository;

    @Mock
    private RedisIdempotencyService
            redisIdempotencyService;

    @InjectMocks
    private TransactionService
            transactionService;

    private Account sender;

    private Account receiver;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        // SENDER

        sender = new Account();

        sender.setAccountNumber(
                "1111111111"
        );

        sender.setBalance(
                BigDecimal.valueOf(5000)
        );

        sender.setAccountStatus(
                AccountStatus.ACTIVE
        );

        // RECEIVER

        receiver = new Account();

        receiver.setAccountNumber(
                "2222222222"
        );

        receiver.setBalance(
                BigDecimal.valueOf(1000)
        );

        receiver.setAccountStatus(
                AccountStatus.ACTIVE
        );
    }

    @Test
    void transferMoney_shouldTransferSuccessfully() {

        // REQUEST

        TransferRequest request =
                new TransferRequest();

        request.setSenderAccountNumber(
                "1111111111"
        );

        request.setReceiverAccountNumber(
                "2222222222"
        );

        request.setAmount(
                BigDecimal.valueOf(1000)
        );

        // MOCK ACCOUNT LOOKUPS

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "2222222222"
                ))
                .thenReturn(
                        Optional.of(receiver)
                );

        // EXECUTE
        when(transactionRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        transactionService.transferMoney(
                request,
                "idem-key-123"
        );

        // VERIFY BALANCES

        assertEquals(
                BigDecimal.valueOf(4000),
                sender.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(2000),
                receiver.getBalance()
        );

        // VERIFY SAVES

        verify(accountRepository,
                times(1))
                .save(sender);

        verify(accountRepository,
                times(1))
                .save(receiver);

        verify(transactionRepository,
                times(1))
                .save(any());
    }


    @Test
    void transferMoney_shouldThrowException_whenBalanceInsufficient() {

        // REQUEST

        TransferRequest request =
                new TransferRequest();

        request.setSenderAccountNumber(
                "1111111111"
        );

        request.setReceiverAccountNumber(
                "2222222222"
        );

        request.setAmount(
                BigDecimal.valueOf(10000)
        );

        // MOCK ACCOUNT LOOKUPS

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "2222222222"
                ))
                .thenReturn(
                        Optional.of(receiver)
                );

        // VERIFY EXCEPTION

        assertThrows(

                InsufficientBalanceException.class,

                () -> transactionService.transferMoney(
                        request,
                        "idem-key-456"
                )
        );

        // VERIFY BALANCES UNCHANGED

        assertEquals(
                BigDecimal.valueOf(5000),
                sender.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(1000),
                receiver.getBalance()
        );

        // VERIFY NO TRANSACTION SAVED

        verify(transactionRepository,
                never())
                .save(any());
    }
    @Test
    void transferMoney_shouldThrowException_whenAccountFrozen() {

        // FREEZE SENDER ACCOUNT

        sender.setAccountStatus(
                AccountStatus.FROZEN
        );

        // REQUEST

        TransferRequest request =
                new TransferRequest();

        request.setSenderAccountNumber(
                "1111111111"
        );

        request.setReceiverAccountNumber(
                "2222222222"
        );

        request.setAmount(
                BigDecimal.valueOf(1000)
        );

        // MOCK ACCOUNT LOOKUPS

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "2222222222"
                ))
                .thenReturn(
                        Optional.of(receiver)
                );

        // VERIFY EXCEPTION

        assertThrows(

                AccountFrozenException.class,

                () -> transactionService.transferMoney(
                        request,
                        "idem-key-789"
                )
        );

        // VERIFY BALANCES UNCHANGED

        assertEquals(
                BigDecimal.valueOf(5000),
                sender.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(1000),
                receiver.getBalance()
        );

        // VERIFY NO TRANSACTION SAVED

        verify(transactionRepository,
                never())
                .save(any());
    }
    @Test
    void transferMoney_shouldNotTransferTwice_whenIdempotencyKeyExists() {

        // REQUEST

        TransferRequest request =
                new TransferRequest();

        request.setSenderAccountNumber(
                "1111111111"
        );

        request.setReceiverAccountNumber(
                "2222222222"
        );

        request.setAmount(
                BigDecimal.valueOf(1000)
        );

        // EXISTING TRANSACTION

        Transaction existingTransaction =
                new Transaction();

        existingTransaction.setTransactionReference(
                "TXN-OLD-123"
        );

        existingTransaction.setAmount(
                BigDecimal.valueOf(1000)
        );

        existingTransaction.setSenderAccount(
                sender
        );

        existingTransaction.setReceiverAccount(
                receiver
        );

        // IMPORTANT

        existingTransaction.setTransactionStatus(
                TransactionStatus.SUCCESS
        );

        existingTransaction.setTransactionType(
                TransactionType.TRANSFER
        );

        // REDIS RETURNS EXISTING TXN

        when(redisIdempotencyService
                .getTransactionReference(
                        "idem-key-existing"
                ))
                .thenReturn(
                        "TXN-OLD-123"
                );

        // TRANSACTION LOOKUP

        when(transactionRepository
                .findByTransactionReference(
                        "TXN-OLD-123"
                ))
                .thenReturn(
                        Optional.of(existingTransaction)
                );

        // EXECUTE

        transactionService.transferMoney(
                request,
                "idem-key-existing"
        );

        // VERIFY BALANCES UNCHANGED

        assertEquals(
                BigDecimal.valueOf(5000),
                sender.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(1000),
                receiver.getBalance()
        );

        // VERIFY NO NEW TRANSACTION SAVED

        verify(transactionRepository,
                never())
                .save(any());

        verify(accountRepository,
                never())
                .save(any());
    }
    @Test
    void transferMoney_shouldRollback_whenTransactionFails() {

        // REQUEST

        TransferRequest request =
                new TransferRequest();

        request.setSenderAccountNumber(
                "1111111111"
        );

        request.setReceiverAccountNumber(
                "2222222222"
        );

        request.setAmount(
                BigDecimal.valueOf(1000)
        );

        // MOCK ACCOUNT LOOKUPS

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "2222222222"
                ))
                .thenReturn(
                        Optional.of(receiver)
                );

        // FORCE FAILURE

        when(transactionRepository.save(any()))
                .thenThrow(
                        new RuntimeException(
                                "Database failure"
                        )
                );

        // VERIFY EXCEPTION

        assertThrows(

                RuntimeException.class,

                () -> transactionService.transferMoney(
                        request,
                        "idem-key-rollback"
                )
        );

        // VERIFY BALANCES CHANGED IN MEMORY

        // NOTE:
        // Mockito tests do NOT verify real DB rollback.
        // Real rollback verification requires integration test.

        assertEquals(
                BigDecimal.valueOf(4000),
                sender.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(2000),
                receiver.getBalance()
        );

        // VERIFY TRANSACTION SAVE ATTEMPTED

        verify(transactionRepository,
                times(1))
                .save(any());
    }
    @Test
    void depositMoney_shouldDepositSuccessfully() {

        // REQUEST

        DepositRequest request =
                new DepositRequest();

        request.setAccountNumber(
                "1111111111"
        );

        request.setAmount(
                BigDecimal.valueOf(2000)
        );

        // MOCK ACCOUNT LOOKUP

        when(accountRepository
                .findByAccountNumber(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        // MOCK SAVE

        when(transactionRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // EXECUTE

        transactionService.depositMoney(request);

        // VERIFY BALANCE

        assertEquals(
                BigDecimal.valueOf(7000),
                sender.getBalance()
        );

        // VERIFY SAVE

        verify(accountRepository,
                times(1))
                .save(sender);

        verify(transactionRepository,
                times(1))
                .save(any());
    }
    @Test
    void withdrawMoney_shouldWithdrawSuccessfully() {

        // REQUEST

        WithdrawRequest request =
                new WithdrawRequest();

        request.setAccountNumber(
                "1111111111"
        );

        request.setAmount(
                BigDecimal.valueOf(1000)
        );

        // MOCK ACCOUNT LOOKUP

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        // MOCK SAVE

        when(transactionRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // EXECUTE

        transactionService.withdrawMoney(request);

        // VERIFY BALANCE

        assertEquals(
                BigDecimal.valueOf(4000),
                sender.getBalance()
        );

        // VERIFY SAVE

        verify(accountRepository,
                times(1))
                .save(sender);

        verify(transactionRepository,
                times(1))
                .save(any());
    }
    @Test
    void withdrawMoney_shouldThrowException_whenBalanceInsufficient() {

        // REQUEST

        WithdrawRequest request =
                new WithdrawRequest();

        request.setAccountNumber(
                "1111111111"
        );

        request.setAmount(
                BigDecimal.valueOf(10000)
        );

        // MOCK ACCOUNT LOOKUP

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        // VERIFY EXCEPTION

        assertThrows(

                InsufficientBalanceException.class,

                () -> transactionService.withdrawMoney(
                        request
                )
        );

        // VERIFY BALANCE UNCHANGED

        assertEquals(
                BigDecimal.valueOf(5000),
                sender.getBalance()
        );

        // VERIFY NO TRANSACTION SAVE

        verify(transactionRepository,
                never())
                .save(any());
    }
    @Test
    void depositMoney_shouldThrowException_whenAccountFrozen() {

        // FREEZE ACCOUNT

        sender.setAccountStatus(
                AccountStatus.FROZEN
        );

        // REQUEST

        DepositRequest request =
                new DepositRequest();

        request.setAccountNumber(
                "1111111111"
        );

        request.setAmount(
                BigDecimal.valueOf(1000)
        );

        // MOCK ACCOUNT LOOKUP
        when(accountRepository
                .findByAccountNumber(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        // VERIFY EXCEPTION

        assertThrows(

                AccountFrozenException.class,

                () -> transactionService.depositMoney(
                        request
                )
        );

        // VERIFY BALANCE UNCHANGED

        assertEquals(
                BigDecimal.valueOf(5000),
                sender.getBalance()
        );

        // VERIFY NO TRANSACTION SAVE

        verify(transactionRepository,
                never())
                .save(any());
    }

    @Test
    void withdrawMoney_shouldThrowException_whenAccountFrozen() {

        // FREEZE ACCOUNT

        sender.setAccountStatus(
                AccountStatus.FROZEN
        );

        // REQUEST

        WithdrawRequest request =
                new WithdrawRequest();

        request.setAccountNumber(
                "1111111111"
        );

        request.setAmount(
                BigDecimal.valueOf(1000)
        );

        // MOCK ACCOUNT LOOKUP

        when(accountRepository
                .findByAccountNumberForUpdate(
                        "1111111111"
                ))
                .thenReturn(
                        Optional.of(sender)
                );

        // VERIFY EXCEPTION

        assertThrows(

                AccountFrozenException.class,

                () -> transactionService.withdrawMoney(
                        request
                )
        );

        // VERIFY BALANCE UNCHANGED

        assertEquals(
                BigDecimal.valueOf(5000),
                sender.getBalance()
        );

        // VERIFY NO TRANSACTION SAVE

        verify(transactionRepository,
                never())
                .save(any());
    }
    @Test
    void getTransactionHistory_shouldReturnTransactions() {

        // TRANSACTION

        Transaction transaction =
                new Transaction();

        transaction.setTransactionReference(
                "TXN-HISTORY-123"
        );

        transaction.setTransactionType(
                TransactionType.TRANSFER
        );

        transaction.setTransactionStatus(
                TransactionStatus.SUCCESS
        );

        transaction.setAmount(
                BigDecimal.valueOf(1000)
        );

        transaction.setSenderAccount(sender);

        transaction.setReceiverAccount(receiver);

        // MOCK REPOSITORY

        when(transactionRepository
                .findAllTransactionsByAccount(
                        "1111111111"
                ))
                .thenReturn(
                        List.of(transaction)
                );

        // EXECUTE

        List<TransactionHistoryResponse>
                responses =
                transactionService
                        .getTransactionHistory(
                                "1111111111"
                        );

        // VERIFY

        assertEquals(
                1,
                responses.size()
        );

        assertEquals(
                "TXN-HISTORY-123",
                responses.get(0)
                        .getTransactionReference()
        );
    }
}