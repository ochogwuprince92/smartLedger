# Phase 2: Project Scaffold

## Overview
This document describes the project scaffold for the Finance Ledger & Reconciliation microservice. The scaffold follows a modular monolith architecture where each bounded context is an independent module that can be extracted into a microservice in the future.

## Project Structure

```
smartLedger/
├── docs/
│   └── architecture/
│       ├── phase-0-vision.md
│       ├── phase-0-product-requirements.md
│       ├── phase-0-adr.md
│       ├── phase-0-ubiquitous-language.md
│       ├── phase-0-bounded-contexts.md
│       ├── phase-1-solution-architecture.md
│       ├── phase-2-project-scaffold.md
│       └── diagrams/
│           ├── context-diagram.md
│           ├── container-diagram.md
│           ├── component-diagram.md
│           ├── package-diagram.md
│           ├── sequence-payment.md
│           ├── sequence-webhook.md
│           ├── sequence-journal-posting.md
│           ├── sequence-reconciliation.md
│           ├── sequence-receipt.md
│           └── sequence-ai-insight.md
├── .jenkins/                   # Jenkins pipeline scripts (optional)
│   └── scripts/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── finance/
│   │   │           └── smartLedger/
│   │   │               ├── SmartLedgerApplication.java
│   │   │               ├── configuration/          # Spring configuration
│   │   │               │   ├── scheduling/
│   │   │               │   └── ...
│   │   │               ├── common/                 # Shared utilities, constants
│   │   │               │   └── infrastructure/
│   │   │               │       └── scheduler/
│   │   │               ├── exception/              # Custom exceptions
│   │   │               ├── security/               # Security configuration
│   │   │               ├── audit/                  # Audit logging
│   │   │               ├── shared/                 # Shared kernel
│   │   │               ├── payment/                # Payment context
│   │   │               │   ├── domain/
│   │   │               │   ├── application/
│   │   │               │   ├── infrastructure/
│   │   │               │   └── presentation/
│   │   │               ├── ledger/                 # Ledger context
│   │   │               │   ├── domain/
│   │   │               │   ├── application/
│   │   │               │   ├── infrastructure/
│   │   │               │   └── presentation/
│   │   │               ├── journal/                 # Journal context
│   │   │               │   ├── domain/
│   │   │               │   ├── application/
│   │   │               │   ├── infrastructure/
│   │   │               │   └── presentation/
│   │   │               ├── reconciliation/         # Reconciliation context
│   │   │               │   ├── domain/
│   │   │               │   ├── application/
│   │   │               │   ├── infrastructure/
│   │   │               │   │   ├── persistence/
│   │   │               │   │   ├── messaging/
│   │   │               │   │   ├── external/
│   │   │               │   │   └── scheduler/
│   │   │               │   └── presentation/
│   │   │               ├── reporting/              # Reporting context
│   │   │               │   ├── domain/
│   │   │               │   ├── application/
│   │   │               │   ├── infrastructure/
│   │   │               │   │   ├── persistence/
│   │   │               │   │   ├── external/
│   │   │               │   │   └── scheduler/
│   │   │               │   └── presentation/
│   │   │               ├── notification/           # Notification context
│   │   │               │   ├── domain/
│   │   │               │   ├── application/
│   │   │               │   ├── infrastructure/
│   │   │               │   │   ├── email/
│   │   │               │   │   ├── sms/
│   │   │               │   │   └── scheduler/
│   │   │               │   └── presentation/
│   │   │               ├── receipt/                # Receipt context
│   │   │               │   ├── domain/
│   │   │               │   ├── application/
│   │   │               │   ├── infrastructure/
│   │   │               │   └── presentation/
│   │   │               └── ai/                     # AI Insight context
│   │   │                   ├── domain/
│   │   │                   ├── application/
│   │   │                   ├── infrastructure/
│   │   │                   │   ├── external/
│   │   │                   │   └── scheduler/
│   │   │                   └── presentation/
│   │   └── resources/
│   │       ├── static/               # Static assets (CSS, JS, images)
│   │       ├── templates/            # Thymeleaf templates
│   │       ├── application.yml      # Application configuration
│   │       ├── application-dev.yml  # Development profile
│   │       ├── application-test.yml # Test profile
│   │       └── application-prod.yml # Production profile
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── finance/
│       │           └── smartLedger/
│       │               ├── payment/
│       │               │   ├── domain/
│       │               │   ├── application/
│       │               │   └── infrastructure/
│       │               ├── ledger/
│       │               │   ├── domain/
│       │               │   ├── application/
│       │               │   └── infrastructure/
│       │               ├── journal/
│       │               │   ├── domain/
│       │               │   ├── application/
│       │               │   └── infrastructure/
│       │               ├── reconciliation/
│       │               │   ├── domain/
│       │               │   ├── application/
│       │               │   └── infrastructure/
│       │               ├── reporting/
│       │               │   ├── domain/
│       │               │   ├── application/
│       │               │   └── infrastructure/
│       │               ├── notification/
│       │               │   ├── domain/
│       │               │   ├── application/
│       │               │   └── infrastructure/
│       │               ├── receipt/
│       │               │   ├── domain/
│       │               │   ├── application/
│       │               │   └── infrastructure/
│       │               └── ai/
│       │                   ├── domain/
│       │                   ├── application/
│       │                   └── infrastructure/
│       └── resources/
│           ├── application-test.yml
│           └── test-data/
├── .gitignore
├── .gitattributes
├── Jenkinsfile              # CI/CD pipeline configuration
├── pom.xml
├── mvnw
├── mvnw.cmd
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Module Structure

Each module follows the same four-layer structure:

### 1. Domain Layer
**Purpose**: Contains the core business logic and domain model.

**Contents**:
- Entities (domain objects with identity)
- Aggregates (consistency boundaries)
- Value Objects (immutable values)
- Domain Events (business events)
- Repository Interfaces (ports)
- Domain Services (business logic that doesn't belong to entities)
- Specifications (business rules)
- Factories (complex object creation)

**Rules**:
- No dependencies on Spring or external frameworks
- No dependencies on other modules' domain layers
- Pure Java with business logic only
- All monetary values use BigDecimal
- All entities use UUID for identity

### 2. Application Layer
**Purpose**: Orchestrates use cases and coordinates domain objects.

**Contents**:
- Application Services (use case orchestration)
- Command Handlers (CQRS write operations)
- Query Handlers (CQRS read operations)
- DTOs (Data Transfer Objects)
- Command/Query objects
- Event Publishers

**Rules**:
- Depends on domain layer
- Depends on infrastructure layer interfaces
- No business logic (delegates to domain)
- Manages transactions
- Publishes domain events

### 3. Infrastructure Layer
**Purpose**: Implements technical capabilities and external integrations.

**Contents**:
- Repository Implementations (JPA, JDBC)
- External Service Adapters (Payment Gateway, Email, SMS, AI)
- Persistence Configuration (JPA, Flyway)
- Cache Implementation (Redis)
- File Storage (Cloud storage for receipts)
- Mappers (MapStruct mappers)
- Scheduled Tasks (Cron jobs for background processing)

**Rules**:
- Implements interfaces defined in domain layer
- Depends on Spring and external libraries
- No business logic
- Isolates external dependencies
- Scheduled tasks use Spring's @Scheduled annotation

### 4. Presentation Layer
**Purpose**: Handles external communication (REST API, Web UI).

**Contents**:
- REST Controllers
- Thymeleaf Controllers
- Request/Response DTOs
- Validators
- View Models

**Rules**:
- Depends on application layer
- No business logic
- Handles validation
- Converts between DTOs and domain objects

## Shared Kernel

The following packages are shared across all modules:

### configuration
Spring configuration beans, profiles, and application setup.

### common
Shared utilities, constants, enums that don't belong to a specific domain.

### exception
Custom exceptions and global exception handling.

### security
Security configuration, JWT setup, role-based access control.

### audit
Audit logging infrastructure and change tracking.

### shared
Shared kernel containing:
- BaseEntity (base entity with UUID, timestamps)
- AuditableEntity (audit fields)
- Money Value Object
- ApiResponse (standard API response)
- ProblemDetail (error response)
- Error Codes
- Pagination
- UUID Generator
- Clock Provider
- Security Context

## Module Independence

Each module is designed to be independently extractable into a microservice:

1. **Clear Boundaries**: Each module has its own domain, application, infrastructure, and presentation layers
2. **Loose Coupling**: Modules communicate via domain events and application interfaces
3. **No Direct Dependencies**: Domain layers never depend on other modules
4. **Shared Kernel**: Common infrastructure is in the shared package
5. **Interface-Based Communication**: Infrastructure implements interfaces defined in domain

## Extraction Strategy

To extract a module into a microservice:

1. Create a new Spring Boot project
2. Copy the module's domain, application, infrastructure, and presentation packages
3. Copy required shared kernel classes
4. Implement REST API for inter-service communication
5. Replace in-memory event publishing with message broker (RabbitMQ, Kafka)
6. Update dependencies between modules to use REST API


## Next Steps

The scaffold is now complete. The next phase (Phase 3) will configure:
- Spring Boot dependencies
- PostgreSQL configuration
- Flyway migrations
- Docker setup
- Testing tools (JUnit, Mockito, Testcontainers)
- Code quality tools (Checkstyle, Spotless)
- Build tools (MapStruct, Maven profiles)
- Jenkins pipeline (Jenkinsfile in project root)
- Scheduled task configuration (scheduler packages in infrastructure layer)
