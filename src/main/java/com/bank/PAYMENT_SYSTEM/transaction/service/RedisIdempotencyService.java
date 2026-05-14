package com.bank.PAYMENT_SYSTEM.transaction.service;

import org.springframework.data.redis.core.
        RedisTemplate;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisIdempotencyService {

    private final RedisTemplate<String, String>
            redisTemplate;

    public RedisIdempotencyService(
            RedisTemplate<String, String>
                    redisTemplate) {

        this.redisTemplate =
                redisTemplate;
    }

    // SAVE IDEMPOTENCY KEY

    public void saveIdempotencyKey(
            String key,
            String transactionReference
    ) {

        redisTemplate
                .opsForValue()
                .set(

                        buildKey(key),

                        transactionReference,

                        Duration.ofHours(24)
                );
    }

    // GET EXISTING TRANSACTION

    public String getTransactionReference(
            String key
    ) {

        return redisTemplate
                .opsForValue()
                .get(buildKey(key));
    }

    // REDIS KEY FORMAT

    private String buildKey(
            String key
    ) {

        return "idem:" + key;
    }
}