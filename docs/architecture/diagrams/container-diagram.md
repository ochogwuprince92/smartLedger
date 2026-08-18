# Container Diagram

## System Containers

```mermaid
graph TB
    subgraph "Finance Ledger & Reconciliation Microservice"
        WEB[Web Application<br/>Thymeleaf MVC<br/>Port: 8080]
        API[REST API<br/>Spring Boot<br/>Port: 8080]
        SCHEDULER[Scheduled Jobs<br/>Spring Scheduling]
        APP[Application Layer<br/>Orchestration]
        DOMAIN[Domain Layer<br/>Business Logic]
    end
    
    subgraph "Data Layer"
        POSTGRES[(PostgreSQL<br/>Primary Database)]
        REDIS[(Redis<br/>Cache & Sessions)]
    end
    
    subgraph "External Services"
        PG[Payment Gateway]
        BANK[Banking System]
        EMAIL[Email Service]
        SMS[SMS Service]
        AI[AI Service]
    end
    
    subgraph "Infrastructure"
        DOCKER[Docker<br/>Containerization]
        FLYWAY[Flyway<br/>Migrations]
        PROMETHEUS[Prometheus<br/>Monitoring]
        GRAFANA[Grafana<br/>Dashboards]
    end
    
    WEB --> APP
    API --> APP
    SCHEDULER --> APP
    
    APP --> DOMAIN
    APP --> POSTGRES
    APP --> REDIS
    
    DOMAIN --> POSTGRES
    
    POSTGRES --> FLYWAY
    APP --> PG
    APP --> BANK
    APP --> EMAIL
    APP --> SMS
    APP --> AI
    
    WEB --> PROMETHEUS
    API --> PROMETHEUS
    SCHEDULER --> PROMETHEUS
    
    PROMETHEUS --> GRAFANA
    
    WEB --> DOCKER
    API --> DOCKER
    SCHEDULER --> DOCKER
    POSTGRES --> DOCKER
    REDIS --> DOCKER
```

## Container Descriptions

### Web Application
- **Technology**: Spring Boot with Thymeleaf MVC
- **Port**: 8080
- **Responsibility**: Provides user interface for finance operations
- **Features**: Account management, journal entry forms, report viewing

### REST API
- **Technology**: Spring Boot REST
- **Port**: 8080
- **Responsibility**: Provides programmatic access for external systems
- **Features**: Payment processing, webhook endpoints, data export
- **Security**: JWT authentication, role-based authorization

### Scheduled Jobs
- **Technology**: Spring Scheduling
- **Responsibility**: Executes periodic tasks
- **Features**: Daily reconciliation, report generation, data cleanup

### Application Layer
- **Technology**: Spring Services
- **Responsibility**: Orchestrates use cases
- **Features**: Command/query handling, event publishing, transaction management

### Domain Layer
- **Technology**: Pure Java (no Spring dependencies)
- **Responsibility**: Core business logic
- **Features**: Aggregates, entities, value objects, domain services

### PostgreSQL
- **Version**: 15+
- **Responsibility**: Primary data store
- **Features**: ACID transactions, constraints, indexes
- **Migration**: Flyway

### Redis
- **Version**: 7+
- **Responsibility**: Cache and session store
- **Features**: Distributed caching, rate limiting, session management
