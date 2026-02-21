package com.blackrock.aladdin.model;

import java.math.BigDecimal;

/**
 * Per–k-period running totals (prefix-sum style).
 */
public record PeriodAccumulator(
        String periodId,
        BigDecimal totalContribution,
        BigDecimal totalRemanent,
        BigDecimal pAdditive
) {
    public static PeriodAccumulator empty(String periodId) {
        return new PeriodAccumulator(periodId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public PeriodAccumulator add(BigDecimal contribution, BigDecimal remanent) {
        return new PeriodAccumulator(
                periodId,
                totalContribution.add(contribution),
                totalRemanent.add(remanent),
                this.pAdditive
        );
    }

    /** Set period-level p additive (from timeline: cumulative p at end - at start). */
    public PeriodAccumulator withPAdditive(BigDecimal pAdditive) {
        return new PeriodAccumulator(periodId, totalContribution, totalRemanent, pAdditive);
    }
}
