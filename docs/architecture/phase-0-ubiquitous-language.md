# Phase 0: Architecture - Ubiquitous Language

## Overview
The ubiquitous language is the shared vocabulary used by all team members (developers, domain experts, stakeholders) to discuss the system. This language is embedded in the code, documentation, and conversations to ensure clear communication and reduce misunderstandings.

## Core Domain Terms

### Ledger
The central accounting record that maintains all financial transactions. The ledger ensures the accounting equation (Assets = Liabilities + Equity) always balances through double-entry bookkeeping.

**Usage:** "The ledger shows a total balance of $1,000,000 for all asset accounts."

### Journal
A chronological record of all financial transactions. Each journal entry represents a complete transaction with debits and credits that balance to zero.

**Usage:** "The journal entry JE-001 records the payment received from customer ABC."

### Posting
The process of recording a journal entry into the ledger, updating account balances, and ensuring the transaction is permanently recorded.

**Usage:** "Posting the journal entry updated the cash account balance by $5,000."

### Account
A category used to classify financial transactions. Each account has a type (Asset, Liability, Equity, Revenue, Expense) and a unique code.

**Usage:** "The cash account (1000) is an asset account that holds all cash transactions."

### Chart of Accounts
The structured list of all accounts used by the organization, organized hierarchically by account type and code.

**Usage:** "The chart of accounts includes 50 accounts across asset, liability, equity, revenue, and expense categories."

### Debit
An entry that increases asset and expense accounts, and decreases liability, equity, and revenue accounts.

**Usage:** "The debit to the cash account increases the balance by $10,000."

### Credit
An entry that increases liability, equity, and revenue accounts, and decreases asset and expense accounts.

**Usage:** "The credit to accounts payable increases the liability by $5,000."

### Double-Entry Bookkeeping
The accounting principle that every transaction must have equal debits and credits, ensuring the accounting equation always balances.

**Usage:** "The double-entry bookkeeping system ensures that total debits always equal total credits."

### Trial Balance
A report that lists all account balances to verify that total debits equal total credits.

**Usage:** "The trial balance confirms that the ledger is in balance with total debits of $500,000 and total credits of $500,000."

## Payment Terms

### Payment
A financial transaction where money is transferred from a payer to a payee in exchange for goods or services.

**Usage:** "The payment of $500 was processed via Stripe."

### Payment Gateway
An external service that processes payment transactions between the system and financial institutions.

**Usage:** "The payment gateway (Stripe) processed the credit card transaction."

### Webhook
An HTTP callback sent by the payment gateway to notify the system of payment status changes.

**Usage:** "The webhook from Stripe confirmed that the payment was successful."

### Settlement
The process of transferring funds from the payment gateway to the organization's bank account.

**Usage:** "The settlement of $10,000 was received in the bank account."

### Suspense Account
A temporary account used to hold transactions that cannot be immediately reconciled or identified.

**Usage:** "The orphaned webhook was posted to the suspense account for manual review."

## Reconciliation Terms

### Reconciliation
The process of comparing internal records (journal entries) with external records (payments, bank statements) to identify and resolve discrepancies.

**Usage:** "The daily reconciliation identified 3 unmatched transactions."

### Variance
A difference between the expected amount and the actual amount in a transaction.

**Usage:** "The variance of $0.50 was caused by a rounding difference."

### Orphaned Transaction
A transaction that exists in one system but not in the other (e.g., a payment with no corresponding journal entry).

**Usage:** "The orphaned transaction was posted to the suspense account."

### Matching
The process of identifying corresponding transactions between internal and external records.

**Usage:** "The matching algorithm successfully paired 95% of transactions."

## Reporting Terms

### Balance Sheet
A financial statement that reports the organization's assets, liabilities, and equity at a specific point in time.

**Usage:** "The balance sheet shows total assets of $2,000,000."

### Income Statement
A financial statement that reports revenues and expenses over a period of time, resulting in net income or loss.

**Usage:** "The income statement shows net income of $150,000 for Q3."

### Cash Flow Statement
A financial statement that reports the inflows and outflows of cash over a period of time.

**Usage:** "The cash flow statement shows operating cash flow of $200,000."

### Receipt
A document acknowledging that a payment was received, typically sent to the customer.

**Usage:** "The receipt was emailed to the customer after payment completion."

## Audit Terms

### Audit Trail
A chronological record of all changes to financial data, including who made the change, when, and what was changed.

**Usage:** "The audit trail shows that user john.doe modified the account balance on 2024-01-15."

### Audit Log
The system record that captures all auditable events for compliance and investigation.

**Usage:** "The audit log was exported for the quarterly audit."

### Compliance
Adherence to financial regulations, accounting standards, and internal policies.

**Usage:** "The system is compliant with PCI DSS requirements for payment processing."

## Security Terms

### Authentication
The process of verifying the identity of a user or system.

**Usage:** "JWT authentication is used for all API requests."

### Authorization
The process of determining what actions an authenticated user is allowed to perform.

**Usage:** "The authorization check confirmed that the user has permission to post journal entries."

### Role
A collection of permissions assigned to users to define their access level.

**Usage:** "The accountant role has permission to post journal entries but not to manage users."

### Permission
A specific action that a role is allowed to perform.

**Usage:** "The POST_JOURNAL_ENTRY permission allows users to create journal entries."

### HMAC
Hash-based Message Authentication Code used to verify the authenticity and integrity of webhook messages.

**Usage:** "The webhook signature was verified using HMAC-SHA256."

### Replay Attack
An attack where a valid message is resent maliciously to cause unintended actions.

**Usage:** "Replay protection prevents the same webhook from being processed twice."

## Technical Terms

### Aggregate
A cluster of domain objects that are treated as a single unit for data changes. The aggregate root ensures consistency within the aggregate.

**Usage:** "The Ledger aggregate includes the Chart of Accounts and all Account entities."

### Aggregate Root
The entity that is the only entry point for accessing and modifying the aggregate.

**Usage:** "The Ledger is the aggregate root for the ledger bounded context."

### Value Object
An immutable object that is defined by its attributes rather than identity. Two value objects with the same attributes are equal.

**Usage:** "Money is a value object that contains an amount and currency."

### Domain Event
An event that represents something that happened in the domain that other parts of the system may be interested in.

**Usage:** "The PaymentCompletedEvent was published when the payment was successfully processed."

### Repository
An interface that provides collection-like access to domain objects, abstracting the data storage mechanism.

**Usage:** "The PaymentRepository interface defines methods for saving and retrieving payments."

### Specification
A predicate that encapsulates business rules for querying or validating domain objects.

**Usage:** "The ActiveAccountSpecification filters for accounts that are currently active."

### Bounded Context
A distinct part of the domain logic with its own ubiquitous language and model.

**Usage:** "The Payment bounded context handles payment processing independently from the Ledger context."

### Module
An independently extractable unit of the modular monolith that corresponds to a bounded context.

**Usage:** "The payment module can be extracted into a microservice in the future."

## AI Terms

### Anomaly Detection
The process of identifying unusual patterns or outliers in financial data that may indicate errors or fraud.

**Usage:** "The anomaly detection identified a suspicious transaction of $1,000,000."

### Cash Flow Forecasting
The prediction of future cash inflows and outflows based on historical data and trends.

**Usage:** "The cash flow forecasting predicts a surplus of $50,000 next month."

### Insight
A piece of information derived from data analysis that provides actionable business value.

**Usage:** "The AI insight recommended reducing expenses in the marketing category."

### Confidence Score
A measure of how confident the AI system is about its prediction or recommendation.

**Usage:** "The anomaly detection has a confidence score of 95%."

## Code Examples

### Domain Model
```java
// Ledger is an aggregate root
public class Ledger {
    private ChartOfAccounts chartOfAccounts;
    // Ledger ensures consistency of all accounts
}

// Money is a value object
public record Money(BigDecimal amount, Currency currency) {
    // Immutable, defined by attributes
}

// PaymentCompletedEvent is a domain event
public class PaymentCompletedEvent {
    private PaymentId paymentId;
    private Money amount;
    // Published when payment completes
}
```

### Repository Interface
```java
// Repository is a port in hexagonal architecture
public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(PaymentId id);
}
```

### Application Service
```java
// Application service orchestrates use cases
public class PaymentApplicationService {
    public void initiatePayment(InitiatePaymentCommand command) {
        // Creates payment, processes via gateway, posts to ledger
    }
}
```

## Language Rules

1. **Use domain terms in code**: Class names, method names, and variable names should use ubiquitous language
2. **Avoid technical jargon in domain discussions**: When discussing with domain experts, use business terms
3. **Document term definitions**: Maintain this document as the source of truth for term definitions
4. **Evolve the language**: Update this document as the domain understanding grows
5. **Enforce consistency**: Code reviews should check for consistent use of ubiquitous language
