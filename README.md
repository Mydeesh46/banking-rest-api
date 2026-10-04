# Core Banking REST API Service

## 📋 Overview

The **Core Banking REST API Service** is a backend system engineered using **Spring Boot 3**, **Spring Data JPA**, and **PostgreSQL**. It delivers simulation of core retail banking transactions, including automated account creation, balance checks, debit/credit processing, and inter-account fund transfers.

The service uses a layered architecture (Controller, Service, DTO, Repository) and enforces **ACID guarantees** using Spring's `@Transactional` boundaries. It features automated running-balance calculation, ledger auditing, and standardized global exception handling via `@RestControllerAdvice`.

---

## 🎯 Key Features

### 🏦 Core Banking Operations

* **Automated Account Provisioning:** Secure account creation with auto-generated identifiers (`ACC########`).
* **Real-Time Balance Enquiry:** Low-latency balance lookups.
* **Transactional Operations:** Atomic cash deposits, cash withdrawals, and inter-account funds transfers.
* **Automated Transaction Audit Logging:** Double-entry ledger recordings generating separate debit and credit records with unique reference UUIDs.

### 🛡️ Transaction Integrity & Concurrency

* **ACID Transaction Management:** Implements `@Transactional` across fund transfers to prevent partial commits or money-duplication vulnerabilities.
* **Strict State Invariants:** Prevents self-transfers, negative deposits, and over-drafting through business rule validations.
* **Data Precision:** Uses `java.math.BigDecimal` for financial calculation accuracy.

### ⚙️ Clean Architecture & Design

* **Layered Microservices Architecture:** Separation of concerns across Controller, Service, Repository, and Data Transfer Object (DTO) boundaries.
* **Global Error Handling:** Intercepts runtime errors (`ResourceNotFoundException`, `InsufficientBalanceException`, validation failures) to return structured JSON envelopes.
* **Declarative Bean Validation:** Request validation using Jakarta Validation annotations (`@NotBlank`, `@NotNull`, `@DecimalMin`).

---

## 🏗️ Architecture

### System Architecture Flow

```text
┌─────────────────────────────────────────────────────────────────┐
│                       Client Layer                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  API Consumers (Postman / Web Clients / Mobile Clients)   │  │
│  │  • HTTP GET / POST                                        │  │
│  │  • Application/JSON Payloads                              │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Controller Layer (REST Endpoints)            │
│  ┌──────────────────────────┐    ┌───────────────────────────┐  │
│  │ AccountController        │    │ TransactionController     │  │
│  │ • POST /api/v1/accounts  │    │ • GET /api/v1/            │  │
│  │ • GET  /api/v1/accounts/ │    │        transactions/{acc} │  │
│  │        {accountNumber}   │    └───────────────────────────┘  │
│  │ • POST /deposit,/withdraw│                                   │
│  │ • POST /transfer         │                                   │
│  └──────────────────────────┘                                   │
└─────────────────────────────────────────────────────────────────┘
                                 │
                   (DTOs Validation & Mapping)
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Service Layer (Business Logic)              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ BankService                                               │  │
│  │ • @Transactional Fund Transfers                          │  │
│  │ • Account Verification & Balance Sufficiency Checks       │  │
│  │ • Ledger Entry Generation (DEBIT / CREDIT)                │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ TransactionService                                        │  │
│  │ • Chronological Statement Retrieval                      │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                 │
                   (Spring Data JPA Repositories)
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Persistence & Storage Layer                  │
│  ┌──────────────────────────┐    ┌───────────────────────────┐  │
│  │ AccountRepository        │    │ TransactionRepository     │  │
│  │ (Spring Data JPA)        │    │ (Spring Data JPA)         │  │
│  └──────────────────────────┘    └───────────────────────────┘  │
│                                │                                │
│                                ▼                                │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                 PostgreSQL Relational DB                 │  │
│  │  • accounts table                                         │  │
│  │  • transactions ledger table                              │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

```

### Fund Transfer Transaction Workflow

```text
[Sender]                [BankService]               [AccountRepo]           [TransactionRepo]       [PostgreSQL DB]
   │                          │                           │                         │                      │
   │─── POST /transfer ──────>│                           │                         │                      │
   │    (amount, from, to)    │                           │                         │                      │
   │                          │─── Fetch Source & Dest ──>│                         │                      │
   │                          │<── Return Accounts ───────│                         │                      │
   │                          │                                                     │                      │
   │                          ├── Check 1: Do Accounts Exist?                       │                      │
   │                          ├── Check 2: Same Account Check                       │                      │
   │                          └── Check 3: Is Balance >= Amount?                    │                      │
   │                                                                                │                      │
   │                          │── Update Balances (Source -, Dest +) ──────────────>│                      │
   │                          │── Save Sender & Receiver Accounts ─────────────────>│                      │
   │                          │── Generate & Save DEBIT Ledger Record ─────────────>│                      │
   │                          │── Generate & Save CREDIT Ledger Record ────────────>│                      │
   │                          │                                                     │                      │
   │                          │── [COMMIT TRANSACTION] ───────────────────────────────────────────────────>│
   │                          │                                                                            │
   │<── 200 OK (Transferred) ─│                                                                            │
   │                          │                                                                            │

```

---

## 📦 Project Structure

```text
banking-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/bank/api/
│   │   │       ├── BankingApiApplication.java       # Spring Boot main runner
│   │   │       ├── controller/                      # REST API Web endpoints
│   │   │       │   ├── AccountController.java
│   │   │       │   └── TransactionController.java
│   │   │       ├── dto/                             # Immutable Records (Data Transfer Objects)
│   │   │       │   ├── AccountResponse.java
│   │   │       │   ├── CreateAccountRequest.java
│   │   │       │   ├── DepositWithdrawRequest.java
│   │   │       │   ├── ErrorResponse.java
│   │   │       │   ├── TransactionResponse.java
│   │   │       │   └── TransferRequest.java
│   │   │       ├── entity/                          # JPA Database Models
│   │   │       │   ├── Account.java
│   │   │       │   └── TransactionRecord.java
│   │   │       ├── exception/                       # Custom Errors & Global Handler
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── InsufficientBalanceException.java
│   │   │       │   └── ResourceNotFoundException.java
│   │   │       ├── repository/                      # Spring Data JPA interfaces
│   │   │       │   ├── AccountRepository.java
│   │   │       │   └── TransactionRepository.java
│   │   │       └── service/                         # Business & Transactional Logic
│   │   │           ├── BankService.java
│   │   │           └── TransactionService.java
│   │   └── resources/
│   │       └── application.properties               # PostgreSQL datasource & JPA settings
│   └── test/
│       └── java/
│           └── com/bank/api/
│               └── BankServiceTest.java             # JUnit 5 & Mockito test suites
├── pom.xml                                          # Maven project dependencies
├── .gitignore                                       # Files and artifacts excluded from Git
└── README.md                                        # Documentation

```

---

## 🔌 API Endpoints

### Base URL

```text
http://localhost:8080

```

---

### 1. Create New Account

Provisions a new account with an initial balance and creates an audit record if the balance is greater than zero.

* **Method:** `POST`
* **Path:** `/api/v1/accounts`
* **Content-Type:** `application/json`

**Request Body:**

```json
{
  "accountHolderName": "Alice Johnson",
  "initialDeposit": 1000.00
}

```

**Response (`201 Created`):**

```json
{
  "accountNumber": "ACC62564813",
  "accountHolderName": "Alice Johnson",
  "balance": 1000.00,
  "createdAt": "2026-09-07T12:00:56.298516"
}

```

---

### 2. Balance Enquiry

Retrieves account information and current available balance.

* **Method:** `GET`
* **Path:** `/api/v1/accounts/{accountNumber}`

**Response (`200 OK`):**

```json
{
  "accountNumber": "ACC62564813",
  "accountHolderName": "Alice Johnson",
  "balance": 1000.00,
  "createdAt": "2026-09-07T12:00:56.298516"
}

```

---

### 3. Deposit Money

Credits an account and logs a `DEPOSIT` transaction.

* **Method:** `POST`
* **Path:** `/api/v1/accounts/deposit`
* **Content-Type:** `application/json`

**Request Body:**

```json
{
  "accountNumber": "ACC62564813",
  "amount": 250.00
}

```

**Response (`200 OK`):**

```json
{
  "accountNumber": "ACC62564813",
  "accountHolderName": "Alice Johnson",
  "balance": 1250.00,
  "createdAt": "2026-09-07T12:00:56.298516"
}

```

---

### 4. Withdraw Money

Debits an account after verifying fund availability.

* **Method:** `POST`
* **Path:** `/api/v1/accounts/withdraw`
* **Content-Type:** `application/json`

**Request Body:**

```json
{
  "accountNumber": "ACC62564813",
  "amount": 100.00
}

```

**Response (`200 OK`):**

```json
{
  "accountNumber": "ACC62564813",
  "accountHolderName": "Alice Johnson",
  "balance": 1150.00,
  "createdAt": "2026-09-07T12:00:56.298516"
}

```

---

### 5. Fund Transfer

Executes an atomic transfer between two distinct accounts.

* **Method:** `POST`
* **Path:** `/api/v1/accounts/transfer`
* **Content-Type:** `application/json`

**Request Body:**

```json
{
  "fromAccount": "ACC62564813",
  "toAccount": "ACC98412356",
  "amount": 300.00,
  "remarks": "Project invoice settlement"
}

```

**Response (`200 OK`):**

```json
{
  "message": "Funds transferred successfully"
}

```

---

### 6. Transaction Statement

Retrieves historical ledger transactions for an account in reverse-chronological order.

* **Method:** `GET`
* **Path:** `/api/v1/transactions/{accountNumber}`

**Response (`200 OK`):**

```json
[
  {
    "transactionReference": "e2a537f0-transfer-DEBIT",
    "accountNumber": "ACC62564813",
    "amount": 300.00,
    "type": "TRANSFER_OUT",
    "runningBalance": 850.00,
    "remarks": "Transferred to ACC98412356 - Project invoice settlement",
    "timestamp": "2026-09-07T12:15:30"
  },
  {
    "transactionReference": "a931c890-initial-DEPOSIT",
    "accountNumber": "ACC62564813",
    "amount": 1000.00,
    "type": "DEPOSIT",
    "runningBalance": 1000.00,
    "remarks": "Initial Opening Deposit",
    "timestamp": "2026-09-07T12:00:56"
  }
]

```

---

## 📖 Usage Guide

### 1. Prerequisites

* **Java Development Kit (JDK):** Version 21 or higher
* **Relational Database:** PostgreSQL 14+
* **Build Tool:** Apache Maven 3.8+ (or Maven Wrapper `mvnw`)
* **REST Client:** Postman or cURL

### 2. Database Initialization

Log into the PostgreSQL CLI or pgAdmin and run:

```sql
CREATE DATABASE bank_db;

```

### 3. Configuration

Verify `src/main/resources/application.properties`:

```properties
spring.application.name=banking-api

# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/bank_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080

```

### 4. Build and Run

Clone and launch the service:

```bash
# Clone the repository
git clone https://github.com/Mydeesh46/banking-rest-api.git
cd banking-rest-api

# Build with Maven
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

```

---

## 🧪 Testing the API

### Unit Tests

The project uses **JUnit 5** and **Mockito** to test core business logic and validations. Run tests with:

```bash
mvn test

```

### Sample Automated Test Coverage

* `testCreateAccountSuccess()`: Asserts account initialization and ledger creation.
* `testTransferFundsSuccess()`: Asserts debit/credit modifications and audit persistence.
* `testTransferInsufficientFunds()`: Validates rollback triggers when account balance is insufficient.

---

## 🔐 Security & Reliability Invariants

* **Transactional Atomicity:** If a credit to a recipient fails, the debit from the sender rolls back automatically.
* **Input Validation:** Enforces positive transfer amounts (`@DecimalMin("1.00")`) and checks for non-empty input strings.
* **Domain Exception Safety:** Catches missing entities and returns an HTTP `404 Not Found` rather than leaking internal database stack traces.
* **Floating-Point Guardrails:** Replaces standard double/float primitives with `BigDecimal` to avoid binary floating-point rounding errors during currency calculations.

---

## 📚 Technology Stack

| Component | Technology | Version |
| --- | --- | --- |
| **Language** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 3.3.x / 3.5.x |
| **ORM / Data Access** | Spring Data JPA / Hibernate ORM | 6.5.x |
| **Database** | PostgreSQL | 15+ |
| **Database Driver** | PostgreSQL JDBC Driver | 42.7.x |
| **Validation** | Jakarta Bean Validation (Hibernate Validator) | 3.0.x |
| **Unit Testing** | JUnit 5, Mockito, AssertJ | Latest |
| **Build Tool** | Apache Maven | 3.9+ |

---

## 👤 Author

**Mydeesh Avishetti**

* GitHub: Mydeesh46
* Project: Banking REST API


