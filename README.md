# Bank of CLI

A functional banking application that runs entirely within the terminal. This project implements a simple command-line interface backed by a persistent database, applying Java, SQL, and Agile development practices.

## Entity Relationship Diagram

![ERD Diagram](images/erd.png)

## Features

- **Account Management** — register a new account with a 4-digit PIN and initial deposit; log in with your account ID and PIN
- **Balance Management** — check your current account balance at any time
- **Deposit & Withdraw** — add or remove funds, with protection against overdrawing
- **Transfer** — move funds securely between two accounts, processed atomically so a partial transfer can never occur
- **Transaction History** — view a full audit trail of deposits, withdrawals, and transfers for your account
- **Session Management** — log out and switch between accounts without restarting the application
- **Logging** — all successful actions and failures are recorded to a rotating log file (`logs/bank-of-cli.log`)

## Architecture

This project follows a layered architecture:

1. **API Layer** (`api/`) — terminal menu, user input, and output formatting
2. **Business Layer** (`business/`) — banking rules (insufficient funds checks, PIN validation, transaction atomicity)
3. **Repository Layer** (`repository/`) — JDBC-based communication with the Postgres database

## Tech Stack

- **Language:** Java 17
- **Build Tool:** Maven
- **Database:** PostgreSQL 18 (via Docker)
- **Testing:** JUnit 5 + Mockito
- **Logging:** SLF4J + Logback
- **Version Control:** Git & GitHub

## Prerequisites

- Java 17+
- Maven
- Docker & Docker Compose

## Setup

1. **Clone the repository**
```bash
git clone https://github.com/PG-23/BankOfCLI.git
cd BankOfCLI
```

2. **Create your local environment files** from the provided examples:
```bash
cp .env.example .env
cp src/main/resources/db.properties.example src/main/resources/db.properties
```
   Fill in matching values in both files (they must use the same database name, username, and password).

3. **Start the database**
```bash
docker compose up -d
```
   This starts a Postgres 18 container and automatically applies the schema in `db/init/01_schema.sql` on first run.

4. **Run the application** — open the project in IntelliJ (or another Java IDE) and run `Main.java`, or build and run via Maven:
```bash
mvn compile exec:java -Dexec.mainClass="com.patrick.bankofcli.Main"
```

## Running Tests
```bash
mvn test
```

Includes positive and negative unit tests for `AccountService.withdraw()`, verifying both a successful withdrawal and correct rejection of an over-limit withdrawal, using Mockito to isolate business logic from the database.

## Logging

Application activity is logged to `logs/bank-of-cli.log` (rotated daily, retaining 7 days of history). `INFO` entries record successful actions (login, deposit, withdrawal, transfer, etc.); `ERROR` entries record failures (incorrect PIN, insufficient funds, account not found, database errors).

## Notes

- PINs are currently stored and compared as plain text. In a production system, these would be hashed (e.g., with bcrypt) — this is a simplification made for the scope of this project.