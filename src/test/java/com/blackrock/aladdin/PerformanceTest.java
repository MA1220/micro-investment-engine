package com.blackrock.aladdin;

import com.blackrock.aladdin.model.*;
import com.blackrock.aladdin.service.InvestmentEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance validation: large dataset completes quickly (e.g. 100,000 transactions).
 */
@SpringBootTest
@DisplayName("Performance Tests")
class PerformanceTest {

    @Autowired
    private InvestmentEngineService engineService;

    @Test
    @DisplayName("shouldHandleLargeDatasetEfficiently")
    void shouldHandleLargeDatasetEfficiently() {
        int n = 100_000;
        List<TransactionDto> transactions = new ArrayList<>(n);
        LocalDate start = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < n; i++) {
            String datePart = start.plusDays(i % 365).toString();
            transactions.add(new TransactionDto("tx" + i, datePart + " 09:30:00", new BigDecimal("100")));
        }

        List<KPeriodDto> kPeriods = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String startStr = String.format("2024-%02d-01 00:00:00", m);
            String endStr = m == 12 ? "2025-01-01 00:00:00" : String.format("2024-%02d-01 00:00:00", m + 1);
            kPeriods.add(new KPeriodDto("K" + m, startStr, endStr));
        }

        ProcessRequest request = new ProcessRequest(
                transactions,
                List.of(),
                List.of(),
                kPeriods,
                new BigDecimal("2000"),
                new BigDecimal("0.02"),
                new BigDecimal("500")
        );

        long startMs = System.currentTimeMillis();
        ProcessResponse response = engineService.process(request);
        long elapsedMs = System.currentTimeMillis() - startMs;

        assertNotNull(response);
        assertEquals(12, response.periodSummaries().size());
        assertTrue(elapsedMs < 30_000, "Large dataset should complete within 30 seconds, took " + elapsedMs + " ms");
    }
}
