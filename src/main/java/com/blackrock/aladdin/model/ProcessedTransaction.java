package com.blackrock.aladdin.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * After Step 1–3: transaction with allowed amount (under ceiling), remanent, and p additive at this time.
 */
public record ProcessedTransaction(
        String id,
        Instant timestamp,
        BigDecimal amount,
        BigDecimal allowed,
        BigDecimal remanent,
        BigDecimal pAdditiveAtTime
) {}
