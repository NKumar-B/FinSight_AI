# ==============================================================================
# FinSight AI - Single Command Microservices Stopper (PowerShell)
# Usage:
#   .\stop-all-microservices.ps1
# ==============================================================================

$ErrorActionPreference = "SilentlyContinue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$pidPath = Join-Path $ScriptDir ".microservices-pids.txt"

Write-Host "=================================================================" -ForegroundColor Yellow
Write-Host "  Stopping FinSight AI Microservices..." -ForegroundColor Yellow
Write-Host "=================================================================" -ForegroundColor Yellow

# Kill by PID file
if (Test-Path $pidPath) {
    $pids = Get-Content $pidPath
    foreach ($p in $pids) {
        if ($p -and $p.Trim()) {
            Stop-Process -Id ([int]$p.Trim()) -Force -ErrorAction SilentlyContinue
        }
    }
    Remove-Item $pidPath -Force -ErrorAction SilentlyContinue
}

# Kill any java processes listening on microservice ports
$ports = 8761, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087
foreach ($port in $ports) {
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        foreach ($conn in $conns) {
            if ($conn.OwningProcess -gt 0) {
                Write-Host "Freeing Port $port (Terminating PID $($conn.OwningProcess))..." -ForegroundColor Gray
                Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            }
        }
    } catch {}
}

Write-Host ""
Write-Host "All FinSight AI microservices stopped and ports freed." -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green
