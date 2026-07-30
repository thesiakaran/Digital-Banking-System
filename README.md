# Enterprise Digital Banking & Fraud Detection System

An event-driven microservices architecture demonstrating advanced backend concepts used in modern fintech companies like Stripe, Monzo, and PayPal. 

This project simulates a high-throughput, distributed banking system that processes financial transactions using the **SAGA Pattern (Choreography)** and evaluates real-time transaction risk using a **Weighted Risk Scoring Engine** backed by Redis.

---

## 🏗️ Architecture & Tech Stack
* **Java 21 & Spring Boot 3.2** (REST APIs, Microservices)
* **Apache Kafka** (Event streaming, SAGA orchestration)
* **Redis** (High-speed caching, Rule Engine tracking)
* **PostgreSQL** (Permanent ledger storage, Database-per-service pattern)
* **Spring Cloud Gateway** (Centralized API routing)

---

## 🗺️ How to Learn & Navigate This Codebase (Code Walkthrough)
Because this is a microservices architecture, you should not read the code top-to-bottom. To truly understand how this system works, you must follow the exact path a user's money takes during a transfer. 

Follow this 5-Step Reading Guide:

### Step 1: The Front Door (`api-gateway`)
Start here. The API Gateway is the central entry point for all frontend client requests.
1. Read `api-gateway/src/main/resources/application.yml`. Look at how it routes URLs (e.g., `/api/accounts`) to the correct internal microservice ports.
2. Read `CorrelationHeaderFilter.java`. This intercepts incoming requests and attaches a unique UUID (`X-Correlation-ID`). This allows you to track a single transaction across all microservices in your logging system.

### Step 2: The Core Ledger (`account-service`)
This service owns the user data and the permanent financial ledger. It connects to the `account_db` PostgreSQL database.
1. Read `Account.java` (Entity). Notice the `@Version` annotation. This implements **Optimistic Locking**, a critical concept that prevents two users from withdrawing the same money at the exact same millisecond.
2. Read `AccountServiceImpl.java`. Look closely at the `holdFunds`, `commitFunds`, and `releaseFunds` methods. This is the foundation of our SAGA pattern.

### Step 3: The Orchestrator (`transaction-service`)
This service acts as the traffic cop. It does not hold user data; it orchestrates the transfer between accounts.
1. Read `TransactionController.java`. Notice that it requires an `Idempotency-Key` header. This prevents the "double-click" bug that causes duplicate financial charges.
2. Read `TransactionServiceImpl.java` (specifically the `initiateTransaction` method). Watch the flow:
   - It saves the transaction as `PENDING`.
   - It makes a synchronous call to the account service to put a hold on the sender's money.
   - It fires a `transaction-created` event into **Kafka**.

### Step 4: The Brain (`fraud-service`)
This is the Risk Scoring Engine. It evaluates the transaction asynchronously over Kafka.
1. Read `TransactionEventListener.java`. See how it listens to Kafka for the `transaction-created` event.
2. Read `RuleEngine.java`. Look at how it loops through a `List<FraudRule>`, summing up a total Risk Score. If the score exceeds 100, it rejects the transaction.
3. Open the `rules/` folder and read the individual implementations:
   - `MoneyMuleRule.java`: Uses Redis to check if the sender just received a massive deposit minutes ago.
   - `VelocityRule.java`: Uses Redis TTLs to block a user from making too many transfers in 60 seconds.
   - `DailyLimitRule.java`: Tracks the daily accumulated total for a user.

### Step 5: The Final Resolution (Back to `transaction-service`)
The SAGA must be completed or rolled back based on the Fraud Engine's decision.
1. Read `FraudResultConsumer.java`. The transaction service listens to Kafka waiting for the Fraud Service to say "Approved" or "Rejected".
2. Go back to `TransactionServiceImpl.java` and read the `processFraudResult` method. 
   - **Rollback (Compensating Transaction):** If fraud was detected, it calls the account service to `release` the held money back to the sender.
   - **Success:** If approved, it calls `commit` to deduct the sender, and deposits the money into the receiver's account. It then marks the transaction as `COMPLETED`.

---

## 🚀 How to Run Locally

1. **Infrastructure:** You must have PostgreSQL (Port 5432), Redis (Port 6379), and Apache Kafka (Port 9092) running locally.
2. **Databases:** Create two empty databases in PostgreSQL named `account_db` and `transaction_db`.
3. **Start Services:** Run the microservices in this order:
   - `api-gateway`
   - `account-service`
   - `transaction-service`
   - `fraud-service`
4. Access the API via `http://localhost:8080/api/...`
