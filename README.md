# Fintech Engine

Backend completo de uma fintech digital, construído com Java 21 e Spring Boot 3. O sistema implementa operações financeiras com **Double-Entry Bookkeeping** (partida dobrada), garantindo consistência e rastreabilidade em todas as movimentações.

## Funcionalidades

- **Autenticação JWT** — registro e login com tokens Bearer
- **Gestão de contas** — criação, consulta e controle de status (ACTIVE, BLOCKED, CLOSED)
- **Transações financeiras** — depósito, saque e transferência P2P
- **Ledger (partida dobrada)** — cada transação gera entradas imutáveis de CREDIT e DEBIT
- **Idempotência** — chave de idempotência em todas as operações, evitando duplicações
- **Extrato paginado** — histórico de transações e entradas do ledger com paginação
- **Documentação interativa** — Swagger UI disponível em `/swagger-ui.html`

## Tech Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4 |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway |
| Autenticação | Spring Security + JWT (JJWT) |
| Documentação | SpringDoc OpenAPI (Swagger) |
| Testes | JUnit 5, Mockito, MockMvc, H2 |
| CI/CD | GitHub Actions |
| Infra | Docker, Docker Compose |

## Arquitetura

O projeto segue o padrão **Modular Monolith**, organizado em domínios independentes:

```
src/main/java/com/fintechengine/
├── modules/
│   ├── auth/          # Registro e login
│   ├── user/          # Perfil do usuário
│   ├── account/       # Contas digitais
│   ├── transaction/   # Orquestração de operações
│   └── ledger/        # Partida dobrada (imutável)
├── security/          # JWT, filtros, config
├── shared/            # Exceções globais
└── config/            # OpenAPI config
```

### Regras de negócio

- Dados financeiros são **imutáveis** — ledger entries nunca são alterados ou deletados
- Saldo é cacheado na conta, mas a **fonte da verdade é o ledger**
- Contas bloqueadas ou fechadas **não podem transacionar**
- Fechamento de conta é **irreversível**
- Para cada transação, `sum(CREDIT) == sum(DEBIT)` — regra de ouro da partida dobrada

## Rodando o projeto

### Com Docker (recomendado)

```bash
docker-compose up --build
```

A aplicação sobe em `http://localhost:8080` com o Postgres configurado automaticamente.

### Sem Docker

Pré-requisitos: Java 21, Maven 3.9+, PostgreSQL rodando na porta 5432.

```bash
# Criar o banco
createdb -U postgres fintech_engine

# Rodar
mvn spring-boot:run
```

## API Endpoints

### Auth
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/register` | Registrar usuário |
| POST | `/auth/login` | Autenticar |

### Accounts
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/accounts` | Criar conta |
| GET | `/accounts/{id}` | Consultar conta |
| PATCH | `/accounts/{id}/status` | Alterar status |
| GET | `/accounts/{id}/transactions` | Extrato de transações |
| GET | `/accounts/{id}/ledger` | Entradas do ledger |

### Transactions
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/transactions/deposit` | Depósito |
| POST | `/transactions/withdrawal` | Saque |
| POST | `/transactions/transfer` | Transferência P2P |
| GET | `/transactions/{id}` | Consultar transação |

> Todos os endpoints (exceto auth) exigem header `Authorization: Bearer <token>`.
> Endpoints paginados aceitam `?page=0&size=20`.

## Testes

```bash
mvn test
```

54 testes cobrindo:
- **Integração** — fluxos completos com MockMvc, autenticação real e H2 em modo PostgreSQL
- **Unitários** — serviços isolados com Mockito
- **Validação** — campos obrigatórios, limites, duplicatas
- **Edge cases** — idempotência, conta bloqueada/fechada, saldo insuficiente, transferência para si mesmo

## Decisões técnicas

- **Optimistic locking** na entidade Account (`@Version`) para evitar race conditions em operações concorrentes
- **H2 em modo PostgreSQL** nos testes para manter compatibilidade sem precisar de banco externo
- **Flyway** para versionamento de schema — cada mudança no banco é rastreável
- **GlobalExceptionHandler** centralizado para respostas de erro consistentes (400, 401, 404, 409, 422)
- **UUIDs** como primary keys para evitar enumeração de IDs
