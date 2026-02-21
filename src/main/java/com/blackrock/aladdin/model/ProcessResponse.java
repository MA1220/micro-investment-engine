package com.blackrock.aladdin.model;

import java.util.List;

/**
 * API response: period summaries and returns (NPS + Index), inflation-adjusted.
 */
public record ProcessResponse(
        List<PeriodSummaryDto> periodSummaries,
        List<ReturnResultDto> npsReturns,
        List<ReturnResultDto> indexReturns
) {}
