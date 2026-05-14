package com.bank.PAYMENT_SYSTEM.account.repository;

import com.bank.PAYMENT_SYSTEM.account.entity.Account;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(
            String accountNumber);

    // PESSIMISTIC LOCK

    @Lock(LockModeType.PESSIMISTIC_WRITE)

    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            """)
    Optional<Account> findByAccountNumberForUpdate(
            @Param("accountNumber")
            String accountNumber
    );
}