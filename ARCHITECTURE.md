# Micro-Investment Engine — Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT / API CONSUMER                             │
│                    POST /api/v1/engine/process  (port 5477)                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  PRESENTATION LAYER                                                         │
│  ┌─────────────────────────┐  ┌──────────────────────────────────────────┐  │
│  │  ProcessController      │  │  EngineExceptionHandler                  │  │
│  │  • Validate request     │  │  • Validation / IllegalArgumentException │  │
│  │  • Return JSON          │  │  • Log & return 4xx / 5xx                │  │
│  └─────────────────────────┘  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  SERVICE LAYER                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  InvestmentEngineService                                            │    │
│  │  • Map request (RequestMapper) → domain                             │    │
│  │  • Sort transactions & k-periods                                    │    │
│  │  • Orchestrate five-step pipeline → Map response (ResponseMapper)   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
          ┌─────────────────────────────┼─────────────────────────────┐
          ▼                             ▼                             ▼
┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
│  ENGINE LAYER       │   │  ENGINE LAYER       │   │  ENGINE LAYER       │
│  TimelineRuleEngine │   │  CeilingRemanent    │   │  KPeriodAggregator  │
│  • p running by time│   │  • ceiling/remanent │   │  • aggregate by     │
│  • sweep / TreeMap  │   │  • q override (q)   │   │    k period         │
└─────────────────────┘   └─────────────────────┘   └─────────────────────┘
          │                             │                             │
          └─────────────────────────────┼─────────────────────────────┘
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  ENGINE LAYER (downstream)                                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  ReturnsEngine                                                      │    │
│  │  • NPS returns (tax cap) · Index returns · Inflation adjustment     │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  MODEL / DATA                                                               │
│  Transaction · QRule · PRule · KPeriod · ProcessedTransaction ·             │
│  PeriodAccumulator · ProcessRequest / ProcessResponse (DTOs)                │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Processing Pipeline (Strict Order)

| Step | Component              | Responsibility                          |
|------|------------------------|-----------------------------------------|
| 1    | TimelineRuleEngine     | Build p additive timeline (sweep)       |
| 2    | CeilingRemanentEngine  | Ceiling, remanent, q override           |
| 3    | KPeriodAggregator      | Group by k period, period p additive    |
| 4    | ReturnsEngine          | NPS & Index returns, inflation          |
| 5    | ResponseMapper         | Domain → ProcessResponse DTO            |

---
