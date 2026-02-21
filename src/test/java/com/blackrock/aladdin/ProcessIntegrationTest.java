package com.blackrock.aladdin;

import com.blackrock.aladdin.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: complete flow Controller → Service → Engine → Response.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Process Integration Tests")
class ProcessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("shouldCompleteFullFlowControllerToResponse")
    void shouldCompleteFullFlowControllerToResponse() throws Exception {
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

        ResultActions result = mockMvc.perform(post("/api/v1/engine/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.periodSummaries.length()").value(3))
                .andExpect(jsonPath("$.periodSummaries[0].periodId").value("K1"))
                .andExpect(jsonPath("$.periodSummaries[0].totalContribution").value(1000))
                .andExpect(jsonPath("$.periodSummaries[1].totalContribution").value(500))
                .andExpect(jsonPath("$.periodSummaries[2].totalContribution").value(1500))
                .andExpect(jsonPath("$.periodSummaries[2].totalRemanent").value(500))
                .andExpect(jsonPath("$.npsReturns.length()").value(3))
                .andExpect(jsonPath("$.indexReturns.length()").value(3));
    }
}
