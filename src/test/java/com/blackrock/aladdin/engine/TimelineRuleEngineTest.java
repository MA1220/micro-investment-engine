package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.PRule;
import com.blackrock.aladdin.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for p (extra/additive) rules timeline and cumulative sum.
 */
@DisplayName("TimelineRuleEngine Tests")
class TimelineRuleEngineTest {

    private static Instant instant(String s) {
        return Instant.parse(s.replace(" ", "T") + "Z");
    }

    @Test
    @DisplayName("shouldApplyExtraRuleCorrectly")
    void shouldApplyExtraRuleCorrectly() {
        // Arrange: one p rule from 2024-01-01, delta 100
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-01-15 09:30:00"), BigDecimal.ONE)
        );
        List<PRule> pRules = List.of(
                new PRule("p1", instant("2024-01-01 00:00:00"), new BigDecimal("100"))
        );

        // Act
        NavigableMap<Instant, BigDecimal> running = TimelineRuleEngine.buildPRunningByTime(txns, pRules);

        // Assert: at txn time, cumulative p should be 100
        var entry = running.floorEntry(instant("2024-01-15 09:30:00"));
        assertNotNull(entry);
        assertEquals(new BigDecimal("100"), entry.getValue());
    }

    @Test
    @DisplayName("shouldCumulateMultiplePRules")
    void shouldCumulateMultiplePRules() {
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-01-10 00:00:00"), BigDecimal.ONE),
                new Transaction("tx2", instant("2024-03-15 00:00:00"), BigDecimal.ONE)
        );
        List<PRule> pRules = List.of(
                new PRule("p1", instant("2024-01-01 00:00:00"), new BigDecimal("50")),
                new PRule("p2", instant("2024-03-01 00:00:00"), new BigDecimal("30"))
        );

        NavigableMap<Instant, BigDecimal> running = TimelineRuleEngine.buildPRunningByTime(txns, pRules);

        // Before 2024-03-01: only p1
        assertEquals(new BigDecimal("50"), running.floorEntry(instant("2024-01-10 00:00:00")).getValue());
        // After 2024-03-01: p1 + p2
        assertEquals(new BigDecimal("80"), running.floorEntry(instant("2024-03-15 00:00:00")).getValue());
    }

    @Test
    @DisplayName("shouldHandleEmptyPRules")
    void shouldHandleEmptyPRules() {
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-01-01 00:00:00"), BigDecimal.ONE)
        );
        NavigableMap<Instant, BigDecimal> running = TimelineRuleEngine.buildPRunningByTime(txns, List.of());
        assertTrue(running.isEmpty() || running.get(running.firstKey()).compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    @DisplayName("shouldHandleNullPRules")
    void shouldHandleNullPRules() {
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-01-01 00:00:00"), BigDecimal.ONE)
        );
        NavigableMap<Instant, BigDecimal> running = TimelineRuleEngine.buildPRunningByTime(txns, null);
        assertNotNull(running);
    }

    @Test
    @DisplayName("shouldHandleOverlappingPRulesAtSameTime")
    void shouldHandleOverlappingPRulesAtSameTime() {
        Instant t0 = instant("2024-02-01 00:00:00");
        List<Transaction> txns = List.of(new Transaction("tx1", t0, BigDecimal.ONE));
        List<PRule> pRules = List.of(
                new PRule("p1", t0, new BigDecimal("10")),
                new PRule("p2", t0, new BigDecimal("20"))
        );

        NavigableMap<Instant, BigDecimal> running = TimelineRuleEngine.buildPRunningByTime(txns, pRules);
        assertEquals(new BigDecimal("30"), running.get(t0));
    }

    @Test
    @DisplayName("boundaryTimestampTransactionExactlyAtPRuleStart")
    void boundaryTimestampTransactionExactlyAtPRuleStart() {
        Instant t = instant("2024-06-01 12:00:00");
        List<Transaction> txns = List.of(new Transaction("tx1", t, BigDecimal.ONE));
        List<PRule> pRules = List.of(new PRule("p1", t, new BigDecimal("100")));

        NavigableMap<Instant, BigDecimal> running = TimelineRuleEngine.buildPRunningByTime(txns, pRules);
        assertEquals(new BigDecimal("100"), running.get(t));
    }

    @Test
    @DisplayName("shouldHandleEmptyTransactionsWithPRules")
    void shouldHandleEmptyTransactionsWithPRules() {
        List<PRule> pRules = List.of(
                new PRule("p1", instant("2024-01-01 00:00:00"), new BigDecimal("50"))
        );
        NavigableMap<Instant, BigDecimal> running = TimelineRuleEngine.buildPRunningByTime(List.of(), pRules);
        assertNotNull(running);
        assertFalse(running.isEmpty());
        assertEquals(new BigDecimal("50"), running.get(instant("2024-01-01 00:00:00")));
    }
}
