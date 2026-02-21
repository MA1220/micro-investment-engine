package com.blackrock.aladdin.service;

import com.blackrock.aladdin.model.*;
import com.blackrock.aladdin.util.TimestampParser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps API DTOs to domain models. Single responsibility; deterministic.
 */
public final class RequestMapper {

    private RequestMapper() {}

    public static List<Transaction> toTransactions(List<TransactionDto> dtos) {
        return dtos.stream()
                .map(d -> new Transaction(
                        d.id(),
                        TimestampParser.parse(d.timestamp()),
                        d.amount()
                ))
                .collect(Collectors.toList());
    }

    public static List<QRule> toQRules(List<QRuleDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(d -> new QRule(
                        d.id(),
                        TimestampParser.parse(d.startTime()),
                        d.ceilingOverride()
                ))
                .collect(Collectors.toList());
    }

    public static List<PRule> toPRules(List<PRuleDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(d -> new PRule(
                        d.id(),
                        TimestampParser.parse(d.startTime()),
                        d.delta()
                ))
                .collect(Collectors.toList());
    }

    public static List<KPeriod> toKPeriods(List<KPeriodDto> dtos) {
        return dtos.stream()
                .map(d -> new KPeriod(
                        d.id(),
                        TimestampParser.parse(d.startInclusive()),
                        TimestampParser.parse(d.endExclusive())
                ))
                .collect(Collectors.toList());
    }
}
