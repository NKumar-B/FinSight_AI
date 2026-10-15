#!/usr/bin/env bash
# ==============================================================================
# FinSight AI - Single Command Microservices Orchestrator (Bash)
# Usage:
#   ./start-all-microservices.sh          -> Starts all services locally in background
#   ./start-all-microservices.sh --docker -> Starts all services with Docker Compose
# ==============================================================================

set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
BACKEND_DIR="$DIR/Backend"

echo "================================================================="
echo "  FinSight AI — Unified Microservices Ecosystem Launcher"
echo "================================================================="

if [ "$1" == "--docker" ]; then
    echo "[Mode: Docker Compose] Launching all microservices, Eureka, Gateway & PostgreSQL..."
    cd "$BACKEND_DIR"
    docker compose up -d
    echo "Containers started! Check Eureka at http://localhost:8761"
    exit 0
fi

echo "[Mode: Native Maven/Java] Starting services in staged order..."
cd "$BACKEND_DIR"

# Stage 1: Eureka
echo "Starting Eureka Server (:8761)..."
nohup mvn spring-boot:run -pl eureka-server > "$BACKEND_DIR/eureka-server.log" 2>&1 &
echo $! >> "$DIR/.microservices.pids"
sleep 8

# Stage 2: Gateway
echo "Starting API Gateway (:8080)..."
nohup mvn spring-boot:run -pl api-gateway > "$BACKEND_DIR/api-gateway.log" 2>&1 &
echo $! >> "$DIR/.microservices.pids"
sleep 5

# Stage 3: Domain Microservices
SERVICES=("auth-service" "financial-data-service" "reconciliation-service" "fraud-detection-service" "audit-reporting-service" "financial-insights-service" "ai-orchestrator-service")

for svc in "${SERVICES[@]}"; do
    echo "Starting $svc..."
    nohup mvn spring-boot:run -pl "$svc" > "$BACKEND_DIR/$svc.log" 2>&1 &
    echo $! >> "$DIR/.microservices.pids"
done

echo "All 9 Microservices + Gateway + Eureka running in background!"
echo "Eureka:  http://localhost:8761"
echo "Gateway: http://localhost:8080"
