package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.ProcessedTransaction;
import com.blackrock.aladdin.model.QRule;
import com.blackrock.aladdin.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ceiling, remanent, and q (fixed) rule application.
 */
@DisplayName("CeilingRemanentEngine Tests")
class CeilingRemanentEngineTest {

    private static Instant instant(String s) {
        return Instant.parse(s.replace(" ", "T") + "Z");
    }

    @Test
    @DisplayName("shouldCalculateRemanentCorrectly")
    void shouldCalculateRemanentCorrectly() {
        // Arrange: amount exceeds ceiling
        BigDecimal defaultCeiling = new BigDecimal("2000");
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-01-15 09:30:00"), new BigDecimal("3000"))
        );
        NavigableMap<Instant, BigDecimal> pRunning = new TreeMap<>();
        pRunning.put(Instant.EPOCH, BigDecimal.ZERO);

        // Act
        List<ProcessedTransaction> result = CeilingRemanentEngine.apply(txns, List.of(), defaultCeiling, pRunning);

        // Assert
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("2000"), result.get(0).allowed());
        assertEquals(new BigDecimal("1000"), result.get(0).remanent());
    }

    @Test
    @DisplayName("shouldApplyFixedRuleCorrectly")
    void shouldApplyFixedRuleCorrectly() {
        // Arrange: q rule overrides ceiling to 1500 from 2024-02-01
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-01-15 09:30:00"), new BigDecimal("2000")), // before q -> default 2000
                new Transaction("tx2", instant("2024-02-15 10:00:00"), new BigDecimal("2000"))   // after q -> 1500
        );
        List<QRule> qRules = List.of(
                new QRule("q1", instant("2024-02-01 00:00:00"), new BigDecimal("1500"))
        );
        NavigableMap<Instant, BigDecimal> pRunning = new TreeMap<>();
        pRunning.put(Instant.EPOCH, BigDecimal.ZERO);

        // Act
        List<ProcessedTransaction> result = CeilingRemanentEngine.apply(txns, qRules, new BigDecimal("2000"), pRunning);

        // Assert: latest start wins; first txn uses default, second uses 1500
        assertEquals(new BigDecimal("2000"), result.get(0).allowed());
        assertEquals(BigDecimal.ZERO, result.get(0).remanent());
        assertEquals(new BigDecimal("1500"), result.get(1).allowed());
        assertEquals(new BigDecimal("500"), result.get(1).remanent());
    }

    @Test
    @DisplayName("shouldUseDefaultCeilingWhenNoQRuleMatches")
    void shouldUseDefaultCeilingWhenNoQRuleMatches() {
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2023-06-01 00:00:00"), new BigDecimal("1000"))
        );
        List<QRule> qRules = List.of(
                new QRule("q1", instant("2024-01-01 00:00:00"), new BigDecimal("500"))
        );
        NavigableMap<Instant, BigDecimal> pRunning = new TreeMap<>();
        pRunning.put(Instant.EPOCH, BigDecimal.ZERO);

        List<ProcessedTransaction> result = CeilingRemanentEngine.apply(txns, qRules, new BigDecimal("2000"), pRunning);

        assertEquals(new BigDecimal("1000"), result.get(0).allowed());
        assertEquals(BigDecimal.ZERO, result.get(0).remanent());
    }

    @Test
    @DisplayName("shouldHandleOverlappingQRulesLatestStartWins")
    void shouldHandleOverlappingQRulesLatestStartWins() {
        // Two q rules; transaction time is after both -> floorEntry gives latest start <= t
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-03-15 00:00:00"), new BigDecimal("2000"))
        );
        List<QRule> qRules = List.of(
                new QRule("q1", instant("2024-01-01 00:00:00"), new BigDecimal("1000")),
                new QRule("q2", instant("2024-03-01 00:00:00"), new BigDecimal("2500"))
        );
        NavigableMap<Instant, BigDecimal> pRunning = new TreeMap<>();
        pRunning.put(Instant.EPOCH, BigDecimal.ZERO);

        List<ProcessedTransaction> result = CeilingRemanentEngine.apply(txns, qRules, new BigDecimal("2000"), pRunning);

        // Latest start <= 2024-03-15 is 2024-03-01 -> ceiling 2500
        assertEquals(new BigDecimal("2000"), result.get(0).allowed());
        assertEquals(BigDecimal.ZERO, result.get(0).remanent());
    }

    @Test
    @DisplayName("shouldHandleEmptyTransactions")
    void shouldHandleEmptyTransactions() {
        NavigableMap<Instant, BigDecimal> pRunning = new TreeMap<>();
        List<ProcessedTransaction> result = CeilingRemanentEngine.apply(List.of(), List.of(), BigDecimal.ONE, pRunning);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("shouldHandleSingleTransaction")
    void shouldHandleSingleTransaction() {
        List<Transaction> txns = List.of(
                new Transaction("tx1", instant("2024-01-01 12:00:00"), new BigDecimal("500"))
        );
        NavigableMap<Instant, BigDecimal> pRunning = new TreeMap<>();
        pRunning.put(Instant.EPOCH, BigDecimal.ZERO);

        List<ProcessedTransaction> result = CeilingRemanentEngine.apply(txns, List.of(), new BigDecimal("1000"), pRunning);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("500"), result.get(0).allowed());
        assertEquals(BigDecimal.ZERO, result.get(0).remanent());
    }

    @Test
    @DisplayName("boundaryTimestampExactlyOnQRuleStart")
    void boundaryTimestampExactlyOnQRuleStart() {
        Instant boundary = instant("2024-02-01 00:00:00");
        List<Transaction> txns = List.of(new Transaction("tx1", boundary, new BigDecimal("1600")));
        List<QRule> qRules = List.of(new QRule("q1", boundary, new BigDecimal("1500")));
        NavigableMap<Instant, BigDecimal> pRunning = new TreeMap<>();
        pRunning.put(Instant.EPOCH, BigDecimal.ZERO);

        List<ProcessedTransaction> result = CeilingRemanentEngine.apply(txns, qRules, new BigDecimal("2000"), pRunning);

        assertEquals(new BigDecimal("1500"), result.get(0).allowed());
        assertEquals(new BigDecimal("100"), result.get(0).remanent());
    }
}
