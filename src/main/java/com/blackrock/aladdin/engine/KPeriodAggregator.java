package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.KPeriod;
import com.blackrock.aladdin.model.PeriodAccumulator;
import com.blackrock.aladdin.model.ProcessedTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Step 4: Group processed transactions by k period using floorKey(timestamp).
 * O(n log k) — no nested loops over periods.
 */
public final class KPeriodAggregator {

    private KPeriodAggregator() {}

    /**
     * @param processed sorted by timestamp
     * @param kPeriods  [startInclusive, endExclusive); must not overlap; sorted by start
     */
    public static NavigableMap<java.time.Instant, PeriodAccumulator> aggregate(
            List<ProcessedTransaction> processed,
            List<KPeriod> kPeriods
    ) {
        NavigableMap<java.time.Instant, PeriodAccumulator> byPeriodStart = new TreeMap<>();
        for (KPeriod k : kPeriods) {
            byPeriodStart.put(k.startInclusive(), PeriodAccumulator.empty(k.id()));
        }

        for (ProcessedTransaction pt : processed) {
            var floor = byPeriodStart.floorKey(pt.timestamp());
            if (floor == null) continue;
            KPeriod period = kPeriods.stream()
                    .filter(k -> k.startInclusive().equals(floor))
                    .findFirst()
                    .orElse(null);
            if (period == null || !pt.timestamp().isBefore(period.endExclusive())) continue;

            PeriodAccumulator acc = byPeriodStart.get(floor);
            byPeriodStart.put(floor, acc.add(pt.allowed(), pt.remanent()));
        }

        return byPeriodStart;
    }

    public static List<PeriodAccumulator> toOrderedList(
            NavigableMap<Instant, PeriodAccumulator> byPeriodStart,
            List<KPeriod> kPeriods
    ) {
        return kPeriods.stream()
                .map(k -> byPeriodStart.getOrDefault(k.startInclusive(), PeriodAccumulator.empty(k.id())))
                .collect(Collectors.toList());
    }

    /**
     * Set period-level p additive from timeline: cumulative p at end of period minus at start.
     */
    public static List<PeriodAccumulator> withPeriodPAdditive(
            List<PeriodAccumulator> accumulators,
            List<KPeriod> kPeriods,
            NavigableMap<Instant, BigDecimal> pRunningByTime
    ) {
        if (pRunningByTime == null || pRunningByTime.isEmpty()) {
            return accumulators;
        }
        return accumulators.stream()
                .map(acc -> {
                    KPeriod period = kPeriods.stream()
                            .filter(k -> k.id().equals(acc.periodId()))
                            .findFirst()
                            .orElse(null);
                    if (period == null) return acc;
                    BigDecimal atStart = pRunningByTime.floorEntry(period.startInclusive()) != null
                            ? pRunningByTime.floorEntry(period.startInclusive()).getValue()
                            : BigDecimal.ZERO;
                    BigDecimal atEnd = pRunningByTime.lowerEntry(period.endExclusive()) != null
                            ? pRunningByTime.lowerEntry(period.endExclusive()).getValue()
                            : BigDecimal.ZERO;
                    return acc.withPAdditive(atEnd.subtract(atStart));
                })
                .collect(Collectors.toList());
    }
}
