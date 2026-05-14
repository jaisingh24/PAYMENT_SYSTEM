package com.bank.PAYMENT_SYSTEM.transaction.repository;



import com.bank.PAYMENT_SYSTEM.transaction.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {
}