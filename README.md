# Secure Banking Backend System

A production-style banking/payment backend system built using Java, Spring Boot, MySQL, Spring Security, and JWT authentication.

This project focuses on:

* secure authentication
* banking account management
* money transfer logic
* ACID transaction handling
* scalable backend architecture
* real-world fintech concepts

---

# Tech Stack

## Backend

* Java 17+
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* JWT Authentication

## Database

* MySQL
* Redis (planned)

## Build Tool

* Maven

---

# Current Features

## Authentication Module

* User Registration
* User Login
* BCrypt Password Hashing
* JWT Token Generation
* JWT Validation
* Stateless Authentication
* Protected APIs

## User Module

* User Entity
* Role Support
* User-Account Relationship

## Account Module

* Create Bank Account
* Generate Unique Account Number
* Get Account Details
* Freeze Account
* Unfreeze Account
* Balance Management

## Transaction Module

* Deposit Money
* Transfer Money
* Transaction Persistence
* Transaction References
* Insufficient Balance Validation
* Frozen Account Validation
* ACID Transactions using @Transactional

## Security Module

* Spring Security Configuration
* JWT Authentication Filter
* Protected Routes
* Stateless Session Management

## Exception Handling

* Custom Exceptions
* Global Exception Handler

---

# Features Planned Next

## High Priority

* Pessimistic Locking
* Concurrency Handling
* Double Spending Prevention
* Withdraw Feature
* Transaction History
* Idempotency

## Medium Priority

* Redis Integration
* API Validation
* Swagger/OpenAPI
* Dockerization
* Audit Logging

## Final Phase

* Unit Testing
* Integration Testing
* Concurrent Transfer Testing
* Admin Module
* Production Deployment

---

# Current Project Status

| Module             | Completion |
| ------------------ | ---------- |
| User Module        | 75%        |
| Auth Module        | 90%        |
| Security Module    | 85%        |
| JWT Module         | 95%        |
| Account Module     | 80%        |
| Transaction Module | 65%        |
| ACID Transactions  | 60%        |
| Exception Handling | 55%        |
| Redis              | 10%        |
| Locking            | 0%         |
| Idempotency        | 0%         |
| Testing            | 0%         |

Overall backend completion:

```text
~55% completed
```

---

# Project Architecture

```text
src/main/java/com/bank/PAYMENT_SYSTEM
│
├── account
├── auth
├── common
├── exception
├── security
├── transaction
└── user
```

---

# Database Tables

Current tables:

```text
users
accounts
transactions
```

---

# API Endpoints

## Authentication APIs

### Register User

```http
POST /auth/register
```

### Login User

```http
POST /auth/login
```

---

## Account APIs

### Create Account

```http
POST /accounts
```

### Get Account

```http
GET /accounts/{id}
```

### Freeze Account

```http
PUT /accounts/{id}/freeze
```

### Unfreeze Account

```http
PUT /accounts/{id}/unfreeze
```

---

## Transaction APIs

### Deposit Money

```http
POST /transactions/deposit
```

### Transfer Money

```http
POST /transactions/transfer
```

---

# Sample Request Bodies

## Register

```json
{
  "fullName": "Jai Singh",
  "email": "jai@gmail.com",
  "phoneNumber": "9876543210",
  "password": "password123"
}
```

---

## Login

```json
{
  "email": "jai@gmail.com",
  "password": "password123"
}
```

---

## Create Account

```json
{
  "userId": "b8957ca2-b01f-44fa-8869-b955ce4725e3",
  "accountType": "SAVINGS"
}
```

---

## Deposit Money

```json
{
  "accountNumber": "1234567890",
  "amount": 5000
}
```

---

## Transfer Money

```json
{
  "senderAccountNumber": "1234567890",
  "receiverAccountNumber": "9876543211",
  "amount": 500
}
```

---

# How To Run This Project

## 1. Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/banking-backend-system.git
```

---

## 2. Open Project

Open project in:

* IntelliJ IDEA
* VS Code
* Eclipse

---

## 3. Create MySQL Database

Run:

```sql
CREATE DATABASE banking_db;
```

---

# Configure application.yml

```yaml
server:
  port: 8080

spring:

  application:
    name: PAYMENT_SYSTEM

  datasource:
    url: jdbc:mysql://localhost:3306/banking_db
    username: root
    password: your_password

  jpa:
    hibernate:
      ddl-auto: update

    show-sql: true

jwt:
  secret: yourSuperSecureJwtSecretKey123456789
  expiration: 86400000
```

---

# Run Application

Using Maven:

```bash
mvn spring-boot:run
```

Or run:

```text
PaymentSystemApplication.java
```

from IDE.

---

# Authentication Flow

```text
Register User
    ↓
Login User
    ↓
Receive JWT Token
    ↓
Use Bearer Token in Protected APIs
```

---

# Transfer Flow

```text
Create Account
    ↓
Deposit Money
    ↓
Transfer Money
    ↓
Balances Updated
    ↓
Transaction Stored
```

---

# Important Engineering Concepts Used

* Stateless JWT Authentication
* Layered Architecture
* DTO Pattern
* Exception Handling
* ACID Transactions
* Secure Password Hashing
* Banking Domain Modeling
* Transaction Persistence

---

# Future Goals

The goal of this project is to evolve into a production-style fintech backend by implementing:

* pessimistic locking
* concurrency control
* Redis caching
* idempotency
* distributed systems concepts
* testing and deployment

---

# Author

Jai Singh Katiyar

Backend Developer | Java | Spring Boot | MySQL | Security | Distributed Systems
