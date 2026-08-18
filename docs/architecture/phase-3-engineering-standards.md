# Phase 3: Engineering Standards Configuration

## Overview
This document describes the engineering standards and configuration setup for the Finance Ledger & Reconciliation microservice. This phase establishes the foundation for development by configuring all necessary tools, dependencies, and infrastructure.

## Objectives
- Configure Spring Boot application with all necessary profiles
- Set up database migrations with Flyway
- Configure Docker for containerization
- Establish Jenkins CI/CD pipeline
- Set up code quality and testing tools
- Configure scheduled task infrastructure
- Create shared kernel foundation

## Technology Stack Configuration

### 1. Spring Boot Configuration
**Version**: 3.3.0
**Java Version**: 21

**Dependencies Configured**:
- spring-boot-starter-data-jpa
- spring-boot-starter-data-redis
- spring-boot-starter-security
- spring-boot-starter-web
- spring-boot-starter-thymeleaf
- spring-boot-starter-validation
- spring-boot-starter-actuator

### 2. Database Configuration
**Database**: PostgreSQL 15+
**Migration Tool**: Flyway 10.4.1
**Connection Pool**: HikariCP (default in Spring Boot)

**Database Schema Requirements**:
- UUID primary keys for all entities
- Audit fields (created_at, updated_at, created_by, updated_by)
- Soft delete support (deleted_at)
- Indexes on foreign keys and frequently queried fields
- Monetary columns using DECIMAL(19,4)

### 3. Cache Configuration
**Cache Provider**: Redis 7+
**Usage**:
- Session storage
- Query result caching
- Rate limiting
- Distributed locking

### 4. Code Quality Tools

#### JaCoCo (Code Coverage)
**Version**: 0.8.11
**Coverage Targets**:
- Domain layer: 95%
- Application layer: 90%
- Infrastructure layer: 80%
- Presentation layer: 75%

#### Spotless (Code Formatting)
**Version**: 2.43.0
**Style**: Google Java Format 1.19.2
**Rules**:
- Remove unused imports
- Trim trailing whitespace
- End with newline

#### Checkstyle (Code Style)
**Version**: 3.3.1
**Configuration**: Google Java Style Guide
**Severity**: Warning (non-blocking)

### 5. Testing Configuration
**Framework**: JUnit 5 (Jupiter)
**Mocking**: Mockito
**Integration Testing**: Testcontainers 1.19.3
**Security Testing**: spring-boot-starter-security-test

**Testcontainers Modules**:
- PostgreSQL
- JUnit Jupiter extension

### 6. Object Mapping
**Tool**: MapStruct 1.5.5.Final
**Purpose**: DTO to Entity mapping
**Configuration**: Annotation processor enabled in Maven compiler plugin

### 7. API Documentation
**Tool**: SpringDoc OpenAPI 2.3.0
**Features**:
- Swagger UI at /swagger-ui.html
- OpenAPI JSON at /v3/api-docs
- Automatic schema generation

### 8. Development Tools
**Spring Boot DevTools**: Live reload, configuration properties metadata
**Lombok**: Reduce boilerplate code

## Application Configuration

### Profile Structure
Three profiles are configured:
- **dev** (default): Development environment with H2 console enabled
- **test**: Testing environment with Testcontainers
- **prod**: Production environment with optimized settings

### Configuration Files
- `application.yml`: Base configuration
- `application-dev.yml`: Development overrides
- `application-test.yml`: Test configuration
- `application-prod.yml`: Production configuration

### Key Configuration Properties

#### Server Configuration
```yaml
server:
  port: 8080
  servlet:
    context-path: /
```

#### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartledger
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_schema: public
```

#### Redis Configuration
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 60000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

#### Flyway Configuration
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
```

#### Scheduled Tasks Configuration
```yaml
app:
  scheduled:
    enabled: true
    pool-size: 5
    reconciliation: "0 0 2 * * ?"
    report-generation: "0 0 6 * * MON"
    data-cleanup: "0 0 3 * * ?"
    ai-insights: "0 0 4 * * ?"
    health-check: "0 */15 * * * ?"
    notification-retry: "0 */5 * * * ?"
```

## Docker Configuration

### Dockerfile
Multi-stage build for optimized image size:
- **Stage 1**: Maven build
- **Stage 2**: Runtime image with JRE 21

### Docker Compose
Services:
- **smartledger**: Application service
- **postgres**: PostgreSQL database
- **redis**: Redis cache

## Jenkins Pipeline

### Pipeline Stages
1. **Build**: Compile and package
2. **Quality Checks**: Checkstyle, Spotless, JaCoCo
3. **Unit Tests**: Run unit tests
4. **Integration Tests**: Run Testcontainers tests
5. **Security Scan**: OWASP Dependency-Check
6. **Docker Build**: Build and push Docker image
7. **Deploy**: Deploy to environment

### Jenkinsfile Configuration
- Declarative pipeline syntax
- Docker agent for consistent build environment
- Environment-specific deployment
- Notification on failure

## Scheduled Task Infrastructure

### Scheduler Configuration
- Enable scheduling with `@EnableScheduling`
- Configure thread pool for parallel task execution
- Cron expressions defined in application.yml
- Each bounded context has its own scheduler package

### Scheduler Packages
- `configuration/scheduling`: Global scheduling configuration
- `reconciliation/infrastructure/scheduler`: Reconciliation jobs
- `reporting/infrastructure/scheduler`: Report generation jobs
- `common/infrastructure/scheduler`: Data cleanup jobs
- `ai/infrastructure/scheduler`: AI insight generation jobs
- `notification/infrastructure/scheduler`: Notification retry jobs

## Shared Kernel Foundation

### Base Classes to Create
1. **BaseEntity**: UUID ID, timestamps
2. **AuditableEntity**: Audit fields (created_by, updated_by)
3. **Money**: Value object for monetary amounts
4. **ApiResponse**: Standard API response wrapper
5. **ProblemDetail**: Error response structure
6. **ErrorCodes**: Enum of error codes
7. **Pagination**: Pagination support
8. **UuidGenerator**: UUID generation utility
9. **ClockProvider**: Time abstraction for testing
10. **SecurityContext**: Security context holder

### Package Structure
```
com.finance.smartLedger.shared/
├── entity/
│   ├── BaseEntity.java
│   └── AuditableEntity.java
├── valueobject/
│   └── Money.java
├── dto/
│   ├── ApiResponse.java
│   └── ProblemDetail.java
├── exception/
│   ├── ErrorCodes.java
│   └── BusinessException.java
├── util/
│   ├── Pagination.java
│   ├── UuidGenerator.java
│   └── ClockProvider.java
└── security/
    └── SecurityContext.java
```

## Project Structure Setup

### Main Application Class
Location: `src/main/java/com/finance/smartLedger/SmartLedgerApplication.java`

Features:
- `@SpringBootApplication` annotation
- `@EnableScheduling` for scheduled tasks
- `@EnableJpaAuditing` for audit support
- Main method for Spring Boot startup

### Resource Directory Structure
```
src/main/resources/
├── static/              # Static assets (CSS, JS, images)
├── templates/           # Thymeleaf templates
├── application.yml      # Base configuration
├── application-dev.yml  # Development profile
├── application-test.yml # Test profile
└── application-prod.yml # Production profile
```

### Migration Directory Structure
```
src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__create_audit_tables.sql
└── V3__create_shared_tables.sql
```

## Next Steps

After completing Phase 3, the following phases will be executed:
- **Phase 4**: Shared Kernel Implementation
- **Phase 5**: Security Implementation
- **Phase 6**: Core Domain Design
- **Phase 7**: Database Schema and Migrations
- **Phase 8**: TDD Feature Development

## Verification Checklist

- [ ] Application builds successfully with `mvn clean install`
- [ ] All profiles (dev, test, prod) are configured
- [ ] Flyway migrations run successfully
- [ ] Docker compose starts all services
- [ ] Jenkins pipeline executes all stages
- [ ] Code quality tools pass (Checkstyle, Spotless)
- [ ] Test coverage meets targets
- [ ] Scheduled tasks configuration is valid
- [ ] Shared kernel classes are created
- [ ] Application starts successfully on port 8080

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Flyway Documentation](https://flywaydb.org/documentation)
- [Testcontainers Documentation](https://testcontainers.com/)
- [MapStruct Documentation](https://mapstruct.org/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Jenkins Pipeline](https://www.jenkins.io/doc/book/pipeline/)
