package com.bank.PAYMENT_SYSTEM.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistoryResponse {

    private String transactionReference;

    private String transactionType;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private String status;

    private LocalDateTime createdAt;

    // GETTERS + SETTERS


    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(
            String transactionReference) {

        this.transactionReference =
                transactionReference;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(
            String transactionType) {

        this.transactionType =
                transactionType;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(
            String senderAccountNumber) {

        this.senderAccountNumber =
                senderAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(
            String receiverAccountNumber) {

        this.receiverAccountNumber =
                receiverAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(
            BigDecimal amount) {

        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}