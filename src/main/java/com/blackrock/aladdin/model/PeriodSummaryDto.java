package com.blackrock.aladdin.model;

import java.math.BigDecimal;

public record PeriodSummaryDto(
        String periodId,
        String startInclusive,
        String endExclusive,
        BigDecimal totalContribution,
        BigDecimal totalRemanent,
        BigDecimal pAdditive
) {}
