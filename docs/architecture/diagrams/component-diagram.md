# Component Diagram

## Application Components

```mermaid
graph TB
    subgraph "Presentation Layer"
        REST[REST Controllers]
        MVC[Thymeleaf Controllers]
        DTO[DTOs]
        VALIDATION[Validation]
    end
    
    subgraph "Application Layer"
        COMMAND[Command Handlers]
        QUERY[Query Handlers]
        ORCHESTRATION[Application Services]
        EVENTS[Event Publisher]
    end
    
    subgraph "Domain Layer"
        AGGREGATES[Aggregates]
        ENTITIES[Entities]
        VOS[Value Objects]
        DOMAIN_EVENTS[Domain Events]
        DOMAIN_SERVICES[Domain Services]
        REPOSITORIES[Repository Interfaces]
        SPECS[Specifications]
    end
    
    subgraph "Infrastructure Layer"
        JPA_REPOS[JPA Repositories]
        MIGRATIONS[Flyway Migrations]
        SECURITY[Security Config]
        GATEWAY[Payment Gateway Adapter]
        EMAIL_ADAPTER[Email Adapter]
        SMS_ADAPTER[SMS Adapter]
        AI_ADAPTER[AI Adapter]
        CACHE[Redis Cache]
        AUDIT[Audit Logger]
    end
    
    REST --> COMMAND
    REST --> QUERY
    MVC --> COMMAND
    MVC --> QUERY
    
    COMMAND --> ORCHESTRATION
    QUERY --> ORCHESTRATION
    
    ORCHESTRATION --> AGGREGATES
    ORCHESTRATION --> DOMAIN_SERVICES
    ORCHESTRATION --> REPOSITORIES
    ORCHESTRATION --> EVENTS
    
    AGGREGATES --> ENTITIES
    AGGREGATES --> VOS
    AGGREGATES --> DOMAIN_EVENTS
    
    DOMAIN_SERVICES --> SPECS
    DOMAIN_SERVICES --> REPOSITORIES
    
    REPOSITORIES --> JPA_REPOS
    
    JPA_REPOS --> MIGRATIONS
    ORCHESTRATION --> CACHE
    COMMAND --> SECURITY
    QUERY --> SECURITY
    
    ORCHESTRATION --> GATEWAY
    ORCHESTRATION --> EMAIL_ADAPTER
    ORCHESTRATION --> SMS_ADAPTER
    ORCHESTRATION --> AI_ADAPTER
    
    EVENTS --> AUDIT
```

## Component Descriptions

### Presentation Layer
- **REST Controllers**: Handle HTTP requests, validate input, return responses
- **Thymeleaf Controllers**: Render HTML views, handle form submissions
- **DTOs**: Data transfer objects for API contracts
- **Validation**: Request validation using Spring Validation

### Application Layer
- **Command Handlers**: Handle write operations (CQRS)
- **Query Handlers**: Handle read operations (CQRS)
- **Application Services**: Orchestrate use cases, manage transactions
- **Event Publisher**: Publish domain events to message bus

### Domain Layer
- **Aggregates**: Consistency boundaries (Ledger, Journal, Account)
- **Entities**: Domain objects with identity
- **Value Objects**: Immutable values (Money, Currency, AccountNumber)
- **Domain Events**: Business events (PaymentReceived, EntryPosted)
- **Domain Services**: Business logic that doesn't belong to entities
- **Repository Interfaces**: Contracts for data access
- **Specifications**: Business rules encapsulation

### Infrastructure Layer
- **JPA Repositories**: PostgreSQL implementation of repositories
- **Flyway Migrations**: Database schema versioning
- **Security Config**: JWT authentication, authorization
- **Payment Gateway Adapter**: External payment system integration
- **Email Adapter**: Email service integration
- **SMS Adapter**: SMS service integration
- **AI Adapter**: AI service integration
- **Redis Cache**: Caching implementation
- **Audit Logger**: Audit trail implementation
