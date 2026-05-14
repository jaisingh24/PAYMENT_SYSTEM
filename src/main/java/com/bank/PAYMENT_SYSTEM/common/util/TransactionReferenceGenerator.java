package com.bank.PAYMENT_SYSTEM.common.util;



import java.util.UUID;

public class TransactionReferenceGenerator {

    public static String generateReference() {

        return "TXN-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
