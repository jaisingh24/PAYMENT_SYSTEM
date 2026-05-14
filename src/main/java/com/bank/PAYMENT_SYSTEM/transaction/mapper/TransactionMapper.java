package com.bank.PAYMENT_SYSTEM.transaction.mapper;

import com.bank.PAYMENT_SYSTEM.transaction.dto.TransactionHistoryResponse;
import com.bank.PAYMENT_SYSTEM.transaction.dto.TransactionResponse;
import com.bank.PAYMENT_SYSTEM.transaction.entity.Transaction;

public class TransactionMapper {

    public static TransactionResponse
    toResponse(Transaction transaction) {

        TransactionResponse response =
                new TransactionResponse();

        response.setTransactionReference(
                transaction.getTransactionReference()
        );

        // SAFE NULL CHECK

        if (transaction.getSenderAccount() != null) {

            response.setSenderAccountNumber(
                    transaction.getSenderAccount()
                            .getAccountNumber()
            );
        }

        if (transaction.getReceiverAccount() != null) {

            response.setReceiverAccountNumber(
                    transaction.getReceiverAccount()
                            .getAccountNumber()
            );
        }

        response.setAmount(
                transaction.getAmount()
        );

        response.setStatus(
                transaction.getTransactionStatus()
                        .name()
        );

        return response;
    }

    public static TransactionHistoryResponse
    toHistoryResponse(Transaction transaction) {

        TransactionHistoryResponse response =
                new TransactionHistoryResponse();

        response.setTransactionReference(
                transaction.getTransactionReference()
        );

        response.setTransactionType(
                transaction.getTransactionType()
                        .name()
        );

        if (transaction.getSenderAccount() != null) {

            response.setSenderAccountNumber(
                    transaction.getSenderAccount()
                            .getAccountNumber()
            );
        }

        if (transaction.getReceiverAccount() != null) {

            response.setReceiverAccountNumber(
                    transaction.getReceiverAccount()
                            .getAccountNumber()
            );
        }

        response.setAmount(
                transaction.getAmount()
        );

        response.setStatus(
                transaction.getTransactionStatus()
                        .name()
        );

        response.setCreatedAt(
                transaction.getCreatedAt()
        );

        return response;
    }
}