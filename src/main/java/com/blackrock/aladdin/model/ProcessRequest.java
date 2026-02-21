package com.blackrock.aladdin.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * API request: transactions, rules, k periods, and global parameters.
 */
public record ProcessRequest(
        @NotNull @Valid List<TransactionDto> transactions,
        @Valid List<QRuleDto> qRules,
        @Valid List<PRuleDto> pRules,
        @NotNull @Valid List<KPeriodDto> kPeriods,
        @NotNull @DecimalMin("0") BigDecimal defaultCeiling,
        @NotNull @DecimalMin("0") BigDecimal inflationRate,
        @NotNull @DecimalMin("0") BigDecimal taxCap
) {
    public ProcessRequest {
        if (qRules == null) qRules = List.of();
        if (pRules == null) pRules = List.of();
    }
}
