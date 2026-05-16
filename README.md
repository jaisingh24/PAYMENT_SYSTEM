# 💳 PAYMENT_SYSTEM

> A production-grade distributed payment and banking backend — built with Java Spring Boot, engineered for safety, scale, and concurrency.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=java)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red?logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://docs.docker.com/compose/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Key Features](#-key-features)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Security Design](#-security-design)
- [Concurrency & ACID](#-concurrency--acid-guarantees)
- [Redis Idempotency](#-redis-idempotency)
- [Testing](#-testing)
- [Future Roadmap](#-future-roadmap)

---

## 🧭 Overview

**PAYMENT_SYSTEM** is a backend-focused financial platform simulating a real-world banking system. It handles user authentication, multi-account management, and concurrent money transfers with strict ACID guarantees — all in a Dockerized, production-style setup.

This project is designed to demonstrate senior-level backend engineering: not just making things work, but making them correct, safe, and scalable under load.

**Core design goals:**
- Zero double-spending — even under concurrent requests
- Stateless, JWT-secured APIs
- Duplicate transaction protection via Redis idempotency keys
- Clean layered architecture following domain-driven design principles

---

## 🏗 Architecture

```
┌──────────────────────────────────────────────────────┐
│                     Client / API Consumer             │
└────────────────────────┬─────────────────────────────┘
                         │ HTTPS
                         ▼
┌──────────────────────────────────────────────────────┐
│              Spring Boot Application                  │
│                                                       │
│   ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│   │  Auth Layer │  │ Account APIs │  │  Txn APIs  │  │
│   │  (JWT/BCrypt│  │  (CRUD, Bal) │  │(Dep/Wth/Tr)│  │
│   └──────┬──────┘  └──────┬───────┘  └─────┬──────┘  │
│          └────────────────┼────────────────┘          │
│                           ▼                           │
│              ┌────────────────────────┐               │
│              │   Service Layer        │               │
│              │  (Business Logic +     │               │
│              │   Pessimistic Locking) │               │
│              └────────┬───────────────┘               │
│                       │                               │
│          ┌────────────┼───────────────┐               │
│          ▼            ▼               ▼               │
│       MySQL         Redis         JPA/Hibernate       │
│    (Persistence)  (Idempotency)   (ORM Layer)         │
└──────────────────────────────────────────────────────┘
```

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security, JWT, BCrypt |
| ORM | Spring Data JPA, Hibernate |
| Primary DB | MySQL 8 |
| Cache / Idempotency | Redis 7 |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| API Docs | Swagger / OpenAPI 3 |
| Testing | JUnit 5, Mockito |

---

## ✨ Key Features

### 🔐 Authentication & Security
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control (RBAC)
- Custom `JwtAuthenticationFilter` in the Spring Security filter chain
- Protected endpoints with token validation on every request

### 👤 User Management
- Register and login with email/password
- Secure profile management tied to JWT identity

### 🏦 Account Management
- Create SAVINGS / CURRENT accounts
- Real-time balance tracking
- Account ownership validation — users can only access their own accounts

### 💸 Transaction System
- Deposit, Withdraw, Transfer money
- Full transaction history per account
- Pre-transaction balance consistency checks
- Atomic rollback on failure

### ⚙️ Concurrency & ACID Guarantees
- Pessimistic locking (`SELECT ... FOR UPDATE`) on account rows during writes
- Prevents double-spending in concurrent transfer scenarios
- Full ACID compliance via Spring `@Transactional`
- Automatic rollback on runtime exceptions

### 🔁 Redis Idempotency
- Clients submit a unique `idempotencyKey` per transfer
- Redis stores processed keys with TTL — duplicate requests return cached results
- Safe for retry logic without re-executing transactions

### 🧪 Validation & Error Handling
- Jakarta Bean Validation on all request DTOs
- Global `@ControllerAdvice` exception handler
- Structured error responses with HTTP status codes
- Custom exceptions: `InsufficientBalanceException`, `UnauthorizedAccessException`, etc.

---

## 📁 Project Structure

```
src/main/java/com/bank/PAYMENT_SYSTEM/
│
├── account/
│   ├── controller/       # REST endpoints for account operations
│   ├── dto/              # Request/Response DTOs
│   ├── entity/           # Account JPA entity
│   ├── repository/       # Spring Data JPA repository
│   └── service/          # Business logic, locking, balance management
│
├── auth/
│   ├── controller/       # Register & Login endpoints
│   ├── dto/              # Auth request/response models
│   └── service/          # Auth orchestration, token generation
│
├── security/
│   ├── JwtAuthenticationFilter.java   # Filter chain integration
│   ├── JwtService.java                # Token generation & validation
│   ├── SecurityConfig.java            # Security rules & CORS config
│   └── CustomUserDetailsService.java  # DB-backed user loading
│
├── transaction/
│   ├── controller/       # Deposit, Withdraw, Transfer, History endpoints
│   ├── dto/              # Transaction request/response models
│   ├── entity/           # Transaction JPA entity
│   ├── repository/       # Transaction data access
│   └── service/          # Core transaction logic with locking + idempotency
│
├── user/
│   ├── controller/       # User profile endpoints
│   ├── entity/           # User JPA entity
│   ├── repository/       # User data access
│   └── service/          # User management logic
│
└── exception/
    ├── GlobalExceptionHandler.java    # Centralized error handling
    └── custom/                        # Domain-specific exception classes
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Clone the Repository

```bash
git clone https://github.com/jaisingh24/PAYMENT_SYSTEM.git
cd PAYMENT_SYSTEM
```

### Option 1: Run with Docker (Recommended)

Spins up the app, MySQL, and Redis in one command:

```bash
docker compose up --build
```

### Option 2: Run Locally

Make sure MySQL and Redis are running locally, then:

```bash
./mvnw spring-boot:run
```

### Swagger UI

Once running, explore all APIs interactively:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📡 API Reference

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | Register a new user | ❌ |
| `POST` | `/api/auth/login` | Login and receive JWT | ❌ |

**Register Request**
```json
{
  "name": "Virat Singh",
  "email": "virat@gmail.com",
  "password": "password123",
  "role": "USER"
}
```

**Login Response**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### Account Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/accounts` | Create a bank account | ✅ |
| `GET` | `/api/accounts/{accountId}` | Get account details | ✅ |

**Create Account Request**
```json
{
  "userId": "5502c958-a1f3-45fa-971c-4709978840bf",
  "accountType": "SAVINGS",
  "initialBalance": 10000
}
```

---

### Transactions

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/transactions/deposit` | Deposit money | ✅ |
| `POST` | `/api/transactions/withdraw` | Withdraw money | ✅ |
| `POST` | `/api/transactions/transfer` | Transfer between accounts | ✅ |
| `GET` | `/api/transactions/{accountId}` | Get transaction history | ✅ |

**Transfer Request** *(includes idempotency key)*
```json
{
  "fromAccountId": "5502c958-a1f3-45fa-971c-4709978840bf",
  "toAccountId": "8802c958-a1f3-45fa-971c-4709978840aa",
  "amount": 1500,
  "idempotencyKey": "txn-unique-key-12345"
}
```

> All protected endpoints require `Authorization: Bearer <JWT_TOKEN>` in the request header.

---

## 🔒 Security Design

```
Incoming Request
      │
      ▼
JwtAuthenticationFilter
  ├── Extract token from Authorization header
  ├── Validate signature & expiry via JwtService
  ├── Load user from CustomUserDetailsService
  └── Set SecurityContext
      │
      ▼
SecurityConfig (Role-based rules)
      │
      ▼
Controller → Service (ownership validation)
```

Passwords are hashed with **BCrypt** before persistence. JWTs are signed with a secret key and carry user identity + roles as claims. No session state is stored server-side.

---

## ⚡ Concurrency & ACID Guarantees

The trickiest problem in payment systems is concurrent transfers — two threads modifying the same account at the same time. Here's how this is solved:

```
Thread A: Transfer ₹500 from Account X
Thread B: Transfer ₹500 from Account X (simultaneously)

Without locking:
  Both read balance = ₹1000
  Both deduct ₹500
  Both write ₹500  ← WRONG, balance should be ₹0

With Pessimistic Locking:
  Thread A acquires lock → reads ₹1000, writes ₹500, releases lock
  Thread B waits → acquires lock → reads ₹500, writes ₹0, releases lock ✅
```

Implementation:
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` on repository queries for balance-modifying operations
- `@Transactional` wraps all transaction operations — any failure triggers a full rollback
- Consistent lock ordering (lower ID first) prevents deadlocks in bi-directional transfers

---

## 🔁 Redis Idempotency

Network failures can cause clients to retry requests. Without idempotency, a retry could create a duplicate transaction. Here's how it's handled:

```
Client sends Transfer + idempotencyKey: "txn-abc-123"
       │
       ▼
Check Redis: key "txn-abc-123" exists?
  ├── YES → Return cached response (no DB write) ✅
  └── NO  → Execute transaction → Store key in Redis with TTL → Return response ✅
```

This makes transfers **safe to retry** without any risk of money being deducted twice.

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw verify
```

Test coverage includes:

| Layer | Approach |
|---|---|
| Service Layer | JUnit 5 + Mockito — isolates business logic |
| Controller Layer | MockMvc — tests HTTP contract, validation, auth |
| Integration | Spring Boot Test — full context, real DB interactions |
| Concurrency | Multi-threaded test scenarios for double-spend prevention |

---

## 🗺 Future Roadmap

| Feature | Priority | Notes |
|---|---|---|
| Microservices decomposition | High | Split auth, accounts, transactions into separate services |
| Kafka event streaming | High | Async transaction events for downstream consumers |
| API Gateway | High | Rate limiting, routing, auth offloading |
| Distributed tracing | Medium | OpenTelemetry + Jaeger/Zipkin |
| CI/CD pipeline | Medium | GitHub Actions → Docker Hub → K8s deploy |
| Monitoring & alerting | Medium | Prometheus + Grafana dashboards |
| Notification service | Low | Email/SMS alerts on transactions |

---

## 👤 Author

**Jai Singh**

[![GitHub](https://img.shields.io/badge/GitHub-jaisingh24-181717?logo=github)](https://github.com/jaisingh24/PAYMENT_SYSTEM)

---

> Built to demonstrate production-quality backend engineering — not just a CRUD app, but a system designed for correctness under pressure.
