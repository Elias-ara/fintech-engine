# Fintech Engine - Architecture & Context

## 1. Overview
This is a robust, ACID-compliant financial backend engine. The system is built as a **Modular Monolith** to facilitate future extraction into microservices if needed. The core principle for financial movements is the **Double-Entry Bookkeeping** pattern.

## 2. Tech Stack
- **Language/Framework:** Java 21+, Spring Boot 3+
- **Database:** PostgreSQL
- **Migrations:** Flyway (or Liquibase)
- **Infrastructure:** Docker & Docker Compose

## 3. Core Domains (Modules)
1. **User Module:** Manages customer profiles and KYC status.
2. **Account Module:** Manages digital wallets/accounts. Balances are dynamically calculated or safely cached, but the *Source of Truth* is always the Ledger.
3. **Transaction Module:** Orchestrates financial operations (Cash-in, Cash-out, P2P Transfers). Handles idempotency.
4. **Ledger Module:** The Double-Entry system. Immutable records of CREDIT and DEBIT.

## 4. Architectural Rules
- **No data deletion:** Soft deletes only for non-financial data. Financial data (Ledger, Transactions) is strictly immutable.
- **Idempotency:** All state-changing endpoints (POST/PUT/PATCH) must require and validate an `Idempotency-Key` header.
- **ACID Transactions:** Ledger entries must be created within the same database transaction as the parent `Transaction` record.
- **UUIDs:** Use UUIDv4 for all primary keys to prevent enumeration attacks.

## 5. Modelagem de Dados do Banco (PostgreSQL)

Esta é a estrutura relacional desenhada para garantir consistência. O Claude usará isso para criar as migrações (Flyway/Liquibase) e as entidades JPA.

    Tabela users (Identidade do cliente)

        id (UUID, PK)

        name (Varchar)

        document (Varchar, Unique) - CPF ou CNPJ

        email (Varchar, Unique)

        created_at (Timestamp)

    Tabela accounts (A carteira do cliente)

        id (UUID, PK)

        user_id (UUID, FK -> users.id)

        status (Varchar) - ACTIVE, BLOCKED, CLOSED

        cached_balance (Numeric 19,4) - Opcional para performance, atualizado via trigger ou lock otimista, mas a fonte da verdade é o ledger.

        created_at (Timestamp)

        updated_at (Timestamp)

    Tabela transactions (A intenção da operação)

        id (UUID, PK)

        idempotency_key (Varchar, Unique) - Garante que a mesma requisição não debite duas vezes.

        operation_type (Varchar) - DEPOSIT, WITHDRAWAL, TRANSFER

        amount (Numeric 19,4) - Sempre positivo.

        status (Varchar) - PENDING, COMPLETED, FAILED

        created_at (Timestamp)

    Tabela ledger_entries (O Double-Entry Bookkeeping - imutável)

        id (UUID, PK)

        transaction_id (UUID, FK -> transactions.id)

        account_id (UUID, FK -> accounts.id) - Para cash-in/out, usa-se uma "Conta Mestra" ou "Conta Sistema" para fechar a partida dobrada.

        operation (Varchar) - CREDIT, DEBIT

        amount (Numeric 19,4) - Sempre positivo.

        created_at (Timestamp)

    A Regra de Ouro do Ledger: Para cada transaction_id, a soma dos ledger_entries de CREDIT deve ser exatamente igual à soma dos ledger_entries de DEBIT.