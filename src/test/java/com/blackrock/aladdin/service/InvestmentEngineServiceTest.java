package com.blackrock.aladdin.service;

import com.blackrock.aladdin.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service layer tests: full pipeline Controller → Service → Engine → Response.
 */
@SpringBootTest
@DisplayName("InvestmentEngineService Tests")
class InvestmentEngineServiceTest {

    @Autowired
    private InvestmentEngineService engineService;

    @Test
    @DisplayName("shouldProcessFullPipelineCorrectly")
    void shouldProcessFullPipelineCorrectly() {
        ProcessRequest request = new ProcessRequest(
                List.of(
                        new TransactionDto("tx1", "2024-01-15 09:30:00", new BigDecimal("1000")),
                        new TransactionDto("tx2", "2024-02-20 10:00:00", new BigDecimal("500")),
                        new TransactionDto("tx3", "2024-03-10 14:00:00", new BigDecimal("2000"))
                ),
                List.of(new QRuleDto("q1", "2024-02-01 00:00:00", new BigDecimal("1500"))),
                List.of(
                        new PRuleDto("p1", "2024-01-01 00:00:00", new BigDecimal("100")),
                        new PRuleDto("p2", "2024-03-01 00:00:00", new BigDecimal("50"))
                ),
                List.of(
                        new KPeriodDto("K1", "2024-01-01 00:00:00", "2024-02-01 00:00:00"),
                        new KPeriodDto("K2", "2024-02-01 00:00:00", "2024-03-01 00:00:00"),
                        new KPeriodDto("K3", "2024-03-01 00:00:00", "2024-04-01 00:00:00")
                ),
                new BigDecimal("2000"),
                new BigDecimal("0.02"),
                new BigDecimal("500")
        );

        ProcessResponse response = engineService.process(request);

        assertNotNull(response);
        assertEquals(3, response.periodSummaries().size());
        assertEquals(3, response.npsReturns().size());
        assertEquals(3, response.indexReturns().size());

        assertEquals("K1", response.periodSummaries().get(0).periodId());
        assertEquals(new BigDecimal("1000"), response.periodSummaries().get(0).totalContribution());
        assertEquals("K2", response.periodSummaries().get(1).periodId());
        assertEquals(new BigDecimal("500"), response.periodSummaries().get(1).totalContribution());
        assertEquals("K3", response.periodSummaries().get(2).periodId());
        assertEquals(new BigDecimal("1500"), response.periodSummaries().get(2).totalContribution());
        assertEquals(new BigDecimal("500"), response.periodSummaries().get(2).totalRemanent());
    }

    @Test
    @DisplayName("shouldHandleEmptyTransactions")
    void shouldHandleEmptyTransactions() {
        ProcessRequest request = new ProcessRequest(
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        new KPeriodDto("K1", "2024-01-01 00:00:00", "2024-02-01 00:00:00")
                ),
                new BigDecimal("2000"),
                new BigDecimal("0"),
                new BigDecimal("500")
        );

        ProcessResponse response = engineService.process(request);

        assertNotNull(response);
        assertEquals(1, response.periodSummaries().size());
        assertEquals(BigDecimal.ZERO, response.periodSummaries().get(0).totalContribution());
    }

    @Test
    @DisplayName("shouldHandleSingleTransaction")
    void shouldHandleSingleTransaction() {
        ProcessRequest request = new ProcessRequest(
                List.of(new TransactionDto("tx1", "2024-01-15 09:30:00", new BigDecimal("500"))),
                List.of(),
                List.of(),
                List.of(new KPeriodDto("K1", "2024-01-01 00:00:00", "2024-02-01 00:00:00")),
                new BigDecimal("1000"),
                new BigDecimal("0"),
                new BigDecimal("100")
        );

        ProcessResponse response = engineService.process(request);

        assertEquals(1, response.periodSummaries().size());
        assertEquals(new BigDecimal("500"), response.periodSummaries().get(0).totalContribution());
        assertEquals(new BigDecimal("500"), response.npsReturns().get(0).grossReturn());
    }
}
