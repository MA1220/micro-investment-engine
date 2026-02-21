package com.blackrock.aladdin.controller;

import com.blackrock.aladdin.model.*;
import com.blackrock.aladdin.service.InvestmentEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests: endpoint contract and validation.
 */
@WebMvcTest(ProcessController.class)
@Import(EngineExceptionHandler.class)
@DisplayName("ProcessController Tests")
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvestmentEngineService engineService;

    private static ProcessRequest validRequest() {
        return new ProcessRequest(
                List.of(
                        new TransactionDto("tx1", "2024-01-15 09:30:00", new BigDecimal("1000"))
                ),
                List.of(),
                List.of(),
                List.of(
                        new KPeriodDto("K1", "2024-01-01 00:00:00", "2024-02-01 00:00:00")
                ),
                new BigDecimal("2000"),
                new BigDecimal("0.02"),
                new BigDecimal("500")
        );
    }

    @Test
    @DisplayName("shouldReturn200AndJsonWhenRequestValid")
    void shouldReturn200AndJsonWhenRequestValid() throws Exception {
        ProcessRequest request = validRequest();
        ProcessResponse stubResponse = new ProcessResponse(
                List.of(new PeriodSummaryDto("K1", "2024-01-01 00:00:00", "2024-02-01 00:00:00", new BigDecimal("500"), BigDecimal.ZERO, BigDecimal.ZERO)),
                List.of(new ReturnResultDto("K1", new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("500"))),
                List.of(new ReturnResultDto("K1", new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("500")))
        );
        when(engineService.process(any(ProcessRequest.class))).thenReturn(stubResponse);

        ResultActions result = mockMvc.perform(post("/api/v1/engine/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.periodSummaries").isArray())
                .andExpect(jsonPath("$.npsReturns").isArray())
                .andExpect(jsonPath("$.indexReturns").isArray());
    }

    @Test
    @DisplayName("shouldReturn400WhenTransactionsNull")
    void shouldReturn400WhenTransactionsNull() throws Exception {
        String body = "{\"transactions\":null,\"qRules\":[],\"pRules\":[],\"kPeriods\":[{\"id\":\"K1\",\"startInclusive\":\"2024-01-01 00:00:00\",\"endExclusive\":\"2024-02-01 00:00:00\"}],\"defaultCeiling\":2000,\"inflationRate\":0.02,\"taxCap\":500}";

        mockMvc.perform(post("/api/v1/engine/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn400WhenTimestampInvalid")
    void shouldReturn400WhenTimestampInvalid() throws Exception {
        ProcessRequest request = new ProcessRequest(
                List.of(new TransactionDto("tx1", "invalid-date", new BigDecimal("1000"))),
                List.of(),
                List.of(),
                List.of(new KPeriodDto("K1", "2024-01-01 00:00:00", "2024-02-01 00:00:00")),
                new BigDecimal("2000"),
                new BigDecimal("0.02"),
                new BigDecimal("500")
        );
        when(engineService.process(any(ProcessRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid timestamp: invalid-date"));

        mockMvc.perform(post("/api/v1/engine/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
