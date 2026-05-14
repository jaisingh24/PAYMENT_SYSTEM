package com.bank.PAYMENT_SYSTEM.transaction.repository;

import com.bank.PAYMENT_SYSTEM.transaction.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE
                t.senderAccount.accountNumber
                    = :accountNumber

                OR

                t.receiverAccount.accountNumber
                    = :accountNumber

            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findAllTransactionsByAccount(
            @Param("accountNumber")
            String accountNumber
    );

    Optional<Transaction>
    findByTransactionReference(
            String transactionReference
    );
}