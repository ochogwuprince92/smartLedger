# Sequence Diagram - AI Insight

## AI Insight Generation Flow

```mermaid
sequenceDiagram
    participant Scheduler as Scheduled Job
    participant AppService as AI Application Service
    participant AIService as AI Domain Service
    participant LedgerRepo as Ledger Repository
    participant JournalRepo as Journal Repository
    participant ReconRepo as Reconciliation Repository
    participant AIAdapter as AI Service Adapter
    participant InsightAggregate as Insight Aggregate
    participant EventPublisher as Event Publisher
    participant Audit as Audit Logger
    participant Notification as Notification Service
    
    Scheduler->>AppService: generateAIInsights()
    AppService->>AIService: analyzeFinancialData()
    
    AIService->>LedgerRepo: findAccountBalances()
    LedgerRepo-->>AIService: List<AccountBalance>
    
    AIService->>JournalRepo: findJournalEntries(period)
    JournalRepo-->>AIService: List<JournalEntry>
    
    AIService->>ReconRepo: findReconciliations(period)
    ReconRepo-->>AIService: List<Reconciliation>
    
    AIService->>AIService: prepareAnalysisData()
    
    AIService->>AIAdapter: detectAnomalies(data)
    AIAdapter->>AIAdapter: Call AI API
    AIAdapter-->>AIService: AnomalyDetectionResult
    
    AIService->>AIAdapter: forecastCashFlow(data)
    AIAdapter->>AIAdapter: Call AI API
    AIAdapter-->>AIService: CashFlowForecast
    
    AIService->->AIAdapter: generateRecommendations(data)
    AIAdapter->>AIAdapter: Call AI API
    AIAdapter-->>AIService: Recommendations
    
    AIService->>InsightAggregate: createInsight(anomalies, forecast, recommendations)
    InsightAggregate->>InsightAggregate: calculateConfidenceScores()
    InsightAggregate->>InsightAggregate: prioritizeInsights()
    
    alt Critical Anomalies Detected
        AIService->>EventPublisher: publish(CriticalAnomalyEvent)
        AIService->>Notification: sendCriticalAlert(financeTeam)
    end
    
    AIService->>InsightRepo: save(insight)
    AIService->>Audit: logAIInsightGeneration()
    AIService-->>AppService: AIInsightResult
    AppService-->>Scheduler: Complete
```

## AI Insight Steps

1. **Scheduled Execution**: Job triggers AI insight generation
2. **Data Collection**: Fetch ledger, journal, and reconciliation data
3. **Data Preparation**: Prepare data for AI analysis
4. **Anomaly Detection**: AI service detects unusual patterns
5. **Cash Flow Forecasting**: AI service predicts cash flow
6. **Recommendations**: AI service generates actionable recommendations
7. **Insight Creation**: Insight aggregate consolidates AI results
8. **Priority Calculation**: Calculate confidence scores and prioritize
9. **Alerting**: Alert finance team to critical anomalies
10. **Persistence**: Save insights for reporting
11. **Audit Logging**: Log AI insight generation

## Key Design Decisions
- Scheduled jobs ensure regular AI analysis
- AI adapter isolates external AI service
- Insight aggregate consolidates AI results
- Confidence scores indicate reliability
- Priority ranking helps focus on important insights
- Critical anomalies trigger immediate alerts
- Audit trail for AI-generated insights
