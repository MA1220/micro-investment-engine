package com.blackrock.aladdin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO: "timestamp" as "yyyy-MM-dd HH:mm:ss".
 */
public record TransactionDto(
        @NotBlank String id,
        @NotBlank String timestamp,
        @NotNull BigDecimal amount
) {}
