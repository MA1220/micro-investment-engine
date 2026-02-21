package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.PeriodAccumulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NPS and Index returns, tax cap, and inflation adjustment.
 */
@DisplayName("ReturnsEngine Tests")
class ReturnsEngineTest {

    @Test
    @DisplayName("shouldCalculateReturnsCorrectly")
    void shouldCalculateReturnsCorrectly() {
        List<PeriodAccumulator> accumulators = List.of(
                new PeriodAccumulator("K1", new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("100"))
        );
        BigDecimal inflationRate = new BigDecimal("0.02");
        BigDecimal taxCap = new BigDecimal("500");

        List<ReturnsEngine.ReturnResult> nps = ReturnsEngine.npsReturns(accumulators, inflationRate, taxCap);
        List<ReturnsEngine.ReturnResult> index = ReturnsEngine.indexReturns(accumulators, inflationRate);

        assertEquals(1, nps.size());
        assertEquals(new BigDecimal("1100"), nps.get(0).grossReturn()); // 1000 + 100
        assertEquals(new BigDecimal("500"), nps.get(0).taxBenefit());     // min(1100, 500)
        assertTrue(nps.get(0).inflationAdjustedReturn().compareTo(new BigDecimal("1078")) > 0);
        assertTrue(nps.get(0).inflationAdjustedReturn().compareTo(new BigDecimal("1079")) < 0);

        assertEquals(new BigDecimal("1100"), index.get(0).grossReturn());
        assertEquals(BigDecimal.ZERO, index.get(0).taxBenefit());
    }

    @Test
    @DisplayName("shouldCapTaxBenefitAtTaxCap")
    void shouldCapTaxBenefitAtTaxCap() {
        List<PeriodAccumulator> accumulators = List.of(
                new PeriodAccumulator("K1", new BigDecimal("10000"), BigDecimal.ZERO, BigDecimal.ZERO)
        );
        List<ReturnsEngine.ReturnResult> nps = ReturnsEngine.npsReturns(accumulators, BigDecimal.ZERO, new BigDecimal("500"));
        assertEquals(new BigDecimal("500"), nps.get(0).taxBenefit());
    }

    @Test
    @DisplayName("shouldApplyInflationAdjustment")
    void shouldApplyInflationAdjustment() {
        List<PeriodAccumulator> accumulators = List.of(
                new PeriodAccumulator("K1", new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO)
        );
        List<ReturnsEngine.ReturnResult> result = ReturnsEngine.indexReturns(accumulators, new BigDecimal("0.1"));
        // 100 / 1.1 ≈ 90.909...
        assertTrue(result.get(0).inflationAdjustedReturn().compareTo(new BigDecimal("90.90")) > 0);
        assertTrue(result.get(0).inflationAdjustedReturn().compareTo(new BigDecimal("90.91")) < 0);
    }

    @Test
    @DisplayName("shouldNotAdjustWhenInflationZero")
    void shouldNotAdjustWhenInflationZero() {
        List<PeriodAccumulator> accumulators = List.of(
                new PeriodAccumulator("K1", new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("50"))
        );
        List<ReturnsEngine.ReturnResult> nps = ReturnsEngine.npsReturns(accumulators, BigDecimal.ZERO, new BigDecimal("10000"));
        assertEquals(new BigDecimal("1050"), nps.get(0).inflationAdjustedReturn());
    }

    @Test
    @DisplayName("indexShouldHaveZeroTaxBenefit")
    void indexShouldHaveZeroTaxBenefit() {
        List<PeriodAccumulator> accumulators = List.of(
                new PeriodAccumulator("K1", new BigDecimal("5000"), BigDecimal.ZERO, BigDecimal.ZERO)
        );
        List<ReturnsEngine.ReturnResult> index = ReturnsEngine.indexReturns(accumulators, BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, index.get(0).taxBenefit());
    }

    @Test
    @DisplayName("shouldHandleEmptyAccumulators")
    void shouldHandleEmptyAccumulators() {
        List<ReturnsEngine.ReturnResult> nps = ReturnsEngine.npsReturns(List.of(), BigDecimal.ZERO, BigDecimal.ONE);
        List<ReturnsEngine.ReturnResult> index = ReturnsEngine.indexReturns(List.of(), BigDecimal.ZERO);
        assertTrue(nps.isEmpty());
        assertTrue(index.isEmpty());
    }

    @Test
    @DisplayName("financialPrecisionMultiplePeriods")
    void financialPrecisionMultiplePeriods() {
        List<PeriodAccumulator> accumulators = List.of(
                new PeriodAccumulator("K1", new BigDecimal("100.50"), new BigDecimal("0.25"), new BigDecimal("10.10")),
                new PeriodAccumulator("K2", new BigDecimal("200.75"), BigDecimal.ZERO, new BigDecimal("20.25"))
        );
        List<ReturnsEngine.ReturnResult> nps = ReturnsEngine.npsReturns(accumulators, new BigDecimal("0.02"), new BigDecimal("1000"));
        assertEquals(2, nps.size());
        assertEquals(new BigDecimal("110.60"), nps.get(0).grossReturn()); // 100.50 + 10.10
        assertEquals(new BigDecimal("221.00"), nps.get(1).grossReturn()); // 200.75 + 20.25
    }
}
