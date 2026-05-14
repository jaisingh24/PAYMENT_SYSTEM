package com.bank.PAYMENT_SYSTEM.transaction.dto;



import java.math.BigDecimal;

public class TransactionResponse {

    private String transactionReference;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private String status;

    // GETTERS + SETTERS


    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(
            String transactionReference) {

        this.transactionReference =
                transactionReference;
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
}