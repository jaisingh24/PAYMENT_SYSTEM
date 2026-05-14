package com.bank.PAYMENT_SYSTEM.transaction.repository;

import com.bank.PAYMENT_SYSTEM.transaction.entity.IdempotencyRecord;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, UUID> {

    Optional<IdempotencyRecord>
    findByIdempotencyKey(
            String idempotencyKey
    );
}