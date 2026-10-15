# ==============================================================================
# FinSight AI - Single Command Microservices Orchestrator (PowerShell)
# Usage:
#   .\start-all-microservices.ps1
# ==============================================================================

$ErrorActionPreference = "Continue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$BackendDir = Join-Path $ScriptDir "Backend"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  FinSight AI -- Unified Microservices Ecosystem Launcher" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# Stage 1: Eureka Service Registry (:8761)
Write-Host ""
Write-Host "[Stage 1/3] Starting Eureka Service Discovery (:8761)..." -ForegroundColor Yellow
$pEureka = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "mvn", "spring-boot:run", "-pl", "eureka-server" `
    -WorkingDirectory $BackendDir `
    -RedirectStandardOutput (Join-Path $BackendDir "eureka-server.out.log") `
    -RedirectStandardError (Join-Path $BackendDir "eureka-server.err.log") `
    -PassThru -NoNewWindow

Write-Host "Waiting 10 seconds for Eureka registry initialization..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# Stage 2: Reactive API Gateway (:8080)
Write-Host ""
Write-Host "[Stage 2/3] Starting Reactive API Gateway (:8080)..." -ForegroundColor Yellow
$pGateway = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "mvn", "spring-boot:run", "-pl", "api-gateway" `
    -WorkingDirectory $BackendDir `
    -RedirectStandardOutput (Join-Path $BackendDir "api-gateway.out.log") `
    -RedirectStandardError (Join-Path $BackendDir "api-gateway.err.log") `
    -PassThru -NoNewWindow

Write-Host "Waiting 6 seconds for API Gateway initialization..." -ForegroundColor Gray
Start-Sleep -Seconds 6

# Stage 3: All 7 Domain Microservices
Write-Host ""
Write-Host "[Stage 3/3] Starting 7 Domain Microservices in Parallel..." -ForegroundColor Yellow

$modules = @(
    "auth-service",
    "financial-data-service",
    "reconciliation-service",
    "fraud-detection-service",
    "audit-reporting-service",
    "financial-insights-service",
    "ai-orchestrator-service"
)

$allPids = @()
if ($pEureka -and $pEureka.Id) { $allPids += $pEureka.Id }
if ($pGateway -and $pGateway.Id) { $allPids += $pGateway.Id }

foreach ($mod in $modules) {
    Write-Host "  -> Launching $mod..." -ForegroundColor White
    $proc = Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/c", "mvn", "spring-boot:run", "-pl", $mod `
        -WorkingDirectory $BackendDir `
        -RedirectStandardOutput (Join-Path $BackendDir "$mod.out.log") `
        -RedirectStandardError (Join-Path $BackendDir "$mod.err.log") `
        -PassThru -NoNewWindow
    if ($proc -and $proc.Id) {
        $allPids += $proc.Id
    }
}

# Save PIDs to file
$pidPath = Join-Path $ScriptDir ".microservices-pids.txt"
$allPids | Out-File -FilePath $pidPath -Force

Write-Host ""
Write-Host "=================================================================" -ForegroundColor Green
Write-Host "  All 9 Microservices + API Gateway + Eureka Successfully Dispatched!" -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green
Write-Host "  * Eureka Dashboard:     http://localhost:8761" -ForegroundColor Cyan
Write-Host "  * API Gateway:          http://localhost:8080" -ForegroundColor Cyan
Write-Host "  * Swagger UI Docs:      http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  * Middleware (BFF):     http://localhost:5000" -ForegroundColor Cyan
Write-Host "  * Frontend UI:          http://localhost:3000" -ForegroundColor Cyan
Write-Host ""
Write-Host "Log files are actively writing to Backend/*.out.log" -ForegroundColor Gray
Write-Host "To stop all microservices at any time, run: .\stop-all-microservices.ps1" -ForegroundColor Yellow
Write-Host "=================================================================" -ForegroundColor Green
