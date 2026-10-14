# FinSight AI — Platform Documentation Repository

This directory contains the formal, enterprise-grade architecture documentation and technical deliverables for the **FinSight AI** platform developed during the **Techurate Internship Program**.

---

## Deliverables in this Directory

| File Name | Format | Description |
|---|---|---|
| **[`FinSight_AI_Comprehensive_Documentation.docx`](./FinSight_AI_Comprehensive_Documentation.docx)** | Microsoft Word (`.docx`) | **Primary Deliverable:** Comprehensive enterprise documentation covering full architecture, 9 microservices, security/ChaCha20, Frontend PWA, i18n translator, 2FA OTP, admin dynamic theming, and test matrix. |
| **[`generate_docs.py`](./generate_docs.py)** | Python Script | Automated generator script used to dynamically build and format the Word document with custom typography, tables, and embedded high-resolution visual assets. |

---

## Document Table of Contents

1. **Document Control & Executive Metadata**
2. **1. Executive Summary & Platform Overview**
   - Autonomous reconciliation & multi-agent fraud investigation goals
   - Core value metrics: 99.98% gateway availability, <12ms ChaCha20 latency
3. **2. Decoupled Three-Tier Architectural Blueprint**
   - `Backend/` (Spring Boot 3.3.4 microservices & Eureka)
   - `Frontend/` (React 18 + Vite + TypeScript PWA)
   - `Middleware/` (Kafka, RabbitMQ, Redis, NGINX)
4. **3. Backend Microservices Ecosystem & Port Topology**
   - `eureka-server` (:8761)
   - `api-gateway` (:8080)
   - `auth-service` (:8081)
   - `financial-data-service` (:8082)
   - `reconciliation-service` (:8083)
   - `fraud-detection-service` (:8084)
   - `audit-reporting-service` (:8085)
   - `financial-insights-service` (:8086)
   - `ai-orchestrator-service` (:8087)
5. **4. Security Architecture & Sensitive Data Protection**
   - ChaCha20-Poly1305 AEAD authenticated symmetric encryption
   - Zero-leak `.env` isolation
   - End-to-end distributed tracing via `X-Correlation-ID`
   - Pre-seeded enterprise RBAC roles & menu structures
6. **5. Frontend Web Application Architecture & Modern UX**
   - Split 2-column responsive layout with 3D fintech hero asset
   - Integrated "Register with Us" enterprise sign-up flow
   - Two-Factor Authentication (2FA) OTP verification card with sandbox fast-fill
   - Progressive Web App (PWA) manifest & service worker caching
   - Multilingual translation engine (English & Hindi)
   - Light and dark theme engine with WCAG contrast
   - Admin live theme/color customizer (6 enterprise palettes + custom hex picker)
7. **6. Middleware & Distributed Infrastructure**
   - Event streaming (Kafka & RabbitMQ)
   - Distributed caching (Redis token revocation & OTP TTL)
   - Edge proxy (NGINX/Envoy SSL termination)
8. **7. Testing, Verification & Quality Assurance**
   - Maven reactor multi-module build validation
   - Frontend Vitest unit test suite (9/9 passed)
   - Postman v2.1 collection with automated JWT chaining
   - OpenAPI / Swagger UI 2.6.0 interactive documentation
9. **8. Comprehensive Milestone Completion Matrix**
   - 100% completion status across all 18 project requirements

---

## Embedded Document Figures & Visual Telemetry

The Word document (`.docx`) includes all high-resolution platform screenshots and visual assets stored in `Documentation/images/`:

1. **Figure 1**: FinSight AI Enterprise Identity & 3D Financial Intelligence Visual Hero
2. **Figure 2**: Enterprise Split 2-Column Authentication Layout (`auth_split_layout_login.png`)
3. **Figure 3**: Two-Factor Authentication (2FA) OTP Verification Card (`two_factor_auth_otp_verification.png`)
4. **Figure 4**: FinSight AI Copilot Workspace with Gemini 1.5 Pro React Online & Navigation Rail (`finsight_ai_copilot_workspace.png`)
5. **Figure 5**: Financial Operations Dashboard in Light Theme (`financial_dashboard_light_theme.png`)
6. **Figure 6**: Hindi Language Localization & Admin Live Dynamic Brand Color Customizer (`hindi_translation_admin_color_customizer.png`)

---

## How to Regenerate the Word Document

If you ever make updates to the architecture and wish to re-compile the Word document:

```bash
cd Documentation
python generate_docs.py
```

