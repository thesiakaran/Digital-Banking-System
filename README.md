# 🏦 Enterprise Digital Banking & Real-Time Fraud Detection System

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-brightgreen?style=for-the-badge&logo=spring-boot)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-black?style=for-the-badge&logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-Rate_Limiting-red?style=for-the-badge&logo=redis)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-ACID-blue?style=for-the-badge&logo=postgresql)

A highly resilient, distributed microservices architecture simulating a modern core banking system. Built with **Spring Boot, Apache Kafka, and Redis**, this project demonstrates enterprise-grade patterns including the **SAGA Pattern for distributed transactions, Resilience4j Circuit Breakers, and a real-time Fraud Detection Engine.**

---

## 🚀 Core Enterprise Features

*   **⚡ Real-Time Fraud Engine:** A rule-based engine that evaluates transactions in milliseconds. If a transaction violates the **Threshold Rule** (>$100,000) or the **Velocity Rule** (>3 transfers per minute), it is instantly flagged and blocked.
*   **🔄 SAGA Pattern (Distributed Consistency):** Uses Apache Kafka to ensure ACID properties across multiple microservices. If the Fraud Engine rejects a transfer, a compensating transaction is published to safely refund the money, preventing "ghost" deductions.
*   **🛡️ Anti-DDoS API Gateway:** Implements a **Token Bucket Rate Limiter** via Spring Cloud Gateway and Redis. Tracks user IP addresses to block brute-force attacks and prevent core database overload.
*   **🔌 Resilience4j Circuit Breaker:** Protects the banking network from cascading failures. If a microservice crashes, the gateway trips the circuit and routes users to a graceful Fallback Controller.
*   **📜 Spring AOP Enterprise Logging:** Aspect-Oriented Programming (AOP) intercepts controller requests to maintain strict audit trails of execution times and method signatures without polluting business logic.
*   **🎨 Stateful UI & "Demo Mode":** A highly polished Vanilla JS/CSS frontend featuring an integrated Demo Mode that simulates live cyber-attacks entirely in the browser for instant portfolio presentations.

---

## 🏗️ Microservices Architecture

The backend is decomposed into decoupled, independently scalable microservices:

1.  **API Gateway (Port 8080):** The central entry point. Handles Rate Limiting (Redis), Circuit Breaking (Resilience4j), and intelligent routing.
2.  **Account Service (Port 8081):** Manages user profiles, secure account creation, and ACID-compliant ledger balances via PostgreSQL.
3.  **Transaction Service (Port 8082):** Orchestrates the movement of money between accounts and initiates the SAGA transaction workflow.
4.  **Fraud Service (Port 8083):** The security brain. Subscribes to Kafka topics, runs heuristic rules against transactions, and publishes `transaction-approved` or `transaction-rejected` events.

---

## 🛠️ How to Run Locally

To make this project as easy to evaluate as possible, it comes with an **Embedded Infrastructure Config**. You do **NOT** need to install Docker, Kafka, or Redis on your machine! The Spring Boot JVM will boot in-memory instances automatically.

### 1. Requirements
*   Java 21+
*   Maven
*   PostgreSQL (Running on `localhost:5432` with a database named `banking_db`)

### 2. Start the Backend (One-Click)
Simply run the included batch script to compile and boot the entire banking network:
```bash
start-all-services.bat
```

### 3. Start the Frontend
The frontend is purely HTML/CSS/JS. You can open `index.html` directly in your browser, or run a simple Python server:
```bash
cd frontend
python -m http.server 3000
```
Open `http://localhost:3000` to view the banking dashboard.

---

## 🧪 Testing the Fraud Engine

To see the SAGA pattern and Fraud Engine in action, perform the following in the UI:
1.  **Trigger the Threshold Rule:** Attempt to transfer **$150,000**. The UI will display a `FRAUD BLOCKED` error, and the SAGA pattern will reverse the deduction.
2.  **Trigger the Velocity Rule:** Rapidly click the "Transfer" button **5 times in a row**. The Redis Token Bucket will allow the burst, but the Fraud Engine will detect the behavioral anomaly and freeze the transaction. 
3.  **View the Results:** Navigate to the **Fraud Alerts** tab to see the detailed Risk Score breakdown of the blocked cyber-attacks.

---
*Built to demonstrate Elite Distributed Systems Architecture.*
