# RikkeiBank API - Microservices System

## Architecture Overview

This is a complete microservices banking system built with Spring Boot, Spring Cloud, and distributed system patterns.

### Services

1. **config-server** (Port 8888) - Spring Cloud Config Server
2. **discovery-service** (Port 8761) - Eureka Server
3. **api-gateway** (Port 8080) - Spring Cloud Gateway with JWT validation
4. **identity-service** (Port 8081) - JWT Authentication with Redis blacklist
5. **customer-service** (Port 8082) - Customer/Staff management (PostgreSQL)
6. **account-service** (Port 8083) - Account management with Redis caching (PostgreSQL)
7. **transaction-service** (Port 8084) - Saga orchestrator with Kafka + Circuit Breaker
8. **notification-service** (Port 8085) - WebFlux + Kafka consumer (MongoDB)

### Infrastructure

- PostgreSQL (for relational databases)
- MongoDB (for notification logs)
- Redis (for caching and token blacklist)
- Apache Kafka (for event-driven architecture)

### Prerequisites

- Java 17+
- Gradle 8.x
- Docker & Docker Compose

### Quick Start

1. Start infrastructure:
```bash
docker-compose up -d
```

2. Start services in order:
```bash
./gradlew :config-server:bootRun
./gradlew :discovery-service:bootRun
./gradlew :api-gateway:bootRun
./gradlew :identity-service:bootRun
./gradlew :customer-service:bootRun
./gradlew :account-service:bootRun
./gradlew :transaction-service:bootRun
./gradlew :notification-service:bootRun
```

### Role-Based Access Control (RBAC)

- **ADMIN**: Full CRUD access to customers, staff, account types, token revocation
- **TELLER**: View transactions and customer profiles
- **CUSTOMER**: View own accounts/balance, perform transfers
- **GUEST**: Only login, register, refresh token endpoints

### Key Patterns Implemented

- Database-per-service pattern
- Saga Orchestrator pattern for distributed transactions
- Event-driven architecture with Kafka
- Circuit Breaker pattern with Resilience4j
- Caching with Redis
- JWT with Redis blacklist
- Centralized exception handling
