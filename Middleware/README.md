# FinSight AI — Middleware & Integration Layer

[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red.svg)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-orange.svg)](https://www.rabbitmq.com/)

This directory is designated for the **FinSight AI** distributed middleware, event streaming configurations, and caching infrastructure.

---

## Directory Scope & Responsibilities

```
Middleware/
├── event-broker/            # Apache Kafka / RabbitMQ cluster configs & topic definitions
│   ├── topics.json          # Topics: financial.transactions, fraud.alerts, audit.events
│   └── docker-compose-broker.yml
├── caching/                 # Distributed Redis caching layer configurations
│   ├── redis.conf           # TTL policies for JWT blacklisting and OTP tokens
│   └── sentinel.conf
├── reverse-proxy/           # NGINX / Envoy edge proxy configurations
│   ├── nginx.conf           # SSL termination, rate limiting, and gateway routing
│   └── certs/
└── webhooks/                # External banking & payment gateway webhook adapters
```

---

## Key Middleware Capabilities

1. **Distributed Event Streaming**:
   - Publishes transaction events from `financial-data-service` to asynchronous fraud screening and deterministic reconciliation pipelines.
2. **Distributed Cache (Redis)**:
   - High-throughput token revocation lists (blacklisting on logout).
   - Fast TTL management for OTP tokens (5-minute window).
   - Real-time fraud velocity counter store.
3. **Edge Security & Rate Limiting**:
   - NGINX/Envoy ingress for DDoS protection, TLS 1.3 termination, and CORS standardization.
