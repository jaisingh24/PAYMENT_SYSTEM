package com.bank.PAYMENT_SYSTEM.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("error", ex.getMessage());

        return response;
    }

    @ExceptionHandler(
            InsufficientBalanceException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object>
    handleInsufficientBalance(
            InsufficientBalanceException ex) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now());

        response.put(
                "error",
                ex.getMessage());

        return response;
    }

    @ExceptionHandler(
            AccountFrozenException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object>
    handleFrozenAccount(
            AccountFrozenException ex) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now());

        response.put(
                "error",
                ex.getMessage());

        return response;
    }
}
