package com.blackrock.aladdin.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable financial transaction.
 * Timestamp format in API: "yyyy-MM-dd HH:mm:ss".
 */
public record Transaction(
        String id,
        Instant timestamp,
        BigDecimal amount
) {
    public Transaction {
        if (id == null) throw new IllegalArgumentException("id required");
        if (timestamp == null) throw new IllegalArgumentException("timestamp required");
        if (amount == null) throw new IllegalArgumentException("amount required");
    }
}
