# FinSight AI — Backend Microservices Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

This directory houses the entire backend microservices ecosystem for the **FinSight AI** platform.

---

## Architecture & Modules

```
Backend/
├── pom.xml                               # Root Maven Reactor POM
├── docker-compose.yml                    # Multi-container orchestration
├── docker/                               # DB initialization & configs
├── .env / .env.example                   # Environment configuration & secrets
├── finsight-common/                      # Shared DTOs, ChaCha20-Poly1305, JWT, exceptions
├── eureka-server/           (:8761)      # Netflix Eureka Service Discovery
├── api-gateway/             (:8080)      # Spring Cloud Gateway (Routing & JWT filter)
├── auth-service/            (:8081)      # Authentication, OTP (Email/SMS), RBAC Menu
├── financial-data-service/  (:8082)      # Account management & transaction ledgers
├── reconciliation-service/  (:8083)      # Deterministic 4-step ledger reconciliation
├── fraud-detection-service/ (:8084)      # Velocity & anomaly detection, SAR filing
├── audit-reporting-service/ (:8085)      # Immutable audit trail & compliance reports
├── financial-insights-service/ (:8086)   # Cash-flow forecasting & AR aging analysis
└── ai-orchestrator-service/ (:8087)      # 9 Specialist AI agents & Google Gemini ReAct
```

---

## Quick Start

### 1. Build All Modules
```bash
cd Backend
mvn clean compile -DskipTests
```

### 2. Run with Docker Compose
```bash
docker compose up -d
```

### 3. Run Locally (Development Order)
1. **Eureka Server** (`:8761`): `mvn spring-boot:run -pl eureka-server`
2. **API Gateway** (`:8080`): `mvn spring-boot:run -pl api-gateway`
3. **Auth Service** (`:8081`): `mvn spring-boot:run -pl auth-service`
4. **Domain Services** (`:8082` - `:8086`)
5. **AI Orchestrator** (`:8087`): `mvn spring-boot:run -pl ai-orchestrator-service`
