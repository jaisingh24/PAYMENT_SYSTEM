# 🧠 DESIGN.md — PAYMENT_SYSTEM

> This document explains the **why** behind every major architectural and engineering decision in this project. Not just what was built — but the reasoning, tradeoffs, and alternatives considered at each step.

---

## 📌 Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [High-Level Design](#2-high-level-design)
3. [Domain Model](#3-domain-model)
4. [Database Design](#4-database-design)
5. [API Design Principles](#5-api-design-principles)
6. [Authentication & Authorization Design](#6-authentication--authorization-design)
7. [Transaction System Design](#7-transaction-system-design)
8. [Concurrency Control — The Core Problem](#8-concurrency-control--the-core-problem)
9. [Redis Idempotency Design](#9-redis-idempotency-design)
10. [Error Handling Strategy](#10-error-handling-strategy)
11. [Layered Architecture — Why This Structure](#11-layered-architecture--why-this-structure)
12. [Key Design Decisions & Tradeoffs](#12-key-design-decisions--tradeoffs)
13. [What I Would Do Differently at Scale](#13-what-i-would-do-differently-at-scale)

---

## 1. Problem Statement

Build a backend system that allows users to:
- Create bank accounts and manage balances
- Perform financial transactions (deposit, withdraw, transfer)
- Do all of this **safely** — meaning no money should be created or destroyed due to software bugs, race conditions, or network failures

The core engineering challenge is not the CRUD — it's correctness under concurrency. Two users transferring money from the same account at the same millisecond must not result in incorrect balances. This is the problem the entire design is built around.

---

## 2. High-Level Design

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (REST Consumer)                   │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP/HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SPRING BOOT APPLICATION                       │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              SECURITY FILTER CHAIN                       │   │
│  │   JwtAuthenticationFilter → SecurityConfig → RBAC        │   │
│  └──────────────────────────┬───────────────────────────────┘   │
│                             │                                   │
│  ┌──────────┐  ┌──────────┐ │ ┌──────────────┐                  │
│  │   Auth   │  │ Account  │ │ │  Transaction │  ← Controllers   │
│  │Controller│  │Controller│ │ │  Controller  │                  │
│  └────┬─────┘  └────┬─────┘ │ └──────┬───────┘                  │
│       │             │       │        │                          │
│  ┌────▼─────┐  ┌────▼─────┐   ┌──────▼───────┐                  │
│  │   Auth   │  │ Account  │   │  Transaction │  ← Services      │
│  │ Service  │  │ Service  │   │  Service     │                  │
│  └────┬─────┘  └────┬─────┘   └──────┬───────┘                  │
│       │             │                │                          │
│       │    ┌────────┘                │                          │
│       │    │              ┌──────────┘                          │
│       ▼    ▼              ▼                                     │
│  ┌─────────────┐   ┌─────────────┐   ┌────────────┐            │
│  │    MySQL    │   │    MySQL    │   │   Redis    │            │
│  │  (Users +   │   │ (Accounts + │   │(Idempotency│            │
│  │   Auth)     │   │  Txn Hist.) │   │   Keys)    │            │
│  └─────────────┘   └─────────────┘   └────────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

**Request lifecycle:**
1. Client sends HTTP request with JWT in `Authorization` header
2. `JwtAuthenticationFilter` intercepts, validates token, sets `SecurityContext`
3. `SecurityConfig` enforces role-based rules
4. Controller receives validated request, delegates to Service
5. Service applies business logic, calls Repository with locking where needed
6. Repository interacts with MySQL via JPA/Hibernate
7. For transfers, Redis is checked/written for idempotency before any DB operation
8. Response flows back through Controller → Client

---

## 3. Domain Model

The system has four core entities and their relationships:

```
┌──────────────────┐         ┌──────────────────────┐
│      User        │         │       Account         │
│──────────────────│         │──────────────────────│
│ id (UUID)        │ 1     * │ id (UUID)             │
│ name             ├─────────┤ userId (FK)           │
│ email (unique)   │         │ accountType           │
│ password (BCrypt)│         │ (SAVINGS/CURRENT)     │
│ role             │         │ balance (BigDecimal)  │
└──────────────────┘         │ createdAt             │
                             └──────────┬───────────┘
                                        │ 1
                                        │
                                        │ *
                             ┌──────────▼───────────┐
                             │     Transaction       │
                             │──────────────────────│
                             │ id (UUID)             │
                             │ accountId (FK)        │
                             │ type                  │
                             │ (DEPOSIT/WITHDRAW/    │
                             │  TRANSFER)            │
                             │ amount (BigDecimal)   │
                             │ balanceAfter          │
                             │ relatedAccountId      │
                             │ createdAt             │
                             └──────────────────────┘
```

**Why UUID for IDs?**
Integers are sequential and predictable — a user can guess that account `id=101` exists by trying `id=100`. UUIDs are non-guessable, adding a layer of security through obscurity on top of the authorization checks.

**Why BigDecimal for money?**
`double` and `float` use binary floating-point representation. `0.1 + 0.2` in floating-point is `0.30000000000000004`. For money, this is unacceptable. `BigDecimal` gives exact decimal arithmetic — mandatory for any financial system.

**Why store `balanceAfter` in Transaction?**
Allows reconstructing account balance history without replaying all transactions. Also useful for auditing — you can verify at any point in time what the balance should have been.

---

## 4. Database Design

### Schema Overview

```sql
-- Users table
CREATE TABLE users (
    id          VARCHAR(36) PRIMARY KEY,  -- UUID
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,    -- BCrypt hash
    role        ENUM('USER', 'ADMIN') NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Accounts table
CREATE TABLE accounts (
    id           VARCHAR(36) PRIMARY KEY,
    user_id      VARCHAR(36) NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    balance      DECIMAL(19, 4) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Transactions table
CREATE TABLE transactions (
    id                 VARCHAR(36) PRIMARY KEY,
    account_id         VARCHAR(36) NOT NULL,
    type               ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER_IN', 'TRANSFER_OUT'),
    amount             DECIMAL(19, 4) NOT NULL,
    balance_after      DECIMAL(19, 4) NOT NULL,
    related_account_id VARCHAR(36),       -- counterparty for transfers
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
```

### Why `DECIMAL(19, 4)`?

`DECIMAL(19, 4)` stores up to 15 digits before the decimal and 4 digits after. This handles values up to ₹999,999,999,999,999.9999 — more than sufficient for any realistic banking scenario, with 4 decimal places for precision in currency sub-units.

### Indexing Strategy

```sql
-- Fast lookup by user (for "get my accounts")
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- Fast lookup of transaction history (most common read query)
CREATE INDEX idx_transactions_account_id ON transactions(account_id);

-- Compound index for paginated history sorted by time
CREATE INDEX idx_transactions_account_created ON transactions(account_id, created_at DESC);
```

Without the compound index, fetching transaction history for an account requires a full table scan — catastrophic at scale.

### Why MySQL over PostgreSQL?

Both are fully capable for this use case. MySQL was chosen because:
- Wider adoption in Indian product companies (Flipkart, Paytm, Razorpay all use MySQL heavily)
- `InnoDB` engine supports row-level locking — required for pessimistic locking
- Strong Spring Boot / Hibernate support out of the box

PostgreSQL would have been equally valid — it has slightly better support for advanced types and concurrent reads.

---

## 5. API Design Principles

### RESTful Resource Modeling

```
/api/auth/register     → Auth resource   (not /api/createUser)
/api/accounts          → Account resource
/api/accounts/{id}     → Specific account
/api/transactions/deposit    → Action on transaction resource
/api/transactions/{accountId} → Transaction history for account
```

Resources are nouns. Actions map to HTTP verbs (POST/GET/PUT/DELETE). This is not perfectly REST-pure (deposit/withdraw are verb-like) — but pragmatically, financial operations like deposit and withdraw are distinct enough as endpoints to justify naming clarity over strict REST purity.

### DTO Layer — Why Not Expose Entities Directly?

Entities are persistence objects. Exposing them directly as API responses creates several problems:

1. **Over-fetching** — the entity might have fields (password hash, internal IDs) that should never reach the client
2. **Tight coupling** — a DB schema change forces an API contract change
3. **Circular references** — JPA entities with bidirectional relationships cause infinite JSON serialization loops

DTOs decouple the API contract from the persistence model. Each layer owns its own data shape.

```
Request JSON → RequestDTO (validated) → Service → Entity (JPA) → DB
DB → Entity → Service → ResponseDTO → Response JSON
```

---

## 6. Authentication & Authorization Design

### Why JWT over Sessions?

| Aspect | Session-based | JWT-based |
|---|---|---|
| State | Server stores session | Stateless — server stores nothing |
| Scalability | Requires sticky sessions or shared session store | Any server can validate any token |
| Horizontal scaling | Hard | Easy |
| Revocation | Instant (delete session) | Harder (requires blacklist or short TTL) |

For a distributed/scalable backend, stateless JWT is the correct default. The tradeoff is that token revocation requires extra work (blacklisting in Redis or short expiry) — acknowledged as a future improvement.

### Filter Chain Flow

```
HTTP Request
     │
     ▼
OncePerRequestFilter (JwtAuthenticationFilter)
     │
     ├── Extract "Bearer <token>" from Authorization header
     │
     ├── JwtService.extractUsername(token)
     │       └── Parses JWT claims using signing key
     │
     ├── CustomUserDetailsService.loadUserByUsername(email)
     │       └── Queries DB for user, returns UserDetails
     │
     ├── JwtService.validateToken(token, userDetails)
     │       └── Checks: signature valid? not expired? username matches?
     │
     └── SecurityContextHolder.setAuthentication(...)
              └── Downstream code can call SecurityContextHolder.getContext()
                  to get the authenticated user identity
```

### Why BCrypt for Passwords?

BCrypt is specifically designed for password hashing — it's intentionally slow (configurable cost factor) and includes a salt automatically. This means:
- Rainbow table attacks are impossible (salt per password)
- Brute-force is extremely slow (cost factor = 10 means ~100ms per hash check)
- Even if the DB is compromised, recovering plaintext passwords is computationally infeasible

MD5 and SHA-256 are fast hash functions — wrong for passwords because they can be brute-forced quickly.

### Ownership Validation

JWT tells us **who** the user is. But we also need to verify they're operating on **their own** accounts. This check happens in the Service layer:

```java
// Pseudocode
Account account = accountRepository.findById(accountId);
if (!account.getUserId().equals(currentUserId)) {
    throw new UnauthorizedAccessException("You don't own this account");
}
```

This prevents a valid JWT user from operating on another user's account by guessing account IDs.

---

## 7. Transaction System Design

### Three Transaction Types

```
DEPOSIT:   External → Account
             └── Simply add amount to balance
                 Record DEPOSIT transaction

WITHDRAW:  Account → External
             └── Check balance >= amount
                 Deduct amount
                 Record WITHDRAW transaction

TRANSFER:  Account A → Account B
             └── Check A balance >= amount
                 Deduct from A (TRANSFER_OUT)
                 Add to B (TRANSFER_IN)
                 Both must succeed or both fail (atomicity)
```

### Atomicity of Transfers

A transfer is two balance updates. If the system crashes after deducting from Account A but before crediting Account B, money vanishes. This is solved with `@Transactional`:

```java
@Transactional
public void transfer(TransferRequest request) {
    Account from = accountRepository.findByIdWithLock(request.fromAccountId());
    Account to   = accountRepository.findByIdWithLock(request.toAccountId());

    validateBalance(from, request.amount());

    from.debit(request.amount());
    to.credit(request.amount());

    accountRepository.save(from);
    accountRepository.save(to);

    // Both saves succeed, or the entire transaction rolls back
    // No partial state is ever committed to the DB
}
```

If any exception is thrown inside `@Transactional`, Spring rolls back the entire DB transaction — both balance changes are reverted as if neither happened.

### Transaction History Design

Every financial operation records a `Transaction` entity:

```
Deposit ₹5000 to Account A:
  → Transaction { accountId: A, type: DEPOSIT, amount: 5000, balanceAfter: 15000 }

Transfer ₹1000 from Account A to Account B:
  → Transaction { accountId: A, type: TRANSFER_OUT, amount: 1000, balanceAfter: 14000, relatedAccountId: B }
  → Transaction { accountId: B, type: TRANSFER_IN,  amount: 1000, balanceAfter: 6000,  relatedAccountId: A }
```

Two transaction records for a transfer — one on each side. This gives each account a complete, accurate ledger of all money movements.

---

## 8. Concurrency Control — The Core Problem

### The Race Condition

This is the most critical engineering problem in any payment system.

**Scenario:** Account X has ₹1000. Two withdrawal requests of ₹800 arrive simultaneously.

```
Time →        T1                    T2
Thread A:     READ balance = 1000   
Thread B:                           READ balance = 1000
Thread A:     CHECK 1000 >= 800 ✅  
Thread B:                           CHECK 1000 >= 800 ✅
Thread A:     WRITE balance = 200   
Thread B:                           WRITE balance = 200  ← WRONG
                                    (should have seen 200, not 1000)

Result: ₹1600 withdrawn from a ₹1000 account. Money created from nothing.
```

### Solution: Pessimistic Locking

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdWithLock(@Param("id") UUID id);
```

This translates to `SELECT ... FOR UPDATE` in MySQL. The database engine holds a row-level lock on the account row until the transaction commits or rolls back.

```
Time →        T1                         T2
Thread A:     LOCK row X (acquires)      
Thread B:                                LOCK row X (BLOCKS — waits)
Thread A:     READ balance = 1000
Thread A:     CHECK 1000 >= 800 ✅
Thread A:     WRITE balance = 200
Thread A:     COMMIT → releases lock
Thread B:                                LOCK row X (now acquires)
Thread B:                                READ balance = 200
Thread B:                                CHECK 200 >= 800 ❌ → throws InsufficientBalanceException
Thread B:                                ROLLBACK

Result: Only ₹800 withdrawn. Balance correctly = ₹200. ✅
```

### Deadlock Prevention

Transfers lock two accounts. If Thread A locks Account 1 then Account 2, and Thread B locks Account 2 then Account 1 simultaneously — deadlock.

**Solution: Always lock in consistent order (by account ID).**

```java
// Always lock the account with the smaller ID first
UUID firstLock  = fromId.compareTo(toId) < 0 ? fromId : toId;
UUID secondLock = fromId.compareTo(toId) < 0 ? toId   : fromId;

Account first  = accountRepository.findByIdWithLock(firstLock);
Account second = accountRepository.findByIdWithLock(secondLock);
```

Now Thread A and Thread B will always contend for the same lock first — one waits, the other completes, no deadlock possible.

### Why Pessimistic and Not Optimistic Locking?

**Optimistic locking** uses a version column — reads are free, but on write, it checks if the version changed and retries if it did.

For payment systems, optimistic locking causes **retry storms** under high concurrency. Every failed transaction must be retried from scratch, increasing DB load at exactly the wrong time. Pessimistic locking serializes access — slower per operation, but predictable and correct under any concurrency level.

| | Pessimistic | Optimistic |
|---|---|---|
| Read performance | Slower (lock held) | Faster (no lock) |
| Write conflicts | Blocked (queued) | Retried (application-side) |
| Best for | High-contention writes (payments) | Low-contention reads (read-heavy apps) |
| Chosen here | ✅ | ❌ |

---

## 9. Redis Idempotency Design

### The Problem: Network Retries

Client sends a transfer request. Network times out. The request actually succeeded, but the client doesn't know. It retries. Without idempotency — the transfer executes twice.

### Solution Architecture

```
Client Request
  + idempotencyKey: "user-123-txn-1717834200"
       │
       ▼
TransactionService.transfer()
       │
       ├── redisTemplate.hasKey(idempotencyKey)?
       │       │
       │       ├── YES → return cached TransactionResponse (no DB touch)
       │       │
       │       └── NO  → proceed with transfer
       │                     │
       │                     ├── Acquire DB locks
       │                     ├── Execute transfer (ACID)
       │                     ├── Build TransactionResponse
       │                     └── redisTemplate.set(idempotencyKey, response, TTL=24h)
       │                              │
       │                              ▼
       └────────────────── Return TransactionResponse
```

### Key Design Decisions

**TTL = 24 hours:** Clients are unlikely to retry a request after 24 hours. Keeping keys longer wastes Redis memory. Shorter TTL risks re-processing a retry.

**What to store as value:** The full response object (serialized JSON). This way, the retry gets back the exact same response as the original — idempotent from the client's perspective.

**Idempotency scope:** Only applied to transfers, not deposits/withdrawals. Deposits and withdrawals are typically user-initiated single actions, not system-to-system calls that retry automatically. Transfers are the highest-risk operation for duplication.

**Why Redis and not MySQL?** Redis `GET`/`SET` operations are O(1) and sub-millisecond. Using MySQL for this check would add a DB query to every transfer — and the idempotency table would need aggressive cleanup. Redis TTL handles expiry automatically.

---

## 10. Error Handling Strategy

### Global Exception Handler

All exceptions are caught in one place — `GlobalExceptionHandler` annotated with `@ControllerAdvice`. No try-catch blocks in Controllers.

```
Business Exception Flow:
  Service throws InsufficientBalanceException
       │
       ▼
  GlobalExceptionHandler catches it
       │
       ▼
  Returns: HTTP 400 Bad Request
  {
    "error": "INSUFFICIENT_BALANCE",
    "message": "Account balance ₹200 is less than requested ₹800",
    "timestamp": "2024-01-15T10:30:00Z"
  }
```

### Exception Hierarchy

```
RuntimeException
    └── PaymentSystemException (base)
            ├── InsufficientBalanceException     → HTTP 400
            ├── AccountNotFoundException         → HTTP 404
            ├── UnauthorizedAccessException      → HTTP 403
            ├── DuplicateTransactionException    → HTTP 409
            └── InvalidTransactionException      → HTTP 422
```

### Why Custom Exceptions Over Generic Ones?

Generic `RuntimeException("Something went wrong")` forces the caller to parse string messages to understand the error. Custom exceptions:
- Carry structured context (account ID, requested amount, actual balance)
- Map cleanly to HTTP status codes
- Enable specific handling at different layers
- Make debugging dramatically faster

---

## 11. Layered Architecture — Why This Structure

```
Controller  →  Service  →  Repository  →  Database
```

Each layer has a single, clear responsibility:

| Layer | Responsibility | What it must NOT do |
|---|---|---|
| Controller | Parse HTTP request, validate input, return HTTP response | Business logic, DB calls |
| Service | Business rules, transaction management, locking | HTTP concerns, direct SQL |
| Repository | Data access (CRUD, custom queries) | Business logic |
| Entity | DB representation, JPA mapping | Business logic, HTTP concerns |
| DTO | Data transfer between layers / API boundary | Business logic, JPA annotations |

**Why does this matter?**

Imagine the business rule changes: "Withdrawals over ₹50,000 require OTP verification." With clean layers, this change lives entirely in `TransactionService`. The Controller, Repository, and Entity are untouched.

Without layering, this change might need edits in 5 different files — and could break unrelated functionality.

### Package-by-Feature, Not Package-by-Layer

```
# Package by layer (avoided):
com.bank.controllers/
com.bank.services/
com.bank.repositories/

# Package by feature (chosen):
com.bank.account/     (controller + service + repo + dto + entity)
com.bank.transaction/ (controller + service + repo + dto + entity)
com.bank.auth/        (controller + service + dto)
```

Package-by-feature groups everything related to a domain concept together. When you're working on account logic, all relevant files are in one place. This scales better as the codebase grows and maps naturally to a future microservices split — each package becomes a candidate service.

---

## 12. Key Design Decisions & Tradeoffs

| Decision | Choice Made | Alternative | Why This Choice |
|---|---|---|---|
| Locking strategy | Pessimistic | Optimistic | Higher contention use case; retries under load are dangerous |
| Auth mechanism | JWT | Session + Cookie | Stateless, scales horizontally without session store |
| Password hashing | BCrypt | SHA-256 / MD5 | BCrypt is intentionally slow; designed for passwords |
| ID type | UUID | Auto-increment int | Non-guessable; safe for use in URLs |
| Money type | BigDecimal | double / float | Exact decimal arithmetic; floating point loses precision |
| DB | MySQL | PostgreSQL | Row-level locking in InnoDB; industry standard in fintech |
| Idempotency store | Redis | DB table | O(1) lookup; TTL-based expiry built-in; no cleanup job needed |
| Architecture | Monolith | Microservices | Right size for current scope; premature microservices add ops overhead |
| Package structure | By feature | By layer | Better cohesion; natural microservices split boundary |

---

## 13. What I Would Do Differently at Scale

This section is intentional — it shows awareness of current limitations and a path forward.

### At 10x Traffic

**Add connection pooling tuning** — HikariCP (Spring Boot default) needs pool size calibrated to DB capacity. Default settings are conservative.

**Add caching for account reads** — Account balance is read far more than it's written. A Redis cache with write-through invalidation on every transaction would dramatically reduce MySQL read load.

**Add pagination everywhere** — `GET /api/transactions/{accountId}` currently returns all records. With 100K transactions per account, this is a timeout waiting to happen. `Pageable` support is a one-line change in Spring Data.

### At 100x Traffic

**Split into microservices:**
```
auth-service       → handles register/login/token
account-service    → handles account CRUD
transaction-service → handles all money movement
notification-service → sends alerts (Kafka consumer)
```

**Add Kafka for async events:**
```
transaction-service publishes: TransactionCompletedEvent
notification-service consumes: sends email/SMS
audit-service consumes: writes to immutable audit log
```

**Add an API Gateway:**
- Rate limiting per user (prevent abuse)
- Central auth validation (remove from each service)
- Request routing
- SSL termination

### At 1000x Traffic

**Database sharding** — shard accounts by user ID range across multiple MySQL clusters. Cross-shard transfers become distributed transactions (much harder problem).

**CQRS (Command Query Responsibility Segregation)** — separate write path (MySQL, strongly consistent) from read path (Elasticsearch or read replicas, eventually consistent). Transaction history queries hit the read replica, freeing the primary for writes.

**Distributed tracing** — OpenTelemetry + Jaeger to trace a single transfer request across multiple services. Impossible to debug distributed systems without it.

---

> This document represents the engineering thought process behind PAYMENT_SYSTEM — a system where correctness isn't optional, and every design choice has a reason.

**Author:** Jai Singh  — [github.com/jaisingh24](https://github.com/jaisingh24/PAYMENT_SYSTEM)
