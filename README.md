# 🏦 Enterprise Digital Banking & Fraud Detection System

An event-driven microservices architecture demonstrating advanced backend concepts used in modern fintech companies like Stripe, Monzo, and PayPal. 

This project simulates a high-throughput, distributed banking system that processes financial transactions using the **SAGA Pattern (Choreography)** and evaluates real-time transaction risk using a **Weighted Risk Scoring Engine** backed by Redis.

---

## 📐 System Architecture

```
                          ┌─────────────────────────┐
                          │     API Gateway :8080    │
                          │  ┌───────────────────┐   │
                          │  │ Correlation Filter │   │
                          │  └───────────────────┘   │
                          └──────────┬──────────────┘
                       ┌─────────────┼──────────────┐
                       ▼             ▼              ▼
              ┌────────────┐  ┌────────────┐  ┌────────────┐
              │  Account   │  │Transaction │  │   Fraud    │
              │  Service   │  │  Service   │  │  Service   │
              │   :8081    │  │   :8082    │  │   :8083    │
              └─────┬──────┘  └──┬─────┬──┘  └──┬─────┬──┘
                    │            │     │         │     │
                    ▼            ▼     │         ▼     │
              ┌──────────┐ ┌──────────┐│    ┌───────┐  │
              │account_db│ │ txn_db   ││    │ Redis │  │
              │PostgreSQL│ │PostgreSQL││    │Upstash│  │
              └──────────┘ └──────────┘│    └───────┘  │
                                       │               │
                              ┌────────▼───────────────▼──┐
                              │      Apache Kafka          │
                              │  transaction-created       │
                              │  transaction-approved      │
                              │  transaction-rejected      │
                              └────────────────────────────┘
```

### Transaction Flow (SAGA Choreography)

```
Client ──POST /api/transactions──▶ API Gateway ──▶ Transaction Service
                                                         │
                                                    1. Save TX (PENDING)
                                                    2. Call Account Service → holdFunds()
                                                    3. Update TX (FUNDS_HELD)
                                                    4. Publish "transaction-created" to Kafka
                                                         │
                                            ┌────────────▼────────────┐
                                            │     Fraud Service       │
                                            │  7 Weighted Risk Rules  │
                                            │  Score < 100 → APPROVE  │
                                            │  Score ≥ 100 → REJECT   │
                                            └────────────┬────────────┘
                                                         │
                              ┌───────────────────┬──────┴───────────────────┐
                              ▼                                              ▼
                     "transaction-approved"                        "transaction-rejected"
                              │                                              │
                    Transaction Service                            Transaction Service
                    → commitFunds()                                → releaseFunds()
                    → deposit() to receiver                        → TX status: ROLLED_BACK
                    → TX status: COMPLETED
```

---

## 🏗️ Tech Stack & Design Decisions

| Technology | Purpose | Why This Choice? |
|---|---|---|
| **Java 21 & Spring Boot 3.2** | REST APIs, Microservices | Industry standard for enterprise banking backends |
| **Apache Kafka** | Event streaming, SAGA orchestration | Guarantees at-least-once delivery, enables async fraud checks |
| **Redis (Upstash)** | Real-time fraud rule state tracking | Sub-millisecond lookups for velocity checks and blacklists |
| **PostgreSQL** | Permanent ledger storage (DB-per-service) | ACID compliance critical for financial transactions |
| **Spring Cloud Gateway** | Centralized API routing | Reactive, non-blocking; injects correlation IDs for tracing |
| **Zipkin + Micrometer** | Distributed tracing | Trace a single transaction across all 4 microservices |

---

## 🔑 Key Design Patterns Implemented

| Pattern | Where | Purpose |
|---|---|---|
| **SAGA (Choreography)** | Transaction + Account + Fraud Services | Distributed transaction management without 2PC |
| **Weighted Risk Scoring** | Fraud Service Rule Engine | Composite fraud evaluation from multiple signals |
| **Optimistic Locking** | Account Entity (`@Version`) | Prevents double-spending race conditions |
| **Idempotency Keys** | Transaction Controller | Prevents duplicate charges from retry/double-click |
| **Correlation ID Propagation** | API Gateway Filter | End-to-end request tracing across services |
| **Database-per-Service** | All services | Loose coupling, independent scaling |
| **Strategy Pattern** | Fraud Rules (`FraudRule` interface) | Plug-and-play fraud detection rules |

---

## 🧠 Fraud Detection Engine — Weighted Risk Scoring

The fraud service evaluates every transaction against 7 configurable rules. Each rule returns a risk score. If the total exceeds 100, the transaction is **blocked**.

| Rule | Trigger | Risk Score | Data Source |
|---|---|---|---|
| **BlacklistRule** | Sender or receiver in blacklist | 100 (Instant Block) | Redis Set |
| **DailyLimitRule** | Daily cumulative transfers > $15,000 | 100 (Instant Block) | Redis Counter |
| **ThresholdRule** | Single transfer > $100,000 | 100 (Instant Block) | Config |
| **ThresholdRule** | Single transfer > $10,000 | 30 | Config |
| **MoneyMuleRule** | Sender received large sum < 10min ago | 50 | Redis TTL Flag |
| **VelocityRule** | > 5 transactions in 60 seconds | 40 | Redis Counter |
| **NewPayeeRule** | First-time transfer to this receiver | 30 | Redis Set |
| **RoundAmountRule** | Amount ≥ $1,000 and exact multiple of 1,000 | 15 | In-memory |

---

## 🗺️ Code Walkthrough (5-Step Reading Guide)

Because this is a microservices architecture, follow the path a user's money takes:

### Step 1: The Front Door (`api-gateway`)
1. Read `application.yml` — URL routing to internal services
2. Read `CorrelationHeaderFilter.java` — injects `X-Correlation-ID` UUID

### Step 2: The Core Ledger (`account-service`)
1. Read `Account.java` — notice `@Version` (Optimistic Locking)
2. Read `AccountServiceImpl.java` — `holdFunds`, `commitFunds`, `releaseFunds`

### Step 3: The Orchestrator (`transaction-service`)
1. Read `TransactionController.java` — `Idempotency-Key` header requirement
2. Read `TransactionServiceImpl.java` — SAGA flow: PENDING → FUNDS_HELD → Kafka event

### Step 4: The Brain (`fraud-service`)
1. Read `RuleEngine.java` — iterates `List<FraudRule>`, sums risk scores
2. Read individual rules in `rules/` — Redis-backed stateful fraud detection

### Step 5: The Resolution (back to `transaction-service`)
1. Read `FraudResultConsumer.java` — listens for Kafka approval/rejection
2. Read `processFraudResult` — commit (approve) or release (rollback)

---

## 📡 API Endpoints

### Account Service (`:8081`)
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/accounts` | Create new account |
| `GET` | `/api/accounts/{accountNumber}` | Get account details |
| `POST` | `/api/accounts/hold` | Hold funds (SAGA) |
| `POST` | `/api/accounts/{accountNumber}/commit` | Commit held funds |
| `POST` | `/api/accounts/{accountNumber}/release` | Release held funds (rollback) |
| `POST` | `/api/accounts/{accountNumber}/deposit` | Deposit money |
| `POST` | `/api/accounts/withdraw` | Withdraw money |

### Transaction Service (`:8082`)
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/transactions` | Initiate transaction (requires `Idempotency-Key` header) |
| `GET` | `/api/transactions/{transactionId}` | Get transaction status |
| `GET` | `/api/transactions/account/{accountNumber}` | Get account history |

### Fraud Service (`:8083`)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/fraud/alerts` | Get fraud alerts (paginated, filterable by status) |
| `GET` | `/api/fraud/alerts/{id}` | Get alert details |
| `PUT` | `/api/fraud/alerts/{id}/status` | Update alert status |
| `GET` | `/api/fraud/statistics` | Fraud dashboard statistics |
| `POST` | `/api/fraud/blacklist/{accountId}` | Add account to blacklist |
| `DELETE` | `/api/fraud/blacklist/{accountId}` | Remove from blacklist |
| `GET` | `/api/fraud/blacklist` | View blacklisted accounts |

> **Swagger UI** available at `http://localhost:{port}/swagger-ui.html` for each service.

---

## 🚀 How to Run Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Start Infrastructure
```bash
docker-compose up -d
```
This starts PostgreSQL (account_db, transaction_db, fraud_db), Redis, Kafka, Zookeeper, and Zipkin.

### 2. Create Databases
```bash
# Databases are auto-created by Docker Compose
# account_db on port 5432
# transaction_db on port 5433
# fraud_db on port 5434
```

### 3. Start Services
```bash
# From the root directory
./start-all-services.bat
```
Or start individually:
```bash
cd api-gateway && mvn spring-boot:run
cd account-service && mvn spring-boot:run
cd transaction-service && mvn spring-boot:run
cd fraud-service && mvn spring-boot:run
```

### 4. Run Tests
```bash
cd fraud-service && mvn test
```

### 5. Access
- **API Gateway:** `http://localhost:8080/api/...`
- **Swagger UI:** `http://localhost:8081/swagger-ui.html` (account), `http://localhost:8082/swagger-ui.html` (transaction)
- **Zipkin:** `http://localhost:9411`

---

## 🧪 Testing

The fraud detection engine has a comprehensive test suite covering all 7 fraud rules and the rule engine:

```
fraud-service/src/test/java/com/bank/fraud/engine/
├── RuleEngineTest.java          — 6 tests (approval, rejection, scoring logic)
└── rules/
    ├── ThresholdRuleTest.java   — 6 tests (normal, high, max amounts)
    ├── VelocityRuleTest.java    — 6 tests (rate limiting with mocked Redis)
    ├── BlacklistRuleTest.java   — 5 tests (sender/receiver blacklist checks)
    ├── MoneyMuleRuleTest.java   — 7 tests (hot-potato detection patterns)
    ├── DailyLimitRuleTest.java  — 6 tests (cumulative daily tracking)
    ├── NewPayeeRuleTest.java    — 6 tests (first-time payee detection)
    └── RoundAmountRuleTest.java — 6 tests (round number patterns)
```

**48 test cases** total with mocked Redis dependencies using Mockito.

---

## 📁 Project Structure

```
digital-banking-system/
├── api-gateway/                 # Spring Cloud Gateway (routing, correlation IDs)
├── account-service/             # Account CRUD, fund holds, optimistic locking
├── transaction-service/         # SAGA orchestrator, idempotency, WebClient
├── fraud-service/               # Risk scoring engine, Kafka consumer, fraud dashboard
├── frontend/                    # Premium UI (HTML/CSS/JS) with Auth flow and API Mocking
├── docker-compose.yml           # PostgreSQL x3, Redis, Kafka, Zookeeper, Zipkin
├── pom.xml                      # Parent POM (multi-module Maven)
└── start-all-services.bat       # Windows batch to start all services
```

---

## 🖥️ Frontend UI & API Mocking

This repository includes a standalone frontend located in the `/frontend` directory (`index.html`). It provides a premium, glassmorphism-styled dashboard that perfectly visualizes the complex backend transactions.

**Key Features of the UI:**
- **Dynamic Authentication:** A sleek Login and Sign Up modal with micro-animations and loading state simulation.
- **Transaction Dashboard:** Visually track transactions going through the SAGA flow (Pending → Hold → Commit/Rollback).
- **Intelligent Fallback (Mock API):** If the Java backend services or Docker are not running, the frontend intelligently intercepts API calls and provides realistic mock data. This allows reviewers, recruiters, or clients to fully test and explore the UI without needing to spin up the entire infrastructure.
