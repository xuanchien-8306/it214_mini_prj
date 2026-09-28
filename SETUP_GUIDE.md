# RikkeiBank API - Setup Guide

## Prerequisites

- Java 17 or higher
- Gradle 8.x
- Docker & Docker Compose

## Quick Start

### 1. Start Infrastructure Services

```bash
docker-compose up -d
```

This will start:
- PostgreSQL databases (ports 5432, 5433, 5434, 5435)
- MongoDB (port 27017)
- Redis (port 6379)
- Kafka (port 9092)
- Zookeeper (port 2181)

### 2. Initialize Config Server Repository

The config-server expects a Git repository at `~/rikkei-bank-config`. For local development:

```bash
mkdir -p ~/rikkei-bank-config/configs
cp configs/*.yml ~/rikkei-bank-config/configs/
cd ~/rikkei-bank-config
git init
git add .
git commit -m "Initial config"
```

### 3. Start Services in Order

Open separate terminals for each service:

```bash
# Terminal 1 - Config Server
./gradlew :config-server:bootRun

# Terminal 2 - Discovery Service
./gradlew :discovery-service:bootRun

# Terminal 3 - API Gateway
./gradlew :api-gateway:bootRun

# Terminal 4 - Identity Service
./gradlew :identity-service:bootRun

# Terminal 5 - Customer Service
./gradlew :customer-service:bootRun

# Terminal 6 - Account Service
./gradlew :account-service:bootRun

# Terminal 7 - Transaction Service
./gradlew :transaction-service:bootRun

# Terminal 8 - Notification Service
./gradlew :notification-service:bootRun
```

### 4. Verify Services

- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080

## API Endpoints

### Authentication (Public)

```bash
# Register
POST http://localhost:8080/api/v1/auth/register
{
  "username": "customer1",
  "password": "password123",
  "email": "customer1@example.com",
  "role": "CUSTOMER"
}

# Login
POST http://localhost:8080/api/v1/auth/login
{
  "username": "customer1",
  "password": "password123"
}

# Refresh Token
POST http://localhost:8080/api/v1/auth/refresh
{
  "refreshToken": "<refresh_token>"
}
```

### Customer Management (Admin)

```bash
# Create Customer
POST http://localhost:8080/api/v1/customers
Authorization: Bearer <access_token>
{
  "fullName": "John Doe",
  "identityNumber": "123456789",
  "phoneNumber": "0123456789",
  "email": "john@example.com",
  "address": "123 Main St",
  "dateOfBirth": "1990-01-01"
}

# Get All Customers
GET http://localhost:8080/api/v1/customers
Authorization: Bearer <access_token>
```

### Account Management (Admin/Customer)

```bash
# Create Account Type
POST http://localhost:8080/api/v1/account-types
Authorization: Bearer <access_token>
{
  "code": "SAVINGS",
  "name": "Savings Account",
  "description": "Standard savings account"
}

# Create Account
POST http://localhost:8080/api/v1/accounts
Authorization: Bearer <access_token>
{
  "accountNumber": "ACC001",
  "customerId": 1,
  "accountTypeId": 1,
  "initialBalance": 1000.00,
  "currency": "USD"
}

# Get Account Balance
GET http://localhost:8080/api/v1/accounts/number/ACC001
Authorization: Bearer <access_token>
```

### Transactions (Customer)

```bash
# Transfer Money
POST http://localhost:8080/api/v1/transactions/transfer
Authorization: Bearer <access_token>
{
  "fromAccountNumber": "ACC001",
  "toAccountNumber": "ACC002",
  "amount": 100.00,
  "currency": "USD",
  "description": "Payment"
}

# Get Transaction History
GET http://localhost:8080/api/v1/transactions/account/ACC001
Authorization: Bearer <access_token>
```

### Notifications

```bash
# Get Notifications
GET http://localhost:8080/api/v1/notifications/account/ACC001
Authorization: Bearer <access_token>
```

## Architecture Highlights

### Database-Per-Service Pattern
- Each service has its own PostgreSQL database
- No cross-database queries allowed
- Services communicate via REST APIs and Kafka

### Saga Pattern for Distributed Transactions
- Transaction service orchestrates money transfer
- Step 1: Debit from source account
- Step 2: Credit to destination account
- If Step 2 fails, compensating transaction refunds Step 1
- Success events published to Kafka for notifications

### Circuit Breaker Pattern
- Resilience4j configured in transaction-service
- Protects against account-service failures
- States: CLOSED, OPEN, HALF_OPEN

### Caching Strategy
- Redis caching in account-service
- Cacheable: Account types, account details
- Cache eviction on updates

### Security
- JWT tokens with 15min access, 30day refresh
- Redis blacklist for revoked tokens
- Gateway validates tokens before routing
- User context forwarded via headers (X-User-Id, X-User-Roles)

## Troubleshooting

### Services won't start
- Ensure Docker containers are running: `docker-compose ps`
- Check port conflicts
- Verify database connections

### Config Server issues
- Ensure Git repository exists at `~/rikkei-bank-config`
- Check config-server logs

### Kafka connection issues
- Verify Kafka is running: `docker-compose logs kafka`
- Check bootstrap-servers configuration

### Redis connection issues
- Verify Redis is running: `docker-compose logs redis`
- Test connection: `redis-cli ping`

## Stopping Services

```bash
# Stop all services (Ctrl+C in each terminal)

# Stop infrastructure
docker-compose down

# Stop infrastructure and remove volumes
docker-compose down -v
```
