# FinSight AI — Enterprise Microservices Backend Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

> **FinSight AI** is a production-grade, distributed financial intelligence platform designed for autonomous ledger reconciliation, multi-factor AML & fraud anomaly detection, predictive cash-flow forecasting, AR aging analysis, compliance auditing, and multi-agent AI orchestration.

---

## Project Structure

The project is organized into three decoupled architectural tiers:

```
FinSight_AI_Application/
├── Backend/              # Multi-module Spring Boot 3.3 microservices ecosystem
│   ├── eureka-server/           # Service discovery (:8761)
│   ├── api-gateway/             # Reactive Gateway & JWT security filter (:8080)
│   ├── auth-service/            # Auth, ChaCha20 crypto, OTP, and RBAC menu (:8081)
│   ├── financial-data-service/  # Accounts & transaction ledgers (:8082)
│   ├── reconciliation-service/  # Deterministic matching engine (:8083)
│   ├── fraud-detection-service/ # Velocity & anomaly detection (:8084)
│   ├── audit-reporting-service/ # Immutable audit trail (:8085)
│   ├── financial-insights-service/ # Cash-flow runway & AR aging (:8086)
│   ├── ai-orchestrator-service/ # 9 Specialist AI agents (:8087)
│   ├── finsight-common/         # Shared models, ChaCha20 AEAD, and security utils
│   ├── pom.xml                  # Parent Maven Reactor POM
│   └── docker-compose.yml       # Backend multi-container orchestration
├── Frontend/             # Web application client tier (React / Vite / TypeScript)
├── Middleware/           # Event brokers (Kafka/RabbitMQ), Redis caching & reverse proxy
├── .env                  # Root environment secrets (excluded from Git)
└── FinSight_AI_API_Collection.postman_collection.json # API collection
```

---

## 1. High-Level Architecture

The platform follows a modern, decoupled microservices architecture with a reactive API Gateway, centralized Service Registry (Netflix Eureka), OpenFeign inter-service communication with correlation ID propagation, and isolated PostgreSQL databases per bounded domain.

```mermaid
graph TD
    Client[Web / Mobile / Postman Clients] -->|HTTP / REST| Gateway[API Gateway :8080]
    Gateway -->|Service Discovery| Eureka[Eureka Server :8761]
    
    subgraph Core Domain Microservices
        Auth[Auth Service :8081]
        FinancialData[Financial Data Service :8082]
        Recon[Reconciliation Service :8083]
        Fraud[Fraud Detection Service :8084]
        Audit[Audit & Reporting Service :8085]
        Insights[Financial Insights Service :8086]
        AIOrchestrator[AI Orchestrator Service :8087]
    end

    Gateway --> Auth
    Gateway --> FinancialData
    Gateway --> Recon
    Gateway --> Fraud
    Gateway --> Audit
    Gateway --> Insights
    Gateway --> AIOrchestrator

    AIOrchestrator -.->|OpenFeign Tools| FinancialData
    AIOrchestrator -.->|OpenFeign Tools| Recon
    AIOrchestrator -.->|OpenFeign Tools| Fraud
    AIOrchestrator -.->|OpenFeign Tools| Insights
    AIOrchestrator -.->|OpenFeign Tools| Audit

    subgraph Data Tier (Isolated Schemas)
        DB1[(finsight_auth)]
        DB2[(finsight_financial_data)]
        DB3[(finsight_reconciliation)]
        DB4[(finsight_fraud)]
        DB5[(finsight_audit)]
        DB6[(finsight_insights)]
        DB7[(finsight_ai)]
    end

    Auth --> DB1
    FinancialData --> DB2
    Recon --> DB3
    Fraud --> DB4
    Audit --> DB5
    Insights --> DB6
    AIOrchestrator --> DB7
```

---

## 2. Microservices Service Registry & Port Mapping

| Service Name | Port | Database | Primary Responsibility | Key Endpoints |
|---|---|---|---|---|
| **`eureka-server`** | `8761` | — | Service discovery & registration | `/eureka/*`, `/` |
| **`api-gateway`** | `8080` | — | Reverse proxy, JWT authentication filter, request correlation | `/api/v1/**` |
| **`auth-service`** | `8081` | `finsight_auth` | User registration, login, refresh tokens, RBAC roles & permissions | `/api/v1/auth/**`, `/api/v1/users/**` |
| **`financial-data-service`** | `8082` | `finsight_financial_data` | Account management, transaction ingestion (CSV/JSON), SHA-256 idempotency | `/api/v1/accounts/**`, `/api/v1/transactions/**` |
| **`reconciliation-service`** | `8083` | `finsight_reconciliation` | Deterministic matching engine (Exact, Tolerance, 1-to-many), discrepancy review workflows | `/api/v1/reconciliation/runs/**`, `/api/v1/reconciliation/discrepancies/**` |
| **`fraud-detection-service`** | `8084` | `finsight_fraud` | Multi-factor AML & anomaly rules, risk scoring (0-100), alert lifecycle investigation | `/api/v1/fraud/evaluate`, `/api/v1/fraud/alerts/**` |
| **`audit-reporting-service`** | `8085` | `finsight_audit` | Immutable audit trail persistence, state before/after snapshots, compliance reports | `/api/v1/audit/events/**`, `/api/v1/audit/reports/**` |
| **`financial-insights-service`** | `8086` | `finsight_insights` | Invoice balance tracking, AR aging buckets, expense breakdown, 30/60/90-day cash flow runways | `/api/v1/insights/invoices/**`, `/api/v1/insights/analytics/**` |
| **`ai-orchestrator-service`** | `8087` | `finsight_ai` | 9 specialist financial AI agents, microservice tool invocation, human approval review gates | `/api/v1/ai/execute`, `/api/v1/ai/runs/**`, `/api/v1/ai/tools`, `/api/v1/ai/agents` |

---

## 3. Key Architectural Features

### 3.1. Zero-Trust Gateway & Centralized JWT Validation
- **`JwtAuthenticationGatewayFilterFactory`** in `api-gateway` intercepts every incoming request.
- Public routes (`/api/v1/auth/**`, `/actuator/**`, Swagger docs) bypass security checks.
- Authenticated requests have their JWT validated using HMAC-SHA256 (`JJWT 0.12.6`).
- Extracted claims (`X-User-Id`, `X-User-Email`, `X-User-Roles`) are injected into the downstream headers for microservices to consume without repeating cryptographic token validation.

### 3.2. End-to-End Distributed Tracing
- Every HTTP request passing through the API Gateway is tagged with a unique `X-Correlation-ID` header (or generated via UUID if absent).
- Transmitted through `CorrelationIdFilter`, maintained in thread-local storage via `CorrelationIdContext`, and passed forward across OpenFeign clients using `FeignCorrelationInterceptor`.

### 3.3. Deterministic Transaction Reconciliation Engine
- **Matching Tiers:**
  1. **Exact Match:** Identical amount, matching currency, opposite direction, matching source reference.
  2. **Tolerance Match:** Date variance within configurable window (default ±3 days) and amount variance within configurable tolerance (default ±0.05).
  3. **One-to-Many Aggregation Match:** Matches a batch of split transactions against a single aggregate settlement entry.
- **Discrepancy Categorization:** Flags `AMOUNT_MISMATCH`, `TIMING_DIFFERENCE`, `UNMATCHED_SOURCE`, or `UNMATCHED_TARGET`.
- **Review Decision Workflows:** Enables reviewers to record `APPROVE_MATCH`, `REJECT_MATCH`, `WRITE_OFF`, `ADJUSTMENT_REQUIRED`, or `ESCALATE_TO_COMPLIANCE`.

### 3.4. Multi-Factor Fraud Detection & Risk Scoring
- Evaluates inbound transactions through a multi-factor rules matrix:
  - **`HIGH_AMOUNT_ANOMALY`**: Weight 35 (Transactions exceeding threshold, e.g. $50,000).
  - **`HIGH_VELOCITY_SPIKE`**: Weight 30 (Burst of transactions within a short rolling timeframe).
  - **`CROSS_BORDER_ANOMALY`**: Weight 25 (High-risk or unexpected counterparty jurisdiction).
  - **`SUSPICIOUS_ROUND_AMOUNT`**: Weight 20 (Unusually clean round amounts, e.g. $10,000.00).
  - **`DUPLICATE_CANDIDATE`**: Weight 25 (Identical amount and recipient within hours).
  - **`CONNECTED_ACCOUNTS_CYCLE`**: Weight 40 (Circular funds routing).
- Outputs normalized **Risk Score (0–100)** mapped to severity tiers (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).

### 3.5. Predictive Financial Insights & Cash-Flow Runway
- **AR Aging Buckets:** Dynamically computes outstanding receivables across `0-30`, `31-60`, `61-90`, and `90+` days overdue.
- **Expense Intelligence:** Categorizes debit outflows (SaaS, Payroll, Infrastructure, Legal) and identifies top vendor concentration risks.
- **Runway Forecast:** Models opening cash, daily projected operational inflows and recurring outflows over 30, 60, or 90 days.

### 3.6. 9 Specialist Financial AI Agents & Human-in-the-Loop Gates
The `ai-orchestrator-service` provides a multi-agent hierarchy orchestrated by a Supervisory Meta-Orchestrator:

1. **`SupervisoryMetaOrchestratorAgent`**: Decomposes natural language queries, classifies intent, delegates tasks to domain specialists, and enforces human sign-off rules.
2. **`FinancialDataAnalystAgent`**: Ingestion health, ledger balances, and currency exposures.
3. **`ReconciliationSpecialistAgent`**: Matches entries, uncovers discrepancy patterns, and flags settlement breaks.
4. **`FraudDetectionSpecialistAgent`**: Explains alert vectors, identifies suspicious clusters, and recommends protective measures.
5. **`ExpenseOptimizationAdvisorAgent`**: Identifies duplicate subscriptions, vendor concentrations, and cost-reduction levers.
6. **`InvoiceArCollectionsSpecialistAgent`**: Analyzes overdue invoice tranches and suggests prioritized dunning sequences.
7. **`CashFlowForecastSpecialistAgent`**: Simulates cash deficits, runway milestones, and working capital buffers.
8. **`AuditComplianceInvestigatorAgent`**: Forensic inspection of immutable audit records, actor mutations, and SOX controls.
9. **`ReportSynthesizerSpecialistAgent`**: Synthesizes cross-service intelligence into board-level executive summaries.

#### Human Approval Gate
Whenever an agent plan involves high-impact financial actions (such as ledger write-offs, manual balance adjustments, or account freezes):
- The run is placed in `AWAITING_HUMAN_APPROVAL` status.
- Execution requires authorized human review via `POST /api/v1/ai/runs/{runReference}/review` (`APPROVE` or `REJECT`).

---

## 4. Technology Stack

- **Language:** Java 17 (Eclipse Temurin)
- **Framework:** Spring Boot 3.3.4
- **Cloud Components:** Spring Cloud 2023.0.3 (Gateway, Eureka Server, OpenFeign)
- **Security:** Spring Security 6, JJWT 0.12.6, BCrypt
- **Persistence:** Spring Data JPA, Hibernate 6, Flyway 10, PostgreSQL 16
- **Data Ingestion:** Apache Commons CSV 1.11.0
- **Documentation:** Springdoc OpenAPI (Swagger UI) 2.6.0
- **Testing:** JUnit 5, Mockito 5, AssertJ, H2 Database
- **Containers:** Docker, Docker Compose

---

## 5. Local Setup & Execution Guide

### Prerequisites
- JDK 17 installed and configured in `JAVA_HOME`
- Apache Maven 3.9+
- Docker & Docker Compose (optional, for containerized run)
- PostgreSQL 16 (for standalone local execution)

### 5.1. Build the Entire Backend Platform
```bash
cd Backend
mvn clean compile -DskipTests
```
To run tests across all modules:
```bash
cd Backend
mvn test
```

### 5.2. Running via Docker Compose (Recommended)
Launch all PostgreSQL databases, Eureka, API Gateway, and all domain microservices with one command:
```bash
cd Backend
docker compose up -d --build
```

Verify service status:
```bash
cd Backend
docker compose ps
```

Access the service registry:
- Eureka Dashboard: [http://localhost:8761](http://localhost:8761)
- API Gateway: [http://localhost:8080](http://localhost:8080)

### 5.3. Running Locally (Step-by-Step)
If running outside Docker:
1. Start PostgreSQL on `localhost:5432` with username `finsight_user` and password `finsight_secure_pass_2026`.
2. Run `Backend/docker/init-databases.sql` to initialize the 7 databases.
3. Open a terminal in `Backend/`:
   ```bash
   cd Backend
   ```
4. Start `eureka-server`:
   ```bash
   mvn spring-boot:run -pl eureka-server
   ```
5. Start `api-gateway`:
   ```bash
   mvn spring-boot:run -pl api-gateway
   ```
6. Start remaining domain services in separate terminals (from `Backend`):
   ```bash
   mvn spring-boot:run -pl auth-service
   mvn spring-boot:run -pl financial-data-service
   mvn spring-boot:run -pl reconciliation-service
   mvn spring-boot:run -pl fraud-detection-service
   mvn spring-boot:run -pl audit-reporting-service
   mvn spring-boot:run -pl financial-insights-service
   mvn spring-boot:run -pl ai-orchestrator-service
   ```

---

## 6. Postman API Collection

A fully configured Postman v2.1 collection is included in the root directory:
📁 **[`FinSight_AI_API_Collection.postman_collection.json`](./FinSight_AI_API_Collection.postman_collection.json)**

### How to Use:
1. Open Postman and click **Import** -> select `FinSight_AI_API_Collection.postman_collection.json`.
2. The collection uses the default environment variable `{{gateway_url}} = http://localhost:8080`.
3. Run the **01. Auth Service -> Login** request. The test script will **automatically extract and store the JWT access token** into `{{jwt_token}}`.
4. All subsequent requests automatically inherit the Bearer token for seamless testing!

---

## 7. Role-Based Access Control (RBAC)

The system includes pre-seeded roles:
- `ROLE_ADMIN`: Unrestricted administrative access across all services.
- `ROLE_FINANCIAL_ANALYST`: Manage financial data, initiate reconciliation, inspect insights.
- `ROLE_COMPLIANCE_OFFICER`: Review fraud alerts, investigate AML incidents, approve AI human gates.
- `ROLE_AUDITOR`: Read-only access to immutable audit trails and compliance reports.
- `ROLE_USER`: Standard business user.

---

## 8. License & Copyright
Developed for FinSight AI Platform © 2026. All rights reserved.
