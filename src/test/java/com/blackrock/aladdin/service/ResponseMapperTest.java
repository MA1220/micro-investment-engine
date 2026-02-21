package com.blackrock.aladdin.service;

import com.blackrock.aladdin.engine.ReturnsEngine;
import com.blackrock.aladdin.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for response DTO mapping.
 */
@DisplayName("ResponseMapper Tests")
class ResponseMapperTest {

    @Test
    @DisplayName("shouldMapPeriodSummaryCorrectly")
    void shouldMapPeriodSummaryCorrectly() {
        var acc = new PeriodAccumulator("K1", new BigDecimal("1000"), new BigDecimal("50"), new BigDecimal("100"));
        var period = new KPeriod("K1",
                java.time.Instant.parse("2024-01-01T00:00:00Z"),
                java.time.Instant.parse("2024-02-01T00:00:00Z"));

        PeriodSummaryDto dto = ResponseMapper.toPeriodSummaryDto(acc, period);

        assertEquals("K1", dto.periodId());
        assertEquals("2024-01-01 00:00:00", dto.startInclusive());
        assertEquals("2024-02-01 00:00:00", dto.endExclusive());
        assertEquals(new BigDecimal("1000"), dto.totalContribution());
        assertEquals(new BigDecimal("50"), dto.totalRemanent());
        assertEquals(new BigDecimal("100"), dto.pAdditive());
    }

    @Test
    @DisplayName("shouldMapReturnResultCorrectly")
    void shouldMapReturnResultCorrectly() {
        var r = new ReturnsEngine.ReturnResult("K1", new BigDecimal("1100"), new BigDecimal("500"), new BigDecimal("1078.43"));

        ReturnResultDto dto = ResponseMapper.toReturnResultDto(r);

        assertEquals("K1", dto.periodId());
        assertEquals(new BigDecimal("1100"), dto.grossReturn());
        assertEquals(new BigDecimal("500"), dto.taxBenefit());
        assertEquals(new BigDecimal("1078.43"), dto.inflationAdjustedReturn());
    }

    @Test
    @DisplayName("shouldHandleNullPeriodInPeriodSummary")
    void shouldHandleNullPeriodInPeriodSummary() {
        var acc = new PeriodAccumulator("K1", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PeriodSummaryDto dto = ResponseMapper.toPeriodSummaryDto(acc, null);
        assertEquals("K1", dto.periodId());
        assertEquals("", dto.startInclusive());
        assertEquals("", dto.endExclusive());
    }
}
