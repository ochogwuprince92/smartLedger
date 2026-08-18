# Finance Ledger & Reconciliation Microservice

smartLedger is a production-grade, modular monolith microservice for financial accounting, designed primarily for school bursary systems and fee portals, with applicability to SMEs, SaaS platforms, payment service providers, and non-profit organizations.

## Overview

This microservice provides a robust accounting engine with double-entry bookkeeping, automated reconciliation, comprehensive audit trails, and AI-powered financial insights. Built following Clean Architecture, Hexagonal Architecture, and Domain-Driven Design principles.

## Target Audience

### Primary
- **School Bursary Systems**: Educational institutions requiring robust fee collection, payment tracking, and financial reconciliation
- **Fee Portals**: Online payment platforms processing high volumes of fee payments with automated reconciliation

### Secondary
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
- **Java 21**
- **Spring Boot 3.3.0**
- **PostgreSQL 15+**
- **Flyway** (Database migrations)
- **Redis** (Caching and sessions)
- **Docker** (Containerization)
- **Spring Security with JWT** (Authentication and authorization)
- **Thymeleaf** (MVC for web UI)
- **Testcontainers** (Integration testing)
- **MapStruct** (Object mapping)
- **OpenAPI/SpringDoc** (API documentation)

### Bounded Contexts
The system is organized into 10 independent modules:

1. **Payment** - Payment processing, gateway integration, webhook handling
2. **Ledger** - Chart of accounts, account balances, accounting equation
3. **Journal** - Immutable journal entries, double-entry validation
4. **Reconciliation** - Transaction matching, variance detection
5. **Reporting** - Financial reports (Balance Sheet, Income Statement, Cash Flow)
6. **Security** - Authentication, authorization, role management (cross-cutting)
7. **Audit** - Audit trail, change tracking (cross-cutting)
8. **Notification** - Email/SMS notifications
9. **Receipt** - PDF receipt generation and delivery
10. **AI Insight** - Anomaly detection, cash flow forecasting, recommendations

## Features

### Core Features
- **Double-Entry Ledger Engine**: Ensures accounting equation integrity (Assets = Liabilities + Equity)
- **Chart of Accounts Management**: Hierarchical account structure with type validation
- **Immutable Journal Entries**: Complete audit trail with no modifications after posting
- **Automated Reconciliation**: Match payments with journal entries, detect variances
- **Financial Reporting**: Balance Sheet, Income Statement, Cash Flow, Trial Balance
- **Payment Processing**: Integration with Stripe, PayPal, and other payment gateways
- **Webhook Handling**: HMAC verification, replay protection, async processing
- **Receipt Generation**: PDF receipts with email delivery
- **Audit Trail**: Complete change tracking for compliance
- **AI Insights**: Anomaly detection, cash flow forecasting, recommendations

### Security Features
- JWT-based authentication
- Role-based access control (RBAC)
- Role hierarchy
- Permission-based authorization
- HMAC verification for webhooks
- Replay attack prevention
- Encryption at rest and in transit

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
│   │   │       ├── security/         # Security configuration
│   │   │       ├── audit/            # Audit logging
│   │   │       ├── shared/           # Shared kernel
│   │   │       ├── payment/          # Payment context
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

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+
- Docker (optional, for containerized deployment)

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

The application will start on port 8080.

### 6. Access the Application

- **REST API**: http://localhost:8080/api
- **Web UI**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator

### 7. Email Configuration (Local Development)

To enable email functionality in local development, you need to configure Gmail SMTP settings with an App Password:

#### Step 1: Enable 2FA on your Google Account
1. Go to https://myaccount.google.com/security
2. Enable Two-Factor Authentication (2FA)

#### Step 2: Generate an App Password
1. Go to https://myaccount.google.com/apppasswords
2. Select "Mail" and "Other (Custom name)"
3. Enter "SmartLedger Local Dev" as the name
4. Google will generate a 16-character password like: `abcd efgh ijkl mnop`

#### Step 3: Create application-local.yml
Create `src/main/resources/application-local.yml` (git-ignored):

```yaml
spring:
  profiles:
    active: local

  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-gmail-address@gmail.com}
    password: ${MAIL_PASSWORD:your-gmail-app-password}
    protocol: smtp
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

app:
  email:
    enabled: true
    from: ${MAIL_USERNAME:your-gmail-address@gmail.com}
    from-name: SmartLedger Local Dev
```

#### Step 4: Set Environment Variables
**Windows PowerShell:**
```powershell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="abcdefghijklmnop"  # 16-char app password, no spaces
```

**Linux/Mac:**
```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="abcdefghijklmnop"  # 16-char app password, no spaces
```

#### Step 5: Run with Local Profile
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**IMPORTANT NOTES:**
- Use the 16-character App Password, NOT your regular Gmail password
- Google removed "less secure app access" - regular passwords will fail
- Remove spaces from the app password when setting environment variables
- Email is disabled by default in base configuration (`app.email.enabled: false`)

## Docker Deployment

### Using Docker Compose

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Redis cache
- Finance Ledger microservice

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

Coverage targets:
- Domain layer: 95%
- Application layer: 90%
- Infrastructure layer: 80%

## API Documentation

OpenAPI/SpringDoc documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Configuration Profiles

### Development (default)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Test
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

### Production
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Code Quality

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

- **Build**: Compiles the project and runs unit tests
- **Quality Checks**: Runs Checkstyle, Spotless, and JaCoCo coverage reports
- **Integration Tests**: Executes integration tests with Testcontainers
- **Security Scan**: Runs dependency vulnerability checks
- **Docker Build**: Builds Docker image and pushes to registry
- **Deploy**: Deploys to staging/production environments

### Jenkins Configuration

1. Install required Jenkins plugins:
   - Pipeline
   - Docker Pipeline
   - Git
   - Maven Integration
   - JaCoCo
   - OWASP Dependency-Check

2. Configure Jenkins credentials for:
   - Git repository access
   - Docker registry
   - Database connections (for integration tests)
   - Notification services (email, Slack)

3. Create a Multibranch Pipeline job pointing to your Git repository

### Manual Jenkins Build

To trigger a build manually from Jenkins:
```bash
# Or use Jenkins CLI
java -jar jenkins-cli.jar -s http://jenkins-server:8080 build <job-name>
```

### Environment Variables

Configure these environment variables in Jenkins:
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_REDIS_HOST` - Redis host
- `SPRING_REDIS_PORT` - Redis port
- `DOCKER_REGISTRY` - Docker registry URL
- `DOCKER_CREDENTIALS_ID` - Jenkins credentials ID for Docker registry

## Scheduled Tasks (Cron Jobs)

### Overview

The system uses Spring's `@Scheduled` annotation for background task execution. Scheduled tasks are essential for:

- **Daily Reconciliation**: Automatic matching of payments with journal entries (FR-005)
- **Report Generation**: Scheduled financial reports (Balance Sheet, Income Statement, Cash Flow)
- **Data Cleanup**: Removal of temporary data and archival of old records
- **AI Insight Generation**: Periodic anomaly detection and cash flow forecasting
- **System Health Monitoring**: Health checks and metric collection
- **Notification Retries**: Retry failed email/SMS notifications

### Scheduled Tasks Configuration

Scheduled tasks are configured in `src/main/resources/application.yml`:

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 5
  cron:
    reconciliation: "0 0 2 * * ?"  # Daily at 2:00 AM
    report-generation: "0 0 6 * * MON"  # Every Monday at 6:00 AM
    data-cleanup: "0 0 3 * * ?"  # Daily at 3:00 AM
    ai-insights: "0 0 4 * * ?"  # Daily at 4:00 AM
    health-check: "0 */15 * * * ?"  # Every 15 minutes
    notification-retry: "0 */5 * * * ?"  # Every 5 minutes
```

### Cron Job Examples

The system includes the following scheduled jobs:

1. **Daily Reconciliation Job**
   - Schedule: `0 0 2 * * ?` (2:00 AM daily)
   - Purpose: Match payments with journal entries, detect variances
   - Location: `src/main/java/com/finance/smartLedger/reconciliation/infrastructure/scheduler/ReconciliationScheduler.java`

2. **Report Generation Job**
   - Schedule: `0 0 6 * * MON` (6:00 AM every Monday)
   - Purpose: Generate weekly financial reports
   - Location: `src/main/java/com/finance/smartLedger/reporting/infrastructure/scheduler/ReportScheduler.java`

3. **Data Cleanup Job**
   - Schedule: `0 0 3 * * ?` (3:00 AM daily)
   - Purpose: Clean temporary data, archive old records
   - Location: `src/main/java/com/finance/smartLedger/common/infrastructure/scheduler/DataCleanupScheduler.java`

4. **AI Insight Generation Job**
   - Schedule: `0 0 4 * * ?` (4:00 AM daily)
   - Purpose: Generate financial insights and anomaly detection
   - Location: `src/main/java/com/finance/smartLedger/ai/infrastructure/scheduler/AIInsightScheduler.java`

5. **Health Check Job**
   - Schedule: `0 */15 * * * ?` (Every 15 minutes)
   - Purpose: Monitor system health and metrics
   - Location: `src/main/java/com/finance/smartLedger/configuration/HealthCheckScheduler.java`

6. **Notification Retry Job**
   - Schedule: `0 */5 * * * ?` (Every 5 minutes)
   - Purpose: Retry failed email/SMS notifications
   - Location: `src/main/java/com/finance/smartLedger/notification/infrastructure/scheduler/NotificationRetryScheduler.java`

### Enabling Scheduling

To enable scheduled tasks, add the `@EnableScheduling` annotation to the main application class:

```java
@SpringBootApplication
@EnableScheduling
public class SmartLedgerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartLedgerApplication.class, args);
    }
}
```

### Monitoring Scheduled Tasks

- View scheduled task execution logs in the application logs
- Monitor task execution metrics via Actuator endpoints: `http://localhost:8080/actuator/metrics`
- Use Spring Boot Admin for visual monitoring of scheduled tasks

### Cron Expression Reference

| Field | Values | Special Characters |
|-------|--------|-------------------|
| Seconds | 0-59 | `, - * /` |
| Minutes | 0-59 | `, - * /` |
| Hours | 0-23 | `, - * /` |
| Day of Month | 1-31 | `, - * / ? L W` |
| Month | 1-12 or JAN-DEC | `, - * /` |
| Day of Week | 1-7 or SUN-SAT | `, - * / ? L #` |
| Year (optional) | 1970-2099 | `, - * /` |

Example cron expressions:
- `0 0 12 * * ?` - Every day at noon
- `0 15 10 ? * MON-FRI` - Monday to Friday at 10:15 AM
- `0 0/5 * * * ?` - Every 5 minutes
- `0 0 8,12,18 * * ?` - Daily at 8 AM, 12 PM, and 6 PM

## Architecture Documentation

Detailed architecture documentation is available in the `docs/architecture/` directory:

- **Phase 0**: Vision, Product Requirements, ADRs, Ubiquitous Language, Bounded Contexts
- **Phase 1**: Solution Architecture Diagrams (Context, Container, Component, Package, Sequence)
- **Phase 2**: Project Scaffold Structure

## Development Guidelines

### Domain Layer
- No dependencies on Spring or external frameworks
- All monetary values use `BigDecimal`
- All entities use `UUID` for identity
- Pure Java with business logic only

### Application Layer
- Orchestrates use cases
- Manages transactions
- Publishes domain events
- No business logic (delegates to domain)

### Infrastructure Layer
- Implements interfaces defined in domain layer
- Isolates external dependencies
- No business logic

### Presentation Layer
- Handles validation
- Converts between DTOs and domain objects
- No business logic

## Contributing

1. Follow the architectural principles (SOLID, Clean Architecture, DDD)
2. Write tests before implementation (TDD)
3. Maintain test coverage targets
4. Follow ubiquitous language in code
5. Update documentation for changes
6. Ensure all checks pass before submitting PR

## License

[Specify your license here]

## Support

For issues, questions, or contributions, please contact [contact information].

## Roadmap

- [ ] Phase 3: Engineering Standards Configuration
- [ ] Phase 4: Shared Kernel Implementation
- [ ] Phase 5: Security Implementation
- [ ] Phase 6: Core Domain Design
- [ ] Phase 7: Database Schema and Migrations
- [ ] Phase 8: TDD Feature Development (Chart of Accounts → AI Insight)

## Acknowledgments

Built with:
- Spring Boot
- PostgreSQL
- Redis
- Docker
- And many other open-source libraries
