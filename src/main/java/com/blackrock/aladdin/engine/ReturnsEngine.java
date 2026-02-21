package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.PeriodAccumulator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 5: Compute NPS (with tax cap) and Index (no tax) returns; inflation-adjust.
 * Deterministic: no randomness; audit-friendly formulas.
 */
public final class ReturnsEngine {

    private static final int SCALE = 10;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    private ReturnsEngine() {}

    /**
     * Gross return = contribution + pAdditive (simplified value growth).
     * NPS: tax benefit = min(grossReturn * rate, taxCap) then inflation-adjust.
     * Index: no tax benefit; inflation-adjust only.
     */
    public static List<ReturnResult> npsReturns(
            List<PeriodAccumulator> periodAccumulators,
            BigDecimal inflationRate,
            BigDecimal taxCap
    ) {
        return periodAccumulators.stream()
                .map(acc -> {
                    BigDecimal contribution = acc.totalContribution().add(acc.pAdditive());
                    BigDecimal grossReturn = contribution; // simplified: no separate market return here
                    BigDecimal taxBenefit = grossReturn.min(taxCap);
                    BigDecimal afterTax = grossReturn; // NPS: gross is already tax-advantaged; cap limits benefit
                    BigDecimal inflationAdjusted = inflate(afterTax, inflationRate);
                    return new ReturnResult(acc.periodId(), grossReturn, taxBenefit, inflationAdjusted);
                })
                .collect(Collectors.toList());
    }

    public static List<ReturnResult> indexReturns(
            List<PeriodAccumulator> periodAccumulators,
            BigDecimal inflationRate
    ) {
        return periodAccumulators.stream()
                .map(acc -> {
                    BigDecimal grossReturn = acc.totalContribution().add(acc.pAdditive());
                    BigDecimal taxBenefit = BigDecimal.ZERO;
                    BigDecimal inflationAdjusted = inflate(grossReturn, inflationRate);
                    return new ReturnResult(acc.periodId(), grossReturn, taxBenefit, inflationAdjusted);
                })
                .collect(Collectors.toList());
    }

    private static BigDecimal inflate(BigDecimal value, BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) return value;
        return value.divide(BigDecimal.ONE.add(rate), SCALE, ROUND);
    }

    public record ReturnResult(String periodId, BigDecimal grossReturn, BigDecimal taxBenefit, BigDecimal inflationAdjustedReturn) {}
}
