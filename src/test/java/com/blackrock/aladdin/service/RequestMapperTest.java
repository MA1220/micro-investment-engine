package com.blackrock.aladdin.service;

import com.blackrock.aladdin.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for transaction and rule parsing (DTO to domain).
 */
@DisplayName("RequestMapper Tests")
class RequestMapperTest {

    @Test
    @DisplayName("shouldMapTransactionsCorrectly")
    void shouldMapTransactionsCorrectly() {
        // Arrange
        var dtos = List.of(
                new TransactionDto("tx1", "2024-01-15 09:30:00", java.math.BigDecimal.valueOf(1000))
        );

        // Act
        List<Transaction> result = RequestMapper.toTransactions(dtos);

        // Assert
        assertEquals(1, result.size());
        assertEquals("tx1", result.get(0).id());
        assertEquals(java.math.BigDecimal.valueOf(1000), result.get(0).amount());
        assertNotNull(result.get(0).timestamp());
    }

    @Test
    @DisplayName("shouldMapQRulesCorrectly")
    void shouldMapQRulesCorrectly() {
        var dtos = List.of(
                new QRuleDto("q1", "2024-02-01 00:00:00", java.math.BigDecimal.valueOf(1500))
        );
        List<QRule> result = RequestMapper.toQRules(dtos);
        assertEquals(1, result.size());
        assertEquals("q1", result.get(0).id());
        assertEquals(java.math.BigDecimal.valueOf(1500), result.get(0).ceilingOverride());
    }

    @Test
    @DisplayName("shouldMapPRulesCorrectly")
    void shouldMapPRulesCorrectly() {
        var dtos = List.of(
                new PRuleDto("p1", "2024-01-01 00:00:00", java.math.BigDecimal.valueOf(100))
        );
        List<PRule> result = RequestMapper.toPRules(dtos);
        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).id());
        assertEquals(java.math.BigDecimal.valueOf(100), result.get(0).delta());
    }

    @Test
    @DisplayName("shouldMapKPeriodsCorrectly")
    void shouldMapKPeriodsCorrectly() {
        var dtos = List.of(
                new KPeriodDto("K1", "2024-01-01 00:00:00", "2024-02-01 00:00:00")
        );
        List<KPeriod> result = RequestMapper.toKPeriods(dtos);
        assertEquals(1, result.size());
        assertEquals("K1", result.get(0).id());
        assertTrue(result.get(0).startInclusive().isBefore(result.get(0).endExclusive()));
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenQRulesNull")
    void shouldReturnEmptyListWhenQRulesNull() {
        assertTrue(RequestMapper.toQRules(null).isEmpty());
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenPRulesNull")
    void shouldReturnEmptyListWhenPRulesNull() {
        assertTrue(RequestMapper.toPRules(null).isEmpty());
    }

    @Test
    @DisplayName("shouldThrowWhenTransactionTimestampInvalid")
    void shouldThrowWhenTransactionTimestampInvalid() {
        var dtos = List.of(new TransactionDto("tx1", "invalid-date", java.math.BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> RequestMapper.toTransactions(dtos));
    }
}
