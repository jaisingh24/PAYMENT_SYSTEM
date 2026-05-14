package com.bank.PAYMENT_SYSTEM.integration;

import com.bank.PAYMENT_SYSTEM.account.entity.Account;

import com.bank.PAYMENT_SYSTEM.account.repository.
        AccountRepository;

import com.bank.PAYMENT_SYSTEM.common.enums.
        AccountStatus;

import com.bank.PAYMENT_SYSTEM.common.enums.
        AccountType;

import com.bank.PAYMENT_SYSTEM.common.enums.
        Role;

import com.bank.PAYMENT_SYSTEM.transaction.dto.
        TransferRequest;

import com.bank.PAYMENT_SYSTEM.transaction.repository.
        TransactionRepository;

import com.bank.PAYMENT_SYSTEM.transaction.service.
        TransactionService;

import com.bank.PAYMENT_SYSTEM.user.entity.
        User;

import com.bank.PAYMENT_SYSTEM.user.repository.
        UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.
        SpringBootTest;

import java.math.BigDecimal;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConcurrentTransferIntegrationTest {

    @Autowired
    private TransactionService
            transactionService;

    @Autowired
    private AccountRepository
            accountRepository;

    @Autowired
    private TransactionRepository
            transactionRepository;

    @Autowired
    private UserRepository
            userRepository;

    private Account sender;

    private Account receiver;

    private User user;

    @BeforeEach
    void setUp() {

        // CLEAN DATABASE

        transactionRepository.deleteAll();

        accountRepository.deleteAll();

        userRepository.deleteAll();

        // CREATE USER

        user = new User();

        user.setFullName(
                "Test User"
        );

        user.setEmail(
                "test@gmail.com"
        );

        user.setPhoneNumber(
                "9999999999"
        );

        user.setPasswordHash(
                "hashedPassword"
        );

        user.setEnabled(true);

        user.setRole(
                Role.USER
        );

        user = userRepository.save(user);

        // CREATE SENDER ACCOUNT

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

        sender.setAccountType(
                AccountType.SAVINGS
        );

        sender.setUser(user);

        sender = accountRepository.save(
                sender
        );

        // CREATE RECEIVER ACCOUNT

        receiver = new Account();

        receiver.setAccountNumber(
                "2222222222"
        );

        receiver.setBalance(
                BigDecimal.ZERO
        );

        receiver.setAccountStatus(
                AccountStatus.ACTIVE
        );

        receiver.setAccountType(
                AccountType.SAVINGS
        );

        receiver.setUser(user);

        receiver = accountRepository.save(
                receiver
        );
    }

    @Test
    void concurrentTransfers_shouldPreventNegativeBalance()
            throws Exception {

        int threadCount = 10;

        ExecutorService executorService =
                Executors.newFixedThreadPool(
                        threadCount
                );

        CountDownLatch latch =
                new CountDownLatch(
                        threadCount
                );

        for (int i = 0;
             i < threadCount;
             i++) {

            int finalI = i;

            executorService.submit(() -> {

                try {

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

                    transactionService.transferMoney(

                            request,

                            "concurrent-key-"
                                    + finalI
                    );

                } catch (Exception e) {

                    System.out.println(
                            e.getMessage()
                    );

                } finally {

                    latch.countDown();
                }
            });
        }

        latch.await();

        Account updatedSender =
                accountRepository
                        .findByAccountNumber(
                                "1111111111"
                        )
                        .orElseThrow();

        Account updatedReceiver =
                accountRepository
                        .findByAccountNumber(
                                "2222222222"
                        )
                        .orElseThrow();

        System.out.println(
                "Sender Balance = "
                        + updatedSender.getBalance()
        );

        System.out.println(
                "Receiver Balance = "
                        + updatedReceiver.getBalance()
        );

        // MOST IMPORTANT GUARANTEE:
        // BALANCE MUST NEVER GO NEGATIVE

        assertTrue(

                updatedSender
                        .getBalance()
                        .compareTo(BigDecimal.ZERO) >= 0
        );
    }
}