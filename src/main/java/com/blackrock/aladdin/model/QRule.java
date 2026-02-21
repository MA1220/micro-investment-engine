package com.blackrock.aladdin.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Override rule: effective from startTime; latest start wins for any given time.
 */
public record QRule(
        String id,
        Instant startTime,
        BigDecimal ceilingOverride
) {
    public QRule {
        if (id == null) throw new IllegalArgumentException("id required");
        if (startTime == null) throw new IllegalArgumentException("startTime required");
        if (ceilingOverride == null) throw new IllegalArgumentException("ceilingOverride required");
    }
}
