# Sequence Diagram - Reconciliation

## Reconciliation Process Flow

```mermaid
sequenceDiagram
    participant Scheduler as Scheduled Job
    participant AppService as Reconciliation Application Service
    participant ReconService as Reconciliation Domain Service
    participant PaymentRepo as Payment Repository
    participant JournalRepo as Journal Repository
    participant ReconAggregate as Reconciliation Aggregate
    participant LedgerService as Ledger Domain Service
    participant SuspenseService as Suspense Account Service
    participant EventPublisher as Event Publisher
    participant Audit as Audit Logger
    participant Notification as Notification Service
    
    Scheduler->>AppService: executeDailyReconciliation()
    AppService->>ReconService: initiateReconciliation(date)
    
    ReconService->>PaymentRepo: findCompletedPaymentsByDate(date)
    PaymentRepo-->>ReconService: List<Payment>
    
    ReconService->>JournalRepo: findJournalEntriesByDate(date)
    JournalRepo-->>ReconService: List<JournalEntry>
    
    ReconService->>ReconAggregate: createReconciliation(payments, entries)
    ReconAggregate->>ReconAggregate: matchTransactions()
    
    loop For Each Transaction
        ReconAggregate->>ReconAggregate: compareAmounts()
        
        alt Amounts Match
            ReconAggregate->>ReconAggregate: markMatched()
        else Amounts Differ
            ReconAggregate->>ReconAggregate: markVariance()
            ReconAggregate->>ReconAggregate: calculateVarianceAmount()
        end
    end
    
    ReconAggregate->>ReconAggregate: calculateTotals()
    ReconAggregate->>ReconAggregate: generateReconciliationReport()
    
    alt All Matched
        ReconService->>EventPublisher: publish(ReconciliationCompletedEvent)
        ReconService->>Audit: logReconciliationSuccess()
    else Variances Found
        ReconService->>SuspenseService: createVarianceEntries(variances)
        SuspenseService->>LedgerService: postToSuspenseAccount(variance)
        ReconService->>EventPublisher: publish(ReconciliationVarianceEvent)
        ReconService->>Notification: sendVarianceAlert(financeTeam)
        ReconService->>Audit: logReconciliationVariance()
    else Orphaned Transactions
        ReconService->>SuspenseService: createOrphanedEntries(orphaned)
        ReconService->>EventPublisher: publish(OrphanedTransactionEvent)
        ReconService->>Notification: sendOrphanedAlert(financeTeam)
        ReconService->>Audit: logOrphanedTransactions()
    end
    
    ReconService->>ReconRepo: save(reconciliation)
    ReconService-->>AppService: ReconciliationResult
    AppService-->>Scheduler: Complete
```

## Reconciliation Steps

1. **Scheduled Execution**: Daily job triggers reconciliation
2. **Data Retrieval**: Fetch payments and journal entries for date
3. **Matching**: Compare payment amounts with journal entries
4. **Variance Detection**: Identify amount differences
5. **Report Generation**: Create reconciliation report
6. **Variance Handling**: Post variances to suspense account
7. **Orphan Handling**: Handle unmatched transactions
8. **Notification**: Alert finance team of issues
9. **Audit Logging**: Log reconciliation results

## Key Design Decisions
- Scheduled jobs ensure daily reconciliation
- Reconciliation aggregate encapsulates matching logic
- Variances automatically posted to suspense account
- Notifications alert finance team to issues
- Audit trail maintained for compliance
