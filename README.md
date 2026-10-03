# Banking REST API Service

A production-style Core Banking Backend REST API developed using Spring Boot, Spring Data JPA, and PostgreSQL.

## Features
- **Account Management**: Create accounts and check real-time balances.
- **Transactions**: Secure deposits, withdrawals, and inter-account fund transfers.
- **Data Integrity**: Enforced ACID transactions using `@Transactional` to guarantee atomic fund transfers.
- **Robust Exception Handling**: Global error handling (`@RestControllerAdvice`) delivering uniform error envelopes.
- **Unit Testing**: Service layer tested with JUnit 5 and Mockito.

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven

## Getting Started
1. Configure database credentials in `src/main/resources/application.properties`.
2. Create the PostgreSQL database:
   ```sql
   CREATE DATABASE bank_db;
