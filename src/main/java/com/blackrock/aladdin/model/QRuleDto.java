package com.blackrock.aladdin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QRuleDto(
        @NotBlank String id,
        @NotBlank String startTime,
        @NotNull BigDecimal ceilingOverride
) {}
