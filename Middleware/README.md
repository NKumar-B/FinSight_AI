# FinSight AI — Distributed Middleware, API Mediation BFF & Event Streaming Layer

[![Node.js](https://img.shields.io/badge/Node.js-24.x-green.svg)](https://nodejs.org/)
[![Express](https://img.shields.io/badge/Express-4.21-blue.svg)](https://expressjs.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)](https://www.typescriptlang.org/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red.svg)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black.svg)](https://kafka.apache.org/)
[![NGINX](https://img.shields.io/badge/NGINX-1.25-darkgreen.svg)](https://nginx.org/)

The **Middleware** layer serves as the high-throughput **Backend-For-Frontend (BFF)**, API mediation gateway, distributed event broker, and caching fabric for the **FinSight AI** platform. It bridges the modern React 18 / TypeScript Frontend with the 9 Spring Boot microservices.

---

## 1. Directory Structure

```
Middleware/
├── package.json                   # Dependencies: Express, Axios, CORS, Dotenv, TypeScript, TSX
├── tsconfig.json                  # TypeScript compiler configuration (ES2022 / NodeNext)
├── docker-compose-middleware.yml  # Multi-container orchestration (BFF + Redis + NGINX)
├── README.md                      # Architecture documentation & API Integration Reference
├── src/
│   ├── server.ts                  # Main Express entrypoint (Port 5000)
│   ├── config/
│   │   └── env.ts                 # Environment secrets, port mappings, and gateway URLs
│   ├── middleware/
│   │   ├── correlation.ts         # X-Correlation-ID distributed tracing middleware
│   │   ├── authGuard.ts           # JWT extraction, verification & RBAC role guards
│   │   ├── rateLimiter.ts         # In-memory sliding window rate limiter
│   │   └── errorHandler.ts        # Centralized RFC 7807 error formatter
│   ├── services/
│   │   ├── backendClient.ts       # Resilient HTTP proxy to Spring Boot microservices
│   │   ├── cacheService.ts        # Redis caching layer with in-memory TTL fallback
│   │   ├── cryptoService.ts       # ChaCha20-Poly1305 & SHA-256 idempotency calculation
│   │   └── eventBroker.ts         # Topic publisher for Kafka & RabbitMQ event streams
│   └── routes/
│       ├── index.ts               # Master router aggregating all /api/v1 domain routes
│       ├── auth.routes.ts         # Authentication, registration, 2FA OTP, refresh & RBAC menu
│       ├── financial.routes.ts    # Accounts, ledger transactions & SHA-256 ingestion
│       ├── reconciliation.routes.ts # Autonomous 4-step reconciliation & discrepancy review
│       ├── fraud.routes.ts        # Multi-factor AML evaluation, rules & investigations
│       ├── insights.routes.ts     # Invoices, AR aging buckets & cash-flow runway models
│       ├── ai.routes.ts           # Copilot natural language execution & 9 AI specialist agents
│       ├── audit.routes.ts        # Immutable audit trail & regulatory compliance reports
│       ├── dashboard.routes.ts    # BFF composite summary aggregation endpoint
│       └── webhooks.routes.ts     # Banking & payment gateway webhook adapters
├── caching/
│   ├── redis.conf                 # Redis production configuration (LRU policy, AOF)
│   └── sentinel.conf              # Redis Sentinel high availability configuration
├── event-broker/
│   ├── topics.json                # Topic schemas: financial.transactions, fraud.alerts, audit.events
│   └── docker-compose-broker.yml  # Kafka / Zookeeper / RabbitMQ cluster definition
└── reverse-proxy/
    └── nginx.conf                 # Edge reverse proxy (SSL termination, rate limiting, gzip)
```

---

## 2. Frontend-to-Backend API Integration Matrix

The Middleware directly binds each Frontend UI component to the corresponding Spring Boot domain microservice, applying caching, correlation IDs, and resilient fallback handling:

| Frontend UI Feature / Page | Middleware BFF Route | Target Spring Boot Microservice | Key Architectural Operations |
|---|---|---|---|
| **Login & Register Form** (`LoginPage.tsx`) | `POST /api/v1/auth/login`<br>`POST /api/v1/auth/register` | `auth-service` (:8081)<br>or `api-gateway` (:8080) | Password verification, ChaCha20 crypto check, role assignment (`ROLE_ADMIN`, `ROLE_FINANCIAL_ANALYST`), OTP challenge dispatch. |
| **2FA OTP Card** (`LoginPage.tsx`) | `POST /api/v1/auth/otp/verify`<br>`POST /api/v1/auth/otp/dispatch` | `auth-service` (:8081) | Verifies 6-digit OTP code against Redis cache (TTL: 5m), issues authorized JWT session. Fast-fill sandbox code: `834912`. |
| **Role-Aware Sidebar** (`AppShell.tsx`) | `GET /api/v1/auth/menu` | `auth-service` (:8081) | Filters navigation links dynamically based on user identity authorities. |
| **Dashboard KPIs & Charts** (`DashboardPage.tsx`) | `GET /api/v1/dashboard/summary` | **BFF Aggregation** (Composite from: `:8082`, `:8083`, `:8084`, `:8087`) | Aggregates treasury liquidity, 7-day clearing volume, risk matrix, expense breakdown, and agent telemetry in **one single round-trip**. |
| **Accounts & Balances** (`AccountsPage.tsx`) | `GET /api/v1/accounts`<br>`POST /api/v1/accounts` | `financial-data-service` (:8082) | Multi-currency chart of accounts, cached in Redis with 60s TTL; invalidates on account creation. |
| **Transaction Ingestion** (`TransactionsPage.tsx`) | `POST /api/v1/transactions/ingest`<br>`GET /api/v1/transactions` | `financial-data-service` (:8082) | Generates **SHA-256 idempotency key** to prevent duplicates; broadcasts `financial.transactions` to event broker. |
| **Reconciliation Matching** (`ReconciliationPage.tsx`) | `POST /api/v1/reconciliation/runs`<br>`GET /api/v1/reconciliation/runs` | `reconciliation-service` (:8083) | Triggers 4-step deterministic matching engine (Exact, Tolerance ±3d/0.05, 1-to-many aggregate); publishes `reconciliation.events`. |
| **Discrepancy Review** (`DiscrepanciesPage.tsx`) | `POST /api/v1/reconciliation/review`<br>`GET /api/v1/reconciliation/discrepancies` | `reconciliation-service` (:8083) | Records reviewer actions (`APPROVE_MATCH`, `REJECT_MATCH`, `WRITE_OFF`) and logs immutable audit trail. |
| **Fraud Alerts & AML** (`FraudAlertsPage.tsx`) | `GET /api/v1/fraud/alerts`<br>`POST /api/v1/fraud/evaluate` | `fraud-detection-service` (:8084) | Evaluates multi-factor risk score (0–100) across velocity, cross-border, and structuring rules; publishes `fraud.alerts`. |
| **Forensic Investigations** (`InvestigationsPage.tsx`) | `GET /api/v1/investigations`<br>`GET /api/v1/investigations/:id/evidence` | `fraud-detection-service` (:8084) | Retrieves structured case files, MT103 SWIFT logs, velocity score cards, and sanction watchlist graph evidence. |
| **AI Financial Copilot** (`AiCopilotPage.tsx`) | `POST /api/v1/ai/execute` | `ai-orchestrator-service` (:8087) | Natural language query decomposition, OpenFeign tool invocation across services, and human gate checks. |
| **AI Specialist Agents** (`AiAgentsPage.tsx`) | `GET /api/v1/ai/agents`<br>`POST /api/v1/ai/runs/:id/review` | `ai-orchestrator-service` (:8087) | Fleet telemetry for all 9 financial agents; records authorized human approval decisions (`APPROVE` / `REJECT`). |
| **Invoices & AR Aging** (`InvoicesPage.tsx`) | `GET /api/v1/insights/invoices`<br>`GET /api/v1/insights/aging` | `financial-insights-service` (:8086) | Aging bucket tranches (`0-30`, `31-60`, `61-90`, `90+` days overdue) and vendor concentration intelligence. |
| **Cash-Flow Forecast** (`CashFlowPage.tsx`) | `GET /api/v1/insights/analytics/cash-flow` | `financial-insights-service` (:8086) | 30/60/90-day predictive runway modeling with confidence bands. |
| **Audit Log Trail** (`AuditEventsPage.tsx`) | `GET /api/v1/audit/events`<br>`POST /api/v1/audit/reports` | `audit-reporting-service` (:8085) | Immutable append-only audit entries with actor attribution, before/after JSON states, and SOX export reports. |
| **Banking Webhooks** (`External APIs`) | `POST /api/v1/webhooks/banking`<br>`POST /api/v1/webhooks/payment-gateway` | **Middleware Adapter Layer** | Ingests real-time banking webhooks (Plaid, Stripe, Swift) and pushes to Kafka topics. |

---

## 3. Core Architectural Capabilities

### 3.1. Distributed Tracing (`X-Correlation-ID`)
Every incoming HTTP request passing through the Middleware is tagged with an `X-Correlation-ID` header (or creates a UUID `c-xxxx`). The header is propagated across downstream Spring Boot microservices and thread-local logs, ensuring end-to-end auditability.

### 3.2. Resilient Fallback Handling
The `BackendClient` service intelligently routes requests:
- If the Spring Boot backend microservice is reachable, it forwards the live production data.
- If the microservice is temporarily stopped or undergoing maintenance, it serves realistic enterprise fallback fixtures so the Frontend never crashes.

### 3.3. Distributed Event Streaming (Kafka / RabbitMQ)
Configured with topic topologies in `event-broker/topics.json`:
- `financial.transactions`: Inbound and cleared ledger entries.
- `fraud.alerts`: Velocity violations and high-risk AML alerts.
- `reconciliation.events`: Match run completions and discrepancies.
- `ai.agent.telemetry`: Tool dispatches and approval gates.
- `audit.events`: Append-only compliance log.

### 3.4. Distributed Caching (Redis)
- TTL policies defined in `caching/redis.conf`.
- Automatic caching for read-heavy endpoints (`/api/v1/accounts`, `/api/v1/dashboard/summary`).
- 5-minute TTL expiration for 2FA OTP codes.
- 24-hour TTL for SHA-256 transaction idempotency hashes.

---

## 4. Running the Middleware

### Local Execution (Standalone)
```bash
cd Middleware
npm install
npm run build
npm start
```
The server will start on **`http://localhost:5000`**.

### Development Mode (with Hot Reloading)
```bash
cd Middleware
npm run dev
```

### Full Dockerized Stack (Middleware + Redis + NGINX)
```bash
cd Middleware
docker compose -f docker-compose-middleware.yml up -d
```

### Health Check Endpoint
```bash
curl http://localhost:5000/health
```
Response:
```json
{
  "status": "UP",
  "service": "finsight-middleware-bff",
  "port": 5000,
  "correlationId": "c-xxxx",
  "backendGatewayTarget": "http://localhost:8080"
}
```
