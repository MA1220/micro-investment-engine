package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.PRule;
import com.blackrock.aladdin.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeSet;
import java.util.TreeMap;

/**
 * Builds timeline of p deltas and computes running sum at each distinct time.
 * Sweep-line: sort all events (txn times + p start times), then one pass.
 * Result: NavigableMap<Instant, runningP> for O(log n) lookup of p at any t.
 */
public final class TimelineRuleEngine {

    private TimelineRuleEngine() {}

    /**
     * Returns a map: time -> cumulative p additive up to (and including) that time.
     * Used by CeilingRemanentEngine to attach p at each transaction time.
     */
    public static NavigableMap<Instant, BigDecimal> buildPRunningByTime(
            List<Transaction> transactions,
            List<PRule> pRules
    ) {
        if (pRules == null || pRules.isEmpty()) {
            NavigableMap<Instant, BigDecimal> empty = new TreeMap<>();
            if (!transactions.isEmpty()) {
                empty.put(Instant.EPOCH, BigDecimal.ZERO);
            }
            return empty;
        }

        TreeSet<Instant> events = new TreeSet<>();
        for (Transaction t : transactions) {
            events.add(t.timestamp());
        }
        for (PRule p : pRules) {
            events.add(p.startTime());
        }

        TreeMap<Instant, BigDecimal> deltaAtTime = new TreeMap<>();
        for (PRule p : pRules) {
            deltaAtTime.merge(p.startTime(), p.delta(), BigDecimal::add);
        }

        NavigableMap<Instant, BigDecimal> running = new TreeMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (Instant t : new ArrayList<>(events)) {
            sum = sum.add(deltaAtTime.getOrDefault(t, BigDecimal.ZERO));
            running.put(t, sum);
        }
        return running;
    }
}
