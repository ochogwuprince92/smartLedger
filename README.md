# Finance Ledger & Reconciliation Microservice

smartLedger is a production-grade, modular monolith microservice for financial accounting, designed
primarily for school bursary systems and fee portals, with applicability to SMEs, SaaS platforms, payment
service providers, and non-profit organizations.

## Overview

This microservice provides a robust accounting engine with double-entry bookkeeping, automated
reconciliation, comprehensive audit trails, and AI-powered financial insights. Built following Clean
Architecture, Hexagonal Architecture, and Domain-Driven Design principles.

## Target Audience

### Primary

- **School Bursary Systems**
- **Fee Portals**
- Small to Medium Enterprises (SMEs)
- SaaS Platforms with embedded financial accounting
- Payment Service Providers
- Non-Profit Organizations

## Architecture

### Architectural Principles

- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion
- **Clean Architecture**: Dependencies point inward - Domain never depends on Spring or infrastructure
- **Hexagonal Architecture**: Ports and Adapters pattern with clear separation between core domain and external concerns
- **Domain-Driven Design**: Everything starts from the domain model
- **Test-Driven Development**: No implementation without tests

### Technology Stack

- Java 21
- Spring Boot 3.3.0
- PostgreSQL 15+
- Flyway (Database migrations)
- Docker (Containerization)
- Jenkins (CI/CD)
- Spring Security with JWT (Authentication and authorization)
- Thymeleaf (MVC for web UI)
- Testcontainers (Integration testing)
- MapStruct (Object mapping)
- OpenAPI/SpringDoc (API documentation)

### Bounded Contexts

The system is organized into 11 independent modules:

- **Payment** - Payment processing, gateway integration, webhook handling
- **Fees** - Fee schedules, invoicing, and per-student fee payment tracking
- **Ledger** - Chart of accounts, account balances, accounting equation
- **Journal** - Immutable journal entries, double-entry validation
- **Reconciliation** - Transaction matching, variance detection
- **Reporting** - Financial reports (Balance Sheet, Income Statement, Cash Flow)
- **Security** - Authentication, authorization, role management (cross-cutting)
- **Audit** - Audit trail, change tracking (cross-cutting)
- **Notification** - Email/SMS notifications
- **Receipt** - PDF receipt generation and delivery
- **AI Insight** - Anomaly detection, cash flow forecasting, recommendations

## Data Model

Full entity-relationship diagram across all 11 bounded contexts:

**![Finance Ledger and Reconciliation Microservice - ERD](src/main/resources/static/erd%20%281%29.png)**


### Entity relationships — explanation

**Security (`User`, `Role`, `Permission`, `ServiceCredential`, `PasswordResetToken`)**
Access control is role- and permission-based. A `User` can hold many `Role`s and can also be granted
`Permission`s directly (bypassing roles), via the `user_roles` and `user_permissions` join tables. A `Role`
in turn holds a set of `Permission`s (`role_permissions`) and can inherit from other roles through a
self-referencing `role_hierarchy` table (parent/child roles). `ServiceCredential` is a separate,
non-human identity (API key) with its own flat list of granted permission codes, used for
service-to-service calls. `PasswordResetToken` belongs to exactly one `User`.

**Fees & Billing (`FeeSchedule`, `FeeScheduleItem`, `FeeInvoice`, `FeeInvoiceLineItem`, `FeePayment`)**
A `FeeSchedule` (e.g. "2026 Term 1 fees") is made up of one or more `FeeScheduleItem`s (tuition, PTA levy,
etc.). When fees are billed to a student, a `FeeInvoice` is generated with its own itemized
`FeeInvoiceLineItem`s and running balance. Payments recorded against that invoice are tracked as
`FeePayment` rows, each belonging to exactly one `FeeInvoice`. A `FeePayment` can optionally originate from
a gateway `Payment` (see below) via `sourcePaymentId`, linking an online payment back to the specific
invoice it settled.

**Payments & Receipts (`Payment`, `Receipt`)**
`Payment` is the gateway-facing entity: it tracks a payment transaction end-to-end through its own state
machine (`PENDING → PROCESSING → COMPLETED`, or `FAILED`/`REFUNDED`), including the gateway reference,
transaction ID, and response codes returned by the payment gateway. Once a `Payment` completes, a
corresponding `Receipt` is generated for the payer. Both the `Payment → Receipt` link and the
`Payment → FeeInvoice` link are soft (UUID-matched, not FK-enforced), keeping the payments module
independent of the fees module.

**General Ledger (`Account`, `Transaction`, `SuspenseAccount`)**
`Account` is the chart of accounts (assets, liabilities, equity, revenue, expense), and supports a
self-referencing parent/child hierarchy for sub-accounts. Every financial event is recorded as a
double-entry `Transaction`, which references exactly one debit `Account` and one credit `Account`. A
completed `Payment` results in a `Transaction` being posted (cash debited, accounts-receivable credited),
linked back to the payment via its reference number rather than a hard FK. `SuspenseAccount` holds
unresolved reconciliation variances until they're investigated and cleared.

**Journal (`JournalEntry`, `JournalLineItem`)**
`JournalEntry` represents a manual or system-generated accounting entry (e.g. month-end adjustments),
made up of balanced `JournalLineItem`s, each debiting or crediting a specific account.

**Reconciliation (`Reconciliation`, `ReconciliationItem`)**
A `Reconciliation` run (e.g. matching a gateway settlement batch against the ledger) contains many
`ReconciliationItem`s, each comparing an expected amount against what actually cleared. An item can be
matched to a specific `Transaction`, and unexplained variances at the `Reconciliation` level can be booked
to a `SuspenseAccount` pending review.

**AI Insights (`AIInsight`)**
`AIInsight` stores the result of an automated analysis run — typically anomaly detection or root-cause
summarization over a specific `Reconciliation`'s variances — including risk level and recommendations.

**Cross-cutting (`AuditLog`, `Notification`, `Report`)**
These are intentionally **polymorphic** rather than tied to one entity: `AuditLog` records create/update/
delete actions against `entityType` + `entityId` for *any* auditable entity in the system, `Notification`
likewise references an arbitrary `relatedEntityType` + `relatedEntityId` (e.g. a payment confirmation or an
overdue-invoice reminder), and `Report` is a standalone generated artifact (e.g. a monthly financial
statement) with no FK back to source records — it stores its own snapshot data and file path.

## Project Structure

```
smartLedger/
├── docs/                    # Architecture documentation
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/finance/smartLedger/
│   │   │       ├── configuration/    # Spring configuration
│   │   │       ├── common/           # Shared utilities
│   │   │       ├── exception/        # Custom exceptions
│   │   │       ├── security/         # Security context
│   │   │       ├── audit/            # Audit logging
│   │   │       ├── shared/           # Shared kernel
│   │   │       ├── payment/          # Payment context
│   │   │       ├── fees/             # Fees context
│   │   │       ├── ledger/           # Ledger context
│   │   │       ├── journal/          # Journal context
│   │   │       ├── reconciliation/   # Reconciliation context
│   │   │       ├── reporting/        # Reporting context
│   │   │       ├── notification/     # Notification context
│   │   │       ├── receipt/          # Receipt context
│   │   │       └── ai/               # AI Insight context
│   │   └── resources/
│   │       ├── static/               # Static assets
│   │       ├── templates/            # Thymeleaf templates
│   │       └── application.yml      # Configuration
│   └── test/                         # Test sources
├── Jenkinsfile
├── Dockerfile
├── docker-compose.yml
└── pom.xml
└── Readme.MD
```

Each module contains four layers:

- **domain** - Entities, Aggregates, Value Objects, Domain Events, Repository Interfaces
- **application** - Application Services, Command/Query Handlers, DTOs
- **infrastructure** - Repository Implementations, External Adapters
- **presentation** - REST Controllers, Thymeleaf Controllers

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd smartLedger
```

### 2. Configure Database

Update `src/main/resources/application.yml` with your PostgreSQL and Redis credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartledger
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 3. Run Database Migrations

Flyway migrations will run automatically on application startup. To run manually:

```bash
./mvnw flyway:migrate
```

### 4. Build the Project

```bash
./mvnw clean install
```

### 5. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on port 8081.

### 6. Access the Application

- REST API: http://localhost:8081/swagger-ui/index.html
- Web UI: http://localhost:8081

### 7. Email Configuration (Local Development)

To enable email functionality in local development, you need to configure Gmail SMTP settings with an App Password:

**Step 3: Create `application-local.yml`**

**Step 4: Set Environment Variables**

**Step 5: Run with Local Profile**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Docker Deployment

### Using Docker Compose

```bash
docker-compose up -d
```

### Building Docker Image

```bash
docker build -t finance-ledger:latest .
docker run -p 8080:8080 finance-ledger:latest
```

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify
```

### Test Coverage

```bash
./mvnw jacoco:report
```
## Configuration Profiles

### Checkstyle
```bash
./mvnw checkstyle:check
```

### Spotless (Code Formatting)
```bash
./mvnw spotless:check
./mvnw spotless:apply
```

## CI/CD with Jenkins

### Jenkins Pipeline

The project uses Jenkins for continuous integration and deployment. The `Jenkinsfile` defines the pipeline stages:

### Jenkins Configuration

Install required Jenkins plugins:
- Pipeline
- Docker Pipeline
- Git
- Maven Integration
- JaCoCo
- OWASP Dependency-Check

### Manual Jenkins Build

To trigger a build manually from Jenkins:

```bash
# Or use Jenkins CLI
java -jar jenkins-cli.jar -s http://jenkins-server:8080 build <job-name>
```

### Overview

The system uses Spring's `@Scheduled` annotation for background task execution. Scheduled tasks are essential for:

### Cron Job Examples

The system includes the following scheduled jobs:

**Daily Reconciliation Job**
- Schedule: `0 0 2 * * ?` (2:00 AM daily)
- Purpose: Match payments with journal entries, detect variances
- Location: `src/main/java/com/finance/smartLedger/reconciliation/infrastructure/scheduler/ReconciliationScheduler.java`

### Enabling Scheduling

To enable scheduled tasks, add the `@EnableScheduling` annotation to the main application class:

Example cron expressions:
- `0 0 12 * * ?` - Every day at noon
- `0 15 10 ? * MON-FRI` - Monday to Friday at 10:15 AM
- `0 0/5 * * * ?` - Every 5 minutes
- `0 0 8,12,18 * * ?` - Daily at 8 AM, 12 PM, and 6 PM

## Presentation

Slides covering the project overview, features, architecture, payment lifecycle, and tools/technologies
used are available here:

📄 **[Finance Ledger and Reconciliation Microservice](https://docs.google.com/presentation/d/1zWDT-j6rHVI6FXRPzjig5dz_3FmAUQE9PqWVvFgYRi8/edit?usp=sharing)**

## Support

For issues, questions, or contributions, please contact [ochogwuprince92@gmail.com].