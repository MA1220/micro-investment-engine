package com.blackrock.aladdin.engine;

import com.blackrock.aladdin.model.ProcessedTransaction;
import com.blackrock.aladdin.model.QRule;
import com.blackrock.aladdin.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Step 1: Apply ceiling; compute allowed and remanent.
 * Step 2: q rules override ceiling (latest start wins) via TreeMap floorEntry(t).
 * Single pass over sorted transactions; O(n log q) for q lookups.
 */
public final class CeilingRemanentEngine {

    private CeilingRemanentEngine() {}

    /**
     * @param transactions sorted by timestamp
     * @param qRules       override ceiling by start time (latest start wins)
     * @param defaultCeiling default when no q applies
     */
    public static List<ProcessedTransaction> apply(
            List<Transaction> transactions,
            List<QRule> qRules,
            BigDecimal defaultCeiling,
            NavigableMap<java.time.Instant, BigDecimal> pRunningByTime
    ) {
        NavigableMap<java.time.Instant, BigDecimal> ceilingByStart = new TreeMap<>();
        for (QRule q : qRules) {
            ceilingByStart.put(q.startTime(), q.ceilingOverride());
        }

        return transactions.stream()
                .map(txn -> {
                    BigDecimal ceiling = defaultCeiling;
                    var floor = ceilingByStart.floorEntry(txn.timestamp());
                    if (floor != null) {
                        ceiling = floor.getValue();
                    }
                    BigDecimal allowed = txn.amount().min(ceiling);
                    BigDecimal remanent = txn.amount().subtract(allowed).max(BigDecimal.ZERO);
                    BigDecimal pAtTime = pRunningByTime.floorEntry(txn.timestamp()) != null
                            ? pRunningByTime.floorEntry(txn.timestamp()).getValue()
                            : BigDecimal.ZERO;
                    return new ProcessedTransaction(
                            txn.id(),
                            txn.timestamp(),
                            txn.amount(),
                            allowed,
                            remanent,
                            pAtTime
                    );
                })
                .collect(Collectors.toList());
    }
}
