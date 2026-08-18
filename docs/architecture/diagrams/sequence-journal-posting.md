# Sequence Diagram - Journal Posting

## Journal Entry Posting Flow

```mermaid
sequenceDiagram
    participant Accountant as Accountant
    participant MVC as Thymeleaf Controller
    participant Command as Command Handler
    participant AppService as Journal Application Service
    participant JournalAggregate as Journal Aggregate
    participant LedgerService as Ledger Domain Service
    participant AccountRepo as Account Repository
    participant JournalRepo as Journal Repository
    participant EventPublisher as Event Publisher
    participant Audit as Audit Logger
    
    Accountant->>MVC: POST /journal/entries
    MVC->>MVC: Validate Form
    MVC->>Command: CreateJournalEntryCommand
    Command->>AppService: createJournalEntry(command)
    
    AppService->>LedgerService: validateAccounts(debitAccount, creditAccount)
    LedgerService->>AccountRepo: findById(debitAccountId)
    AccountRepo-->>LedgerService: DebitAccount
    LedgerService->>AccountRepo: findById(creditAccountId)
    AccountRepo-->>LedgerService: CreditAccount
    
    alt Accounts Valid
        LedgerService-->>AppService: ValidationResult (VALID)
        
        AppService->>JournalAggregate: createEntry(command)
        JournalAggregate->>JournalAggregate: validateDoubleEntry()
        JournalAggregate->>JournalAggregate: calculateBalances()
        
        alt Entry Valid
            AppService->>JournalRepo: save(journalEntry)
            AppService->>LedgerService: updateAccountBalances(journalEntry)
            LedgerService->>AccountRepo: updateBalance(debitAccount)
            LedgerService->>AccountRepo: updateBalance(creditAccount)
            AppService->>EventPublisher: publish(JournalEntryPostedEvent)
            AppService->>Audit: logJournalEntry()
            AppService-->>Command: JournalEntryResponse
        else Entry Invalid
            AppService->>Audit: logValidationFailure()
            AppService-->>Command: ValidationError
        end
    else Accounts Invalid
        LedgerService-->>AppService: ValidationResult (INVALID)
        AppService->>Audit: logAccountValidationFailure()
        AppService-->>Command: ValidationError
    end
    
    Command-->>MVC: JournalEntryResponse
    MVC-->>Accountant: Redirect to Journal View
```

## Journal Posting Steps

1. **Form Validation**: Thymeleaf controller validates journal entry form
2. **Command Creation**: CreateJournalEntryCommand created
3. **Account Validation**: Ledger service validates debit and credit accounts exist
4. **Journal Entry Creation**: Journal aggregate creates entry with double-entry validation
5. **Balance Calculation**: Calculate debit and credit balances
6. **Persistence**: Save journal entry to database
7. **Balance Update**: Update account balances in ledger
8. **Event Publishing**: Publish JournalEntryPostedEvent for notifications
9. **Audit Logging**: Log journal entry for compliance

## Key Design Decisions
- Journal aggregate enforces double-entry rules
- Ledger service validates account existence and balances
- Immutable journal entries ensure audit trail
- Domain events trigger trial balance updates
- Account balances updated atomically with journal entry
