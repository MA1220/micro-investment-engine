package com.blackrock.aladdin.service;

import com.blackrock.aladdin.engine.CeilingRemanentEngine;
import com.blackrock.aladdin.engine.KPeriodAggregator;
import com.blackrock.aladdin.engine.ReturnsEngine;
import com.blackrock.aladdin.engine.TimelineRuleEngine;
import com.blackrock.aladdin.model.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;

/**
 * Orchestrates the five-step pipeline. No business logic; delegates to engines.
 */
@Service
public class InvestmentEngineService {

    public ProcessResponse process(ProcessRequest request) {
        List<Transaction> transactions = RequestMapper.toTransactions(request.transactions());
        List<QRule> qRules = RequestMapper.toQRules(request.qRules());
        List<PRule> pRules = RequestMapper.toPRules(request.pRules());
        List<KPeriod> kPeriods = RequestMapper.toKPeriods(request.kPeriods());

        transactions = transactions.stream()
                .sorted(Comparator.comparing(Transaction::timestamp))
                .toList();

        List<KPeriod> sortedKPeriods = kPeriods.stream()
                .sorted(Comparator.comparing(KPeriod::startInclusive))
                .toList();

        NavigableMap<java.time.Instant, java.math.BigDecimal> pRunning = TimelineRuleEngine.buildPRunningByTime(transactions, pRules);

        List<ProcessedTransaction> processed = CeilingRemanentEngine.apply(
                transactions,
                qRules,
                request.defaultCeiling(),
                pRunning
        );

        NavigableMap<java.time.Instant, PeriodAccumulator> byPeriod = KPeriodAggregator.aggregate(processed, sortedKPeriods);
        List<PeriodAccumulator> accumulators = KPeriodAggregator.toOrderedList(byPeriod, sortedKPeriods);
        accumulators = KPeriodAggregator.withPeriodPAdditive(accumulators, sortedKPeriods, pRunning);

        List<ReturnsEngine.ReturnResult> nps = ReturnsEngine.npsReturns(accumulators, request.inflationRate(), request.taxCap());
        List<ReturnsEngine.ReturnResult> index = ReturnsEngine.indexReturns(accumulators, request.inflationRate());

        List<PeriodSummaryDto> periodSummaries = ResponseMapper.toPeriodSummaryDtos(accumulators, sortedKPeriods);
        List<ReturnResultDto> npsReturns = nps.stream().map(ResponseMapper::toReturnResultDto).toList();
        List<ReturnResultDto> indexReturns = index.stream().map(ResponseMapper::toReturnResultDto).toList();

        return new ProcessResponse(periodSummaries, npsReturns, indexReturns);
    }
}
