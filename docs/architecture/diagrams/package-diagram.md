# Package Diagram

## Module Package Structure

```mermaid
graph TB
    subgraph "com.finance.smartLedger"
        CONFIG[configuration]
        COMMON[common]
        EXCEPTION[exception]
        SECURITY[security]
        AUDIT[audit]
        SHARED[shared]
        
        subgraph "payment"
            PAYMENT_DOMAIN[payment.domain]
            PAYMENT_APP[payment.application]
            PAYMENT_INFRA[payment.infrastructure]
            PAYMENT_PRES[payment.presentation]
        end
        
        subgraph "ledger"
            LEDGER_DOMAIN[ledger.domain]
            LEDGER_APP[ledger.application]
            LEDGER_INFRA[ledger.infrastructure]
            LEDGER_PRES[ledger.presentation]
        end
        
        subgraph "journal"
            JOURNAL_DOMAIN[journal.domain]
            JOURNAL_APP[journal.application]
            JOURNAL_INFRA[journal.infrastructure]
            JOURNAL_PRES[journal.presentation]
        end
        
        subgraph "reconciliation"
            RECON_DOMAIN[reconciliation.domain]
            RECON_APP[reconciliation.application]
            RECON_INFRA[reconciliation.infrastructure]
            RECON_PRES[reconciliation.presentation]
        end
        
        subgraph "reporting"
            REPORT_DOMAIN[reporting.domain]
            REPORT_APP[reporting.application]
            REPORT_INFRA[reporting.infrastructure]
            REPORT_PRES[reporting.presentation]
        end
        
        subgraph "notification"
            NOTIF_DOMAIN[notification.domain]
            NOTIF_APP[notification.application]
            NOTIF_INFRA[notification.infrastructure]
            NOTIF_PRES[notification.presentation]
        end
        
        subgraph "receipt"
            RECEIPT_DOMAIN[receipt.domain]
            RECEIPT_APP[receipt.application]
            RECEIPT_INFRA[receipt.infrastructure]
            RECEIPT_PRES[receipt.presentation]
        end
        
        subgraph "ai"
            AI_DOMAIN[ai.domain]
            AI_APP[ai.application]
            AI_INFRA[ai.infrastructure]
            AI_PRES[ai.presentation]
        end
    end
    
    PAYMENT_PRES --> PAYMENT_APP
    PAYMENT_APP --> PAYMENT_DOMAIN
    PAYMENT_APP --> PAYMENT_INFRA
    PAYMENT_INFRA --> PAYMENT_DOMAIN
    
    LEDGER_PRES --> LEDGER_APP
    LEDGER_APP --> LEDGER_DOMAIN
    LEDGER_APP --> LEDGER_INFRA
    LEDGER_INFRA --> LEDGER_DOMAIN
    
    JOURNAL_PRES --> JOURNAL_APP
    JOURNAL_APP --> JOURNAL_DOMAIN
    JOURNAL_APP --> JOURNAL_INFRA
    JOURNAL_INFRA --> JOURNAL_DOMAIN
    
    RECON_PRES --> RECON_APP
    RECON_APP --> RECON_DOMAIN
    RECON_APP --> RECON_INFRA
    RECON_INFRA --> RECON_DOMAIN
    
    REPORT_PRES --> REPORT_APP
    REPORT_APP --> REPORT_DOMAIN
    REPORT_APP --> REPORT_INFRA
    REPORT_INFRA --> REPORT_DOMAIN
    
    NOTIF_PRES --> NOTIF_APP
    NOTIF_APP --> NOTIF_DOMAIN
    NOTIF_APP --> NOTIF_INFRA
    NOTIF_INFRA --> NOTIF_DOMAIN
    
    RECEIPT_PRES --> RECEIPT_APP
    RECEIPT_APP --> RECEIPT_DOMAIN
    RECEIPT_APP --> RECEIPT_INFRA
    RECEIPT_INFRA --> RECEIPT_DOMAIN
    
    AI_PRES --> AI_APP
    AI_APP --> AI_DOMAIN
    AI_APP --> AI_INFRA
    AI_INFRA --> AI_DOMAIN
    
    PAYMENT_APP --> SHARED
    LEDGER_APP --> SHARED
    JOURNAL_APP --> SHARED
    RECON_APP --> SHARED
    REPORT_APP --> SHARED
    NOTIF_APP --> SHARED
    RECEIPT_APP --> SHARED
    AI_APP --> SHARED
    
    PAYMENT_PRES --> SECURITY
    LEDGER_PRES --> SECURITY
    JOURNAL_PRES --> SECURITY
    RECON_PRES --> SECURITY
    REPORT_PRES --> SECURITY
    NOTIF_PRES --> SECURITY
    RECEIPT_PRES --> SECURITY
    AI_PRES --> SECURITY
    
    PAYMENT_INFRA --> AUDIT
    LEDGER_INFRA --> AUDIT
    JOURNAL_INFRA --> AUDIT
    RECON_INFRA --> AUDIT
    REPORT_INFRA --> AUDIT
    NOTIF_INFRA --> AUDIT
    RECEIPT_INFRA --> AUDIT
    AI_INFRA --> AUDIT
```

## Package Structure Details

### Root Packages
- **configuration**: Spring configuration, beans, profiles
- **common**: Shared utilities, constants, enums
- **exception**: Custom exceptions, global exception handler
- **security**: Security configuration, JWT, permissions
- **audit**: Audit logging, change tracking
- **shared**: Shared kernel (base entities, value objects)

### Module Packages (each module has 4 layers)
- **domain**: Domain model (entities, aggregates, value objects, domain events, repository interfaces)
- **application**: Application services, command/query handlers, DTOs
- **infrastructure**: Repository implementations, external adapters, persistence
- **presentation**: Controllers, views, API contracts

### Module Independence
- Each module can be extracted into a microservice
- Modules communicate via application events and interfaces
- Domain layer has no dependencies on other modules
- Shared kernel provides common infrastructure
