# Sequence Diagram - Payment Processing

## Payment Flow

```mermaid
sequenceDiagram
    participant Client as External Client
    participant REST as REST Controller
    participant Command as Command Handler
    participant AppService as Payment Application Service
    participant PaymentAggregate as Payment Aggregate
    participant LedgerService as Ledger Domain Service
    participant JournalService as Journal Domain Service
    participant Gateway as Payment Gateway Adapter
    participant Repo as Payment Repository
    participant EventPublisher as Event Publisher
    participant Audit as Audit Logger
    
    Client->>REST: POST /api/payments/initiate
    REST->>REST: Validate Request
    REST->>Command: InitiatePaymentCommand
    Command->>AppService: initiatePayment(command)
    
    AppService->>PaymentAggregate: createPayment()
    PaymentAggregate-->>AppService: Payment (PENDING)
    
    AppService->>Gateway: processPayment(payment)
    Gateway->>Gateway: Call Payment Gateway API
    Gateway-->>AppService: GatewayResponse
    
    alt Payment Successful
        AppService->>PaymentAggregate: markCompleted()
        AppService->>LedgerService: postToLedger(payment)
        LedgerService->>JournalService: createJournalEntry()
        JournalService-->>LedgerService: JournalEntry
        LedgerService-->>AppService: PostingResult
        AppService->>Repo: save(payment)
        AppService->>EventPublisher: publish(PaymentCompletedEvent)
        AppService->>Audit: logPaymentSuccess()
        AppService-->>Command: PaymentResponse (SUCCESS)
    else Payment Failed
        AppService->>PaymentAggregate: markFailed()
        AppService->>Repo: save(payment)
        AppService->>EventPublisher: publish(PaymentFailedEvent)
        AppService->>Audit: logPaymentFailure()
        AppService-->>Command: PaymentResponse (FAILED)
    end
    
    Command-->>REST: PaymentResponse
    REST-->>Client: 200 OK with PaymentResponse
```

## Payment Processing Steps

1. **Request Validation**: REST controller validates payment request
2. **Command Creation**: Command handler creates InitiatePaymentCommand
3. **Payment Creation**: Application service creates Payment aggregate in PENDING state
4. **Gateway Processing**: Payment gateway adapter processes payment via external API
5. **Success Path**:
   - Payment marked as COMPLETED
   - Ledger service posts double-entry to ledger
   - Journal service creates immutable journal entry
   - Payment persisted to database
   - PaymentCompletedEvent published
   - Audit log updated
6. **Failure Path**:
   - Payment marked as FAILED
   - Payment persisted to database
   - PaymentFailedEvent published
   - Audit log updated
7. **Response**: Return payment response to client

## Key Design Decisions
- Payment aggregate ensures consistency of payment state
- Ledger and Journal services handle double-entry posting
- Domain events trigger notifications and reconciliation
- Audit trail maintained for compliance
- Gateway adapter isolates external payment system
