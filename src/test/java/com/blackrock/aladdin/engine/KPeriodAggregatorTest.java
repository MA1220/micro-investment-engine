package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.KPeriod;
import com.blackrock.aladdin.model.PeriodAccumulator;
import com.blackrock.aladdin.model.ProcessedTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for k-period aggregation (prefix-sum style range correctness).
 * Maps to "PrefixSumEngine" in design: range query correctness by period.
 */
@DisplayName("KPeriodAggregator (PrefixSum) Tests")
class KPeriodAggregatorTest {

    private static Instant instant(String s) {
        return Instant.parse(s.replace(" ", "T") + "Z");
    }

    private static List<KPeriod> threePeriods() {
        return List.of(
                new KPeriod("K1", instant("2024-01-01 00:00:00"), instant("2024-02-01 00:00:00")),
                new KPeriod("K2", instant("2024-02-01 00:00:00"), instant("2024-03-01 00:00:00")),
                new KPeriod("K3", instant("2024-03-01 00:00:00"), instant("2024-04-01 00:00:00"))
        );
    }

    @Test
    @DisplayName("shouldCalculatePrefixSumCorrectly")
    void shouldCalculatePrefixSumCorrectly() {
        List<KPeriod> kPeriods = threePeriods();
        List<ProcessedTransaction> processed = List.of(
                new ProcessedTransaction("tx1", instant("2024-01-15 09:30:00"), new BigDecimal("1000"),
                        new BigDecimal("1000"), BigDecimal.ZERO, BigDecimal.ZERO),
                new ProcessedTransaction("tx2", instant("2024-02-10 10:00:00"), new BigDecimal("500"),
                        new BigDecimal("500"), BigDecimal.ZERO, BigDecimal.ZERO),
                new ProcessedTransaction("tx3", instant("2024-03-15 14:00:00"), new BigDecimal("2000"),
                        new BigDecimal("1500"), new BigDecimal("500"), BigDecimal.ZERO)
        );

        NavigableMap<Instant, PeriodAccumulator> byPeriod = KPeriodAggregator.aggregate(processed, kPeriods);
        List<PeriodAccumulator> list = KPeriodAggregator.toOrderedList(byPeriod, kPeriods);

        assertEquals(3, list.size());
        assertEquals(new BigDecimal("1000"), list.get(0).totalContribution());
        assertEquals(new BigDecimal("500"), list.get(1).totalContribution());
        assertEquals(new BigDecimal("1500"), list.get(2).totalContribution());
        assertEquals(new BigDecimal("500"), list.get(2).totalRemanent());
    }

    @Test
    @DisplayName("shouldExcludeTransactionBeforeFirstPeriod")
    void shouldExcludeTransactionBeforeFirstPeriod() {
        List<KPeriod> kPeriods = threePeriods();
        List<ProcessedTransaction> processed = List.of(
                new ProcessedTransaction("tx0", instant("2023-12-01 00:00:00"), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO)
        );

        NavigableMap<Instant, PeriodAccumulator> byPeriod = KPeriodAggregator.aggregate(processed, kPeriods);
        List<PeriodAccumulator> list = KPeriodAggregator.toOrderedList(byPeriod, kPeriods);

        assertEquals(BigDecimal.ZERO, list.get(0).totalContribution());
        assertEquals(BigDecimal.ZERO, list.get(1).totalContribution());
        assertEquals(BigDecimal.ZERO, list.get(2).totalContribution());
    }

    @Test
    @DisplayName("shouldExcludeTransactionAtOrAfterPeriodEnd")
    void shouldExcludeTransactionAtOrAfterPeriodEnd() {
        // Transaction exactly at 2024-02-01 00:00:00 belongs to K2 [2024-02-01, 2024-03-01), not K1
        List<KPeriod> kPeriods = threePeriods();
        List<ProcessedTransaction> processed = List.of(
                new ProcessedTransaction("tx1", instant("2024-02-01 00:00:00"), new BigDecimal("100"),
                        new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO)
        );

        NavigableMap<Instant, PeriodAccumulator> byPeriod = KPeriodAggregator.aggregate(processed, kPeriods);
        List<PeriodAccumulator> list = KPeriodAggregator.toOrderedList(byPeriod, kPeriods);

        assertEquals(BigDecimal.ZERO, list.get(0).totalContribution());
        assertEquals(new BigDecimal("100"), list.get(1).totalContribution());
        assertEquals(BigDecimal.ZERO, list.get(2).totalContribution());
    }

    @Test
    @DisplayName("boundaryTimestampExactlyOnPeriodStart")
    void boundaryTimestampExactlyOnPeriodStart() {
        List<KPeriod> kPeriods = threePeriods();
        Instant boundary = instant("2024-03-01 00:00:00");
        List<ProcessedTransaction> processed = List.of(
                new ProcessedTransaction("tx1", boundary, new BigDecimal("50"), new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO)
        );

        NavigableMap<Instant, PeriodAccumulator> byPeriod = KPeriodAggregator.aggregate(processed, kPeriods);
        List<PeriodAccumulator> list = KPeriodAggregator.toOrderedList(byPeriod, kPeriods);

        assertEquals(BigDecimal.ZERO, list.get(0).totalContribution());
        assertEquals(BigDecimal.ZERO, list.get(1).totalContribution());
        assertEquals(new BigDecimal("50"), list.get(2).totalContribution());
    }

    @Test
    @DisplayName("shouldHandleEmptyProcessed")
    void shouldHandleEmptyProcessed() {
        List<KPeriod> kPeriods = threePeriods();
        NavigableMap<Instant, PeriodAccumulator> byPeriod = KPeriodAggregator.aggregate(List.of(), kPeriods);
        List<PeriodAccumulator> list = KPeriodAggregator.toOrderedList(byPeriod, kPeriods);

        assertEquals(3, list.size());
        list.forEach(acc -> {
            assertEquals(BigDecimal.ZERO, acc.totalContribution());
            assertEquals(BigDecimal.ZERO, acc.totalRemanent());
        });
    }

    @Test
    @DisplayName("withPeriodPAdditiveShouldSetPFromTimeline")
    void withPeriodPAdditiveShouldSetPFromTimeline() {
        List<KPeriod> kPeriods = threePeriods();
        List<PeriodAccumulator> accumulators = List.of(
                PeriodAccumulator.empty("K1"),
                PeriodAccumulator.empty("K2").add(BigDecimal.ZERO, BigDecimal.ZERO),
                PeriodAccumulator.empty("K3")
        );
        NavigableMap<Instant, BigDecimal> pRunning = new java.util.TreeMap<>();
        pRunning.put(instant("2024-01-01 00:00:00"), new BigDecimal("0"));
        pRunning.put(instant("2024-01-15 00:00:00"), new BigDecimal("100"));
        pRunning.put(instant("2024-02-01 00:00:00"), new BigDecimal("100"));
        pRunning.put(instant("2024-02-15 00:00:00"), new BigDecimal("150"));
        pRunning.put(instant("2024-03-01 00:00:00"), new BigDecimal("150"));
        pRunning.put(instant("2024-03-15 00:00:00"), new BigDecimal("200"));

        List<PeriodAccumulator> result = KPeriodAggregator.withPeriodPAdditive(accumulators, kPeriods, pRunning);

        // K1: atEnd(lowerEntry 2024-02-01) - atStart(2024-01-01) = 100 - 0 = 100
        assertEquals(new BigDecimal("100"), result.get(0).pAdditive());
        // K2: 150 - 100 = 50
        assertEquals(new BigDecimal("50"), result.get(1).pAdditive());
        // K3: 200 - 150 = 50
        assertEquals(new BigDecimal("50"), result.get(2).pAdditive());
    }

    @Test
    @DisplayName("withPeriodPAdditiveShouldReturnSameWhenMapNull")
    void withPeriodPAdditiveShouldReturnSameWhenMapNull() {
        List<PeriodAccumulator> acc = List.of(PeriodAccumulator.empty("K1"));
        List<PeriodAccumulator> result = KPeriodAggregator.withPeriodPAdditive(acc, threePeriods(), null);
        assertSame(acc, result);
    }
}
