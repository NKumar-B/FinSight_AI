# FinSight AI — Enterprise Financial Intelligence & Multi-Agent Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![React](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6.3-blue.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-6.x-purple.svg)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-3.4-38bdf8.svg)](https://tailwindcss.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Vitest](https://img.shields.io/badge/Vitest-2.x-green.svg)](https://vitest.dev/)
[![Vercel Ready](https://img.shields.io/badge/Deployment-Vercel%20%7C%20Netlify-black.svg)](https://vercel.com/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

> **FinSight AI** is a production-grade, distributed fintech platform designed for autonomous multi-agent financial intelligence, deterministic 4-step reconciliation, AML velocity surveillance, forensic case investigations, predictive cash-flow forecasting, AR DSO aging, cryptographic audit trails, and human-in-the-loop review approvals.

---

## 1. High-Level Project Structure

The project is structured into three decoupled architectural tiers:

```
FinSight_AI_Application/
├── Frontend/                                  # Web client tier (React 18 / Vite 6 / TypeScript / Tailwind CSS)
│   ├── src/                                   # 14 Modular feature suites, UI design system & API services
│   ├── public/                                # Static PWA assets, icons, and _redirects for Netlify/Cloudflare
│   ├── vercel.json                            # Vercel SPA routing rewrite rules (prevents 404s on reload)
│   ├── vite.config.ts                         # Vite bundler, proxy configuration & Vitest test setup
│   ├── tsconfig.app.json                      # Strict TypeScript production compiler options
│   └── package.json                           # Frontend scripts & dependencies
├── Backend/                                   # Multi-module Spring Boot 3.3.4 microservices ecosystem
│   ├── eureka-server/                         # Centralized Service Registry (:8761)
│   ├── api-gateway/                           # Reactive Gateway & JWT security filter (:8080)
│   ├── auth-service/                          # User Auth, ChaCha20 crypto, OTP & RBAC (:8081)
│   ├── financial-data-service/                # Treasury accounts & transaction ledger ingestion (:8082)
│   ├── reconciliation-service/                # Deterministic 4-stage matching engine (:8083)
│   ├── fraud-detection-service/               # Multi-factor AML & velocity anomaly radar (:8084)
│   ├── audit-reporting-service/               # Immutable cryptographic audit trails (:8085)
│   ├── financial-insights-service/            # Cash-flow runway & AR DSO aging matrix (:8086)
│   ├── ai-orchestrator-service/               # 9 Specialist financial AI agents (:8087)
│   ├── finsight-common/                       # Shared DTOs, ChaCha20 AEAD & security filters
│   ├── pom.xml                                # Parent Maven Reactor POM
│   └── docker-compose.yml                     # Multi-container local orchestration
├── Middleware/                                # Kafka, RabbitMQ, Redis caching configurations
├── Documentation/                             # Architectural blueprints and API documentation
├── FinSight_AI_API_Collection.postman_collection.json # Automated Postman v2.1 API test suite
├── start-all-microservices.ps1                # 1-Click launcher for all microservices (PowerShell)
├── start-all-microservices.bat                # 1-Click launcher for all microservices (CMD / Batch)
├── start-all-microservices.sh                 # 1-Click launcher for all microservices (Bash / Linux / macOS)
├── stop-all-microservices.ps1                 # Graceful termination script for all background services
└── stop-all-microservices.bat                 # Windows batch stopper script
```

---



## 2. Frontend Application Tier

The frontend is a standalone, enterprise-grade Single Page Application (SPA) designed with digital banking design tokens, fluid dark/light theming, high-density financial data tables, and dynamic visual analytics.

### 2.1. Implemented Modules & Features

| Module | Route | Key Capabilities |
|---|---|---|
| **Landing & Overview** | `/` | Product highlights, enterprise security credentials, interactive features preview. |
| **Authentication & RBAC** | `/login` | Secure JWT authentication, ChaCha20-Poly1305 OTP verification, password reset, and role-gated routes. |
| **Executive Dashboard** | `/dashboard` | Real-time liquidity KPI cards, reconciliation throughput, AML risk radars, and volume trends. |
| **Financial Data & Ledger** | `/financial-data` | Multi-currency treasury account ledgers, institutional bank document validator (Chase/Citi/BoA), and drag-and-drop CSV/PDF ingestion. |
| **4-Step Reconciliation** | `/reconciliation` | Deterministic matching engine (Exact, Tolerance, 1-to-many), discrepancy breakdown, and manual adjustment/write-off actions. |
| **AML Fraud Monitoring** | `/fraud` | Velocity anomaly alerts, risk score gauges (0-100), severity badges, SAR filing triggers, and detection rule analysis. |
| **Forensic Investigations** | `/investigations` | Evidence dossier provenance, chain of custody, cryptographic SHA-256 hash ledger checks, and action gate approvals. |
| **Specialist AI Directory** | `/agents` | Directory of 9 autonomous financial agents, live thought-and-action execution traces, and human review sign-off modal. |
| **Expense Intelligence** | `/expenses` | Category spend breakdowns (SaaS, Payroll, Infrastructure), vendor concentration risks, and anomaly flags. |
| **Invoices & AR Aging** | `/invoices` | Accounts receivable aging matrix (`0-30`, `31-60`, `61-90`, `90+` days overdue), DSO calculation, and collections tracking. |
| **Predictive Cash Flow** | `/cash-flow` | 30/60/90-Day predictive cash runway model with confidence interval bands and inflow/outflow balance trajectories. |
| **AI Copilot** | `/copilot` | Natural language conversational copilot for treasury inquiries, anomaly explanations, and operational queries. |
| **Reports & Export Engine** | `/reports` | Institutional PDF & Excel export engine with cryptographic digital signatures and executive packages. |
| **Cryptographic Audit Log** | `/audit` | Immutable audit event log with correlation IDs (`X-Correlation-ID`), actor provenance, and state before/after diffs. |
| **User Management** | `/users` | Administrator console for user account provisioning, role assignment, and access revocation. |

### 2.2. Offline Sandbox Evaluation & Cloud Deployment

* **Zero-Downtime Sandbox Evaluation**: Built-in mock fixtures across `finSightApi.ts` and `authApi.ts` ensure the UI functions seamlessly even if backend services are offline.
* **Standalone Deployment (Vercel & Netlify)**: The frontend can be hosted independently on **Vercel** (`vercel.json`) or **Netlify** (`_redirects`) without running the backend microservices.
* **Live Gateway Integration**: Set `VITE_API_BASE_URL` in `.env` to connect directly to the Spring Cloud API Gateway.

---

## 3. Backend Microservices Ecosystem

| Service Name | Port | Database | Primary Responsibility | Key Endpoints |
|---|---|---|---|---|
| **`eureka-server`** | `8761` | — | Service discovery & registration registry | `/eureka/*`, `/` |
| **`api-gateway`** | `8080` | — | Reactive reverse proxy, centralized JWT authentication filter, correlation tracking | `/api/v1/**` |
| **`auth-service`** | `8081` | `finsight_auth` | User authentication, registration, refresh tokens, ChaCha20-Poly1305 OTP, and RBAC menus | `/api/v1/auth/**`, `/api/v1/users/**` |
| **`financial-data-service`** | `8082` | `finsight_financial_data` | Account management, transaction ingestion (CSV/JSON), SHA-256 idempotency deduplication | `/api/v1/accounts/**`, `/api/v1/transactions/**` |
| **`reconciliation-service`** | `8083` | `finsight_reconciliation` | Deterministic matching engine (Exact, Tolerance, 1-to-many), discrepancy review workflows | `/api/v1/reconciliation/runs/**`, `/api/v1/reconciliation/discrepancies/**` |
| **`fraud-detection-service`** | `8084` | `finsight_fraud` | Multi-factor AML velocity rules, risk scoring (0-100), alert lifecycle investigation | `/api/v1/fraud/evaluate`, `/api/v1/fraud/alerts/**` |
| **`audit-reporting-service`** | `8085` | `finsight_audit` | Immutable audit trail persistence, state before/after snapshots, compliance reports | `/api/v1/audit/events/**`, `/api/v1/audit/reports/**` |
| **`financial-insights-service`** | `8086` | `finsight_insights` | Invoice balance tracking, AR aging buckets, expense breakdown, 30/60/90-day cash flow runways | `/api/v1/insights/invoices/**`, `/api/v1/insights/analytics/**` |
| **`ai-orchestrator-service`** | `8087` | `finsight_ai` | 9 specialist financial AI agents, microservice tool invocation, human approval review gates | `/api/v1/ai/execute`, `/api/v1/ai/runs/**`, `/api/v1/ai/tools`, `/api/v1/ai/agents` |

---

## 4. Specialist Financial AI Agents

The `ai-orchestrator-service` hosts 9 domain specialist agents coordinated by a meta-orchestrator:

1. **`SupervisoryMetaOrchestratorAgent`**: Decomposes natural language queries, classifies domain intent, coordinates specialists, and enforces human review sign-offs.
2. **`FinancialDataAnalystAgent`**: Ingestion integrity, account balances, and multi-currency exposures.
3. **`ReconciliationSpecialistAgent`**: Matches complex entries, explains discrepancy patterns, and handles settlement breaks.
4. **`FraudDetectionSpecialistAgent`**: Evaluates velocity vectors, flags shell entities, and suggests transaction holds.
5. **`ExpenseOptimizationAdvisorAgent`**: Discovers duplicate subscriptions, vendor concentrations, and cost-reduction opportunities.
6. **`InvoiceArCollectionsSpecialistAgent`**: Analyzes overdue invoice tranches and suggests prioritized dunning sequences.
7. **`CashFlowForecastSpecialistAgent`**: Simulates cash deficits, runway milestones, and working capital buffers.
8. **`AuditComplianceInvestigatorAgent`**: Forensic inspection of immutable audit records, actor mutations, and regulatory controls.
9. **`ReportSynthesizerSpecialistAgent`**: Synthesizes cross-service intelligence into board-level executive summaries.

---

## 5. Pre-Seeded Evaluator Personas

The platform includes 4 pre-configured personas for role-based testing:

| Persona / Role | Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Platform Administrator** | `admin` | `AdminSecure2026!` | Universal access across all 14 modules, user management, and health telemetry |
| **Financial Analyst** | `analyst_john` | `SecurePass2026!` | Treasury accounts, 4-step reconciliation, expense intelligence, invoices, cash-flow |
| **Compliance Officer** | `compliance_officer` | `ComplianceSecure2026!` | AML velocity alerts, forensic evidence dossiers, and SAR filing |
| **External Auditor** | `auditor_pwc` | `AuditorSecure2026!` | Read-only access to immutable audit trails, signed report packages, and hash ledger |

---

## 6. Quick Start & Execution Guide

### Prerequisites
* **Java 17** (JDK 17 configured in `JAVA_HOME`)
* **Apache Maven 3.9+**
* **Node.js 20+** and **npm 10+**
* **Docker & Docker Compose** (or local PostgreSQL 16)

---

### Option A: Launching All Microservices (1-Click)

Launch Eureka, API Gateway, and all 7 domain microservices using the root scripts:

* **Windows (PowerShell)**:
  ```powershell
  .\start-all-microservices.ps1
  ```
* **Windows (Command Prompt)**:
  ```cmd
  start-all-microservices.bat
  ```
* **Linux / macOS (Bash)**:
  ```bash
  chmod +x start-all-microservices.sh
  ./start-all-microservices.sh
  ```

To gracefully terminate all background microservice processes:
```powershell
.\stop-all-microservices.ps1
```

---

### Option B: Running via Docker Compose

```bash
cd Backend
docker compose up -d --build
docker compose ps
```

* **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)
* **API Gateway**: [http://localhost:8080](http://localhost:8080)

---

### Option C: Running the Frontend Standalone

```bash
# Navigate to Frontend directory
cd Frontend

# Install dependencies
npm install

# Start development server on http://localhost:3000
npm run dev

# Run automated Vitest test suite
npm test

# Run strict TypeScript verification and production build
npm run build
```

---

### Option D: Deploying Only the Frontend (Vercel / Netlify)

FinSight AI's frontend is fully pre-configured for standalone cloud deployment:

#### Deploying on Vercel:
1. Connect your GitHub repository on [Vercel](https://vercel.com/).
2. Set **Root Directory** to `Frontend`.
3. Set **Framework Preset** to `Vite`.
4. Ensure **Build Command** is `npm run build` and **Output Directory** is `dist`.
5. *(Optional)* Add `VITE_API_BASE_URL` in Environment Variables if connecting to a hosted backend.
6. `Frontend/vercel.json` automatically handles SPA routing rewrites so page reloads work smoothly.


---

## 7. Automated Testing & Verification

### Backend Tests
```bash
cd Backend
mvn test
```

### Frontend Tests
```bash
cd Frontend
npm test
```
* **6 Test Suites / 24 Tests**: Covers bank document validation, JWT session guards, login flows, button variants, report generation, and currency/percentage formatters.

---

## 8. Postman API Collection

A pre-configured Postman v2.1 collection is included in the root directory:
📁 **[`FinSight_AI_API_Collection.postman_collection.json`](./FinSight_AI_API_Collection.postman_collection.json)**

1. Import the collection into Postman.
2. The collection defaults to `{{gateway_url}} = http://localhost:8080`.
3. Run the **01. Auth Service -> Login** request; the script will **automatically extract and store the JWT access token** into `{{jwt_token}}` for subsequent requests.

---

## 9. License & Copyright

Developed for FinSight AI Platform © 2026. All rights reserved.

---

<p align="center">
  <b> Designed & Developed with ❤️ by Nithin Kumar Badduluri</b>
  <br/>
  <a href="https://www.linkedin.com/in/nithin-kumar-badduluri-3942512a6">LinkedIn</a> •
  <a href="https://github.com/NKumar-B">GitHub</a> •
  <a href="https://nithin-portfolio2-steel.vercel.app/">Portfolio</a>
</p>

<p align="center">
  © 2026 FinSight AI. All rights reserved.
</p>
