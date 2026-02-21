package com.blackrock.aladdin.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Additive rule: from startTime, add delta cumulatively (all p rules sum).
 */
public record PRule(
        String id,
        Instant startTime,
        BigDecimal delta
) {
    public PRule {
        if (id == null) throw new IllegalArgumentException("id required");
        if (startTime == null) throw new IllegalArgumentException("startTime required");
        if (delta == null) throw new IllegalArgumentException("delta required");
    }
}
