package com.blackrock.aladdin.service;

import com.blackrock.aladdin.engine.ReturnsEngine;
import com.blackrock.aladdin.model.*;
import com.blackrock.aladdin.util.TimestampParser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps domain results to API response DTOs.
 */
public final class ResponseMapper {

    private ResponseMapper() {}

    public static PeriodSummaryDto toPeriodSummaryDto(PeriodAccumulator acc, KPeriod period) {
        String start = period != null ? TimestampParser.format(period.startInclusive()) : "";
        String end = period != null ? TimestampParser.format(period.endExclusive()) : "";
        return new PeriodSummaryDto(
                acc.periodId(),
                start,
                end,
                acc.totalContribution(),
                acc.totalRemanent(),
                acc.pAdditive()
        );
    }

    public static ReturnResultDto toReturnResultDto(ReturnsEngine.ReturnResult r) {
        return new ReturnResultDto(
                r.periodId(),
                r.grossReturn(),
                r.taxBenefit(),
                r.inflationAdjustedReturn()
        );
    }

    public static List<PeriodSummaryDto> toPeriodSummaryDtos(
            List<PeriodAccumulator> accumulators,
            List<KPeriod> kPeriods
    ) {
        var byId = kPeriods.stream().collect(Collectors.toMap(KPeriod::id, p -> p));
        return accumulators.stream()
                .map(acc -> toPeriodSummaryDto(acc, byId.get(acc.periodId())))
                .collect(Collectors.toList());
    }
}
