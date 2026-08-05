# Phase 0: Architecture - Architecture Decision Records (ADRs)

## ADR-001: Modular Monolith Architecture

### Status
Accepted

### Context
The Finance Ledger & Reconciliation microservice needs to balance development speed with future scalability. A full microservices architecture introduces complexity (distributed transactions, eventual consistency, network latency) that may not be justified initially. However, the business may require extracting modules into separate services in the future.

### Decision
Adopt a modular monolith architecture where each bounded context is an independent module within a single deployment. Modules communicate via in-memory calls and domain events. Each module has clear boundaries (domain, application, infrastructure, presentation layers) and can be extracted into a microservice without code changes.

### Consequences
**Positive:**
- Simplified development and testing (single deployment, no distributed transactions)
- Clear module boundaries enable future extraction
- Shared infrastructure reduces duplication
- Faster development cycles
- Easier debugging and monitoring

**Negative:**
- Single point of failure for entire system
- Shared database may become bottleneck
- Technology choices are shared across modules
- Scaling is vertical rather than horizontal per module

### Alternatives Considered
- **Microservices**: Rejected due to complexity overhead for initial development
- **Monolithic**: Rejected due to lack of clear boundaries for future extraction

---

## ADR-002: PostgreSQL as Primary Database

### Status
Accepted

### Context
The system requires ACID transactions for financial integrity, complex queries for reporting, and relational data for chart of accounts hierarchies. The database must support high transaction volumes and maintain data consistency.

### Decision
Use PostgreSQL 15+ as the primary database. PostgreSQL provides:
- ACID compliance with strong consistency guarantees
- Advanced indexing (B-tree, GiST, GIN) for performance
- JSON support for flexible schema evolution
- Full-text search capabilities
- Excellent replication and high availability features
- Strong community and commercial support

### Consequences
**Positive:**
- Strong consistency guarantees for financial transactions
- Complex query capabilities for reporting
- Mature tooling and monitoring
- Excellent performance with proper indexing
- Open source with no licensing costs

**Negative:**
- Vertical scaling limits (can be mitigated with read replicas)
- Requires database expertise for optimization
- Sharding is complex (may be needed at extreme scale)

### Alternatives Considered
- **MySQL**: Rejected due to less advanced indexing and JSON support
- **MongoDB**: Rejected due to lack of ACID guarantees for complex transactions
- **CockroachDB**: Rejected due to immaturity and smaller ecosystem

---

## ADR-003: Flyway for Database Migrations

### Status
Accepted

### Context
The database schema will evolve throughout development. We need a reliable, versioned migration system that works across environments (local, staging, production) and supports rollback capabilities.

### Decision
Use Flyway for database schema migrations. Flyway provides:
- SQL-based migrations for full control
- Versioned migration scripts with checksums
- Support for repeatable migrations
- Integration with Spring Boot
- Excellent tooling and community support
- Ability to validate migrations against production

### Consequences
**Positive:**
- Versioned schema evolution
- Reproducible deployments across environments
- Integration with Spring Boot lifecycle
- SQL-based for transparency and control
- Strong community support

**Negative:**
- SQL migrations require manual conflict resolution
- No automatic rollback (must write undo scripts)
- Schema changes require careful planning

### Alternatives Considered
- **Liquibase**: Rejected due to XML-based configuration (less transparent than SQL)
- **Manual scripts**: Rejected due to lack of versioning and automation

---

## ADR-004: Immutable Journal Entries

### Status
Accepted

### Context
Financial systems require complete audit trails. Journal entries must never be modified after creation to ensure the integrity of financial records and support regulatory compliance.

### Decision
Implement immutable journal entries. Once a journal entry is created and posted, it cannot be modified or deleted. Corrections must be made through reversing entries (contra entries) rather than modifying original entries.

### Consequences
**Positive:**
- Complete audit trail for compliance
- Prevents data tampering
- Simplifies audit and reconciliation
- Aligns with accounting best practices
- Supports regulatory requirements

**Negative:**
- Requires more storage (no updates)
- Corrections require reverse entries
- Query complexity for "current" state
- May confuse users expecting edit capability

### Alternatives Considered
- **Mutable entries with audit log**: Rejected due to risk of data tampering
- **Soft delete**: Rejected due to complexity and audit trail gaps

---

## ADR-005: BigDecimal for Monetary Values

### Status
Accepted

### Context
Financial calculations require exact precision. Floating-point types (double, float) introduce rounding errors that are unacceptable for financial systems. The system must handle currency calculations with perfect accuracy.

### Decision
Use `java.math.BigDecimal` for all monetary values. BigDecimal provides:
- Arbitrary precision arithmetic
- Exact decimal representation
- Control over rounding modes
- Immutable and thread-safe
- Standard for financial applications

### Consequences
**Positive:**
- Exact precision for financial calculations
- No floating-point rounding errors
- Standard practice in financial systems
- Immutable and thread-safe

**Negative:**
- More verbose API than primitives
- Performance overhead (negligible for financial operations)
- Requires careful handling of scale and rounding

### Alternatives Considered
- **Double/float**: Rejected due to rounding errors
- **Long (cents)**: Rejected due to loss of scale information and currency complexity
- **Joda Money**: Rejected due to additional dependency (BigDecimal is sufficient)

---

## ADR-006: UUID for Entity Identifiers

### Status
Accepted

### Context
The system will be distributed (modular monolith with potential microservice extraction). Sequential integer IDs can cause issues with distributed systems, expose business information, and complicate data migration.

### Decision
Use UUID (Universally Unique Identifier) for all entity primary keys. UUIDs provide:
- Global uniqueness across distributed systems
- No central coordination required
- No exposure of business information
- Simplifies data merging and migration
- Supports offline generation

### Consequences
**Positive:**
- No coordination needed for ID generation
- Supports distributed architecture
- No business information leakage
- Simplifies data migration
- Works well with modular monolith

**Negative:**
- Larger storage size (16 bytes vs 4/8 bytes)
- Non-sequential (can impact some query patterns)
- Less human-readable
- Index fragmentation (mitigated with UUID v7 or similar)

### Alternatives Considered
- **Sequential integers**: Rejected due to coordination requirements and business information exposure
- **Snowflake IDs**: Rejected due to complexity and coordination requirements
- **ULID**: Rejected due to less standardization than UUID

---

## ADR-007: JWT for Authentication

### Status
Accepted

### Context
The system provides both REST API and web UI. Authentication must work across both interfaces, support stateless scaling, and integrate with role-based authorization. The system may need to support external API clients.

### Decision
Use JWT (JSON Web Tokens) for authentication. JWT provides:
- Stateless authentication (no server-side session storage)
- Cross-platform compatibility
- Built-in claims for user identity and roles
- Support for token expiration and refresh
- Standard for REST APIs

### Consequences
**Positive:**
- Stateless scaling (no session replication needed)
- Works across REST API and web UI
- Standard and well-supported
- Supports external API clients
- Built-in expiration and refresh

**Negative:**
- Token revocation is complex (requires blacklist or short expiration)
- Larger payload than session IDs
- Must protect token storage (XSS prevention)
- Token size can be large with many claims

### Alternatives Considered
- **Session-based authentication**: Rejected due to stateful scaling requirements
- **OAuth 2.0**: Rejected due to complexity (JWT is sufficient for internal authentication)
- **API Keys**: Rejected for user authentication (suitable only for service-to-service)

---

## ADR-008: Application Events for Module Communication

### Status
Accepted

### Context
Modules in the modular monolith need to communicate without tight coupling. Direct method calls create dependencies that complicate future microservice extraction. Asynchronous communication is needed for cross-cutting concerns (notifications, audit, AI insights).

### Decision
Use Spring's Application Event mechanism for module communication. Modules publish domain events, and other modules subscribe to events they're interested in. This provides:
- Loose coupling between modules
- Asynchronous processing capability
- Clear event-driven architecture
- Easy transition to message brokers (RabbitMQ, Kafka) for microservices
- Support for transactional event publishing

### Consequences
**Positive:**
- Loose coupling between modules
- Supports future microservice extraction
- Asynchronous processing for non-critical operations
- Clear event-driven architecture
- Transactional event publishing

**Negative:**
- Eventual consistency for cross-module operations
- More complex debugging (event flow)
- Requires event versioning for schema evolution
- Event ordering guarantees are limited

### Alternatives Considered
- **Direct method calls**: Rejected due to tight coupling
- **Message broker (RabbitMQ/Kafka)**: Rejected due to complexity for initial development (can be added later)
- **Shared database**: Rejected due to tight coupling and lack of explicit contracts

---

## ADR-009: Hexagonal Architecture (Ports and Adapters)

### Status
Accepted

### Context
The system must isolate the core domain from external concerns (databases, web, external APIs). This ensures business logic remains testable and maintainable, and infrastructure can be replaced without affecting domain logic.

### Decision
Implement Hexagonal Architecture (Ports and Adapters). The domain defines ports (interfaces) for required capabilities, and infrastructure implements adapters that satisfy these interfaces. This provides:
- Clear separation between domain and infrastructure
- Domain has no dependencies on Spring or external libraries
- Easy testing of domain logic with mocks
- Infrastructure can be replaced without domain changes
- Supports DDD principles

### Consequences
**Positive:**
- Domain logic isolated from infrastructure
- Easy unit testing of domain
- Infrastructure can be swapped (e.g., PostgreSQL to MongoDB)
- Clear architectural boundaries
- Supports DDD and Clean Architecture

**Negative:**
- More layers and boilerplate
- Initial development overhead
- Requires discipline to maintain boundaries
- May seem over-engineered for simple features

### Alternatives Considered
- **Layered Architecture**: Rejected due to dependencies pointing toward domain
- **Onion Architecture**: Rejected due to complexity (Hexagonal is simpler)

---

## ADR-010: CQRS (Command Query Responsibility Segregation)

### Status
Accepted

### Context
The system has different requirements for read and write operations. Writes (journal posting, payments) require strong consistency and validation. Reads (reporting, queries) require performance and flexibility. The domain model is optimized for writes, but queries need different data shapes.

### Decision
Implement CQRS pattern with separate command and query handlers. Commands modify state and return simple acknowledgments. Queries read state and return rich DTOs. This provides:
- Optimized models for reads vs writes
- Clear separation of concerns
- Performance optimization (query models can be denormalized)
- Scalability (reads and writes can be scaled independently)
- Aligns with DDD aggregates

### Consequences
**Positive:**
- Optimized read/write models
- Clear separation of concerns
- Performance optimization potential
- Supports future read/write splitting
- Aligns with DDD principles

**Negative:**
- More code (separate handlers)
- Potential data duplication (read models)
- Complexity in keeping models synchronized
- May be overkill for simple operations

### Alternatives Considered
- **Traditional CRUD**: Rejected due to lack of optimization for different access patterns
- **Event Sourcing**: Rejected due to complexity for initial implementation (can be added later)

---

## ADR-011: Testcontainers for Integration Testing

### Status
Accepted

### Context
Integration tests require real database instances to validate behavior. Shared test databases cause flaky tests and data pollution. Mocking databases doesn't catch real integration issues (SQL dialect, constraints, performance).

### Decision
Use Testcontainers for integration testing. Testcontainers provides:
- Real database instances in Docker containers
- Isolated test environments
- Consistent test execution across machines
- Support for PostgreSQL, Redis, and other services
- Integration with JUnit 5

### Consequences
**Positive:**
- Real database behavior in tests
- Isolated test environments
- No shared test database issues
- Consistent across development environments
- Catches real integration issues

**Negative:**
- Slower test execution than mocks
- Requires Docker installed
- Higher resource usage
- May need test data seeding

### Alternatives Considered
- **Shared test database**: Rejected due to flaky tests and data pollution
- **H2 in-memory database**: Rejected due to SQL dialect differences
- **Mocking**: Rejected due to lack of real integration validation
