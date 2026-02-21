package com.blackrock.aladdin.model;

import java.math.BigDecimal;

public record ReturnResultDto(
        String periodId,
        BigDecimal grossReturn,
        BigDecimal taxBenefit,
        BigDecimal inflationAdjustedReturn
) {}
