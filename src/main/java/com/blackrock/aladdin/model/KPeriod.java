package com.blackrock.aladdin.model;

import java.time.Instant;

/**
 * K period bucket: [startInclusive, endExclusive).
 */
public record KPeriod(
        String id,
        Instant startInclusive,
        Instant endExclusive
) {
    public KPeriod {
        if (id == null) throw new IllegalArgumentException("id required");
        if (startInclusive == null) throw new IllegalArgumentException("startInclusive required");
        if (endExclusive == null) throw new IllegalArgumentException("endExclusive required");
    }
}
