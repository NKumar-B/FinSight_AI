# FinSight AI — Enterprise Frontend Application

[![React](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6.3-blue.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-6.x-purple.svg)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-3.4-38bdf8.svg)](https://tailwindcss.com/)
[![Vitest](https://img.shields.io/badge/Vitest-2.x-green.svg)](https://vitest.dev/)

**FinSight AI Frontend** is a production-grade, enterprise fintech web application designed for autonomous multi-agent financial intelligence, deterministic 4-step reconciliation, AML velocity surveillance, forensic case investigations, and predictive cash-flow forecasting.

---

## 1. Quick Start

### Prerequisites
- Node.js `v20+` or `v24+`
- npm `v10+`

### Installation & Run
```bash
# Navigate to Frontend directory
cd Frontend

# Install dependencies (already installed)
npm install

# Start development server on http://localhost:3000
npm run dev
```

### Production Build & Automated Tests
```bash
# Run Vitest test suite
npm test

# Run strict TypeScript check and Vite production bundle
npm run build
```

---

## 2. Default Evaluator Personas (Pre-Seeded)

| Persona / Role | Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Platform Administrator** | `admin` | `AdminSecure2026!` | Universal access across all 14 modules, user management, and health telemetry |
| **Financial Analyst** | `analyst_john` | `SecurePass2026!` | Treasury accounts, 4-step reconciliation, expense intelligence, invoices, cash-flow |
| **Compliance Officer** | `compliance_officer` | `ComplianceSecure2026!` | AML velocity alerts, forensic evidence dossiers, and SAR filing |
| **External Auditor** | `auditor_pwc` | `AuditorSecure2026!` | Read-only access to immutable audit trails, signed report packages, and hash ledger |

---

## 3. Modular Architecture Overview

```
Frontend/
├── index.html                   # HTML5 entry with Google Fonts & Meta tags
├── vite.config.ts               # Vite bundler, proxy to port 8080, Vitest setup
├── tailwind.config.js           # Enterprise digital banking design tokens
├── src/
│   ├── app/
│   │   ├── App.tsx              # Root React component
│   │   ├── router.tsx           # React Router v6 with Protected & Role-Gated Routes
│   │   └── providers.tsx        # TanStack Query & Auth Context providers
│   ├── components/
│   │   ├── ui/                  # Button, Card, Badge, Input, Select, Modal, DataTable, Skeleton
│   │   └── layout/              # AppShell, Sidebar (5 groups), TopBar, Breadcrumbs
│   ├── features/
│   │   ├── auth/                # Login, ForgotPassword (OTP), ProtectedRoute, useAuth
│   │   ├── dashboard/           # Operational KPI cards & financial visualizers
│   │   ├── financial-data/      # Accounts & transaction ledgers with CSV drag-and-drop
│   │   ├── reconciliation/      # 4-Stage deterministic matching engine & discrepancies
│   │   ├── fraud-monitoring/    # AML surveillance queue, risk gauges, SAR filing
│   │   ├── investigations/      # Forensic case dossiers, evidence provenance, action gates
│   │   ├── agent-activity/      # 9 Specialist AI agents directory & execution traces
│   │   ├── expenses/            # Spend analytics & cost anomaly detection
│   │   ├── invoices-receivables/# AR DSO aging matrix & itemized receivables
│   │   ├── cash-flow/           # 30/60/90-Day predictive cash runway & confidence bands
│   │   ├── ai-copilot/          # Gemini conversational financial assistant
│   │   ├── reports/             # Downloadable regulatory & executive packages
│   │   ├── audit-history/       # Cryptographic audit logs with correlation IDs
│   │   └── user-management/     # Admin-only role assignment & account status
│   ├── services/
│   │   └── api/
│   │       ├── client.ts        # Axios client with JWT interceptor & refresh logic
│   │       ├── errorHandler.ts  # Standardized backend error parser
│   │       └── finSightApi.ts   # Unified API service with live gateway & test fixtures
│   ├── types/                   # TypeScript DTO interfaces matching Spring Boot
│   └── utils/                   # Currency, date, percentage formatters, and status styling
```

---

## 4. Backend Gateway Integration

The frontend connects directly to the Spring Boot **API Gateway** on `http://localhost:8080`:
* Proxy is configured in `vite.config.ts` (`/api` -> `http://localhost:8080`).
* Configurable via `.env`: `VITE_API_BASE_URL=http://localhost:8080`.
* Seamlessly falls back to verified offline financial fixtures when any backend service is initializing, guaranteeing uninterrupted UI testing and evaluator exploration.
