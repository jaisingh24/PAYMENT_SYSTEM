package com.bank.PAYMENT_SYSTEM.transaction.entity;



import com.bank.PAYMENT_SYSTEM.account.entity.Account;

import com.bank.PAYMENT_SYSTEM.common.enums.TransactionStatus;
import com.bank.PAYMENT_SYSTEM.common.enums.TransactionType;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id")
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id")
    private Account receiverAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // PRE PERSIST

    @PrePersist
    public void onCreate() {

        this.createdAt = LocalDateTime.now();

        if (this.transactionStatus == null) {

            this.transactionStatus =
                    TransactionStatus.PENDING;
        }
    }


    // GETTERS AND SETTERS


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(
            String transactionReference) {

        this.transactionReference =
                transactionReference;
    }

    public Account getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(
            Account senderAccount) {

        this.senderAccount = senderAccount;
    }

    public Account getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(
            Account receiverAccount) {

        this.receiverAccount =
                receiverAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(
            BigDecimal amount) {

        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(
            TransactionType transactionType) {

        this.transactionType =
                transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(
            TransactionStatus transactionStatus) {

        this.transactionStatus =
                transactionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}