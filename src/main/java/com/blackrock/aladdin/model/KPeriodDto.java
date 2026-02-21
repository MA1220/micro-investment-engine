package com.blackrock.aladdin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KPeriodDto(
        @NotBlank String id,
        @NotBlank String startInclusive,
        @NotBlank String endExclusive
) {}
