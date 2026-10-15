@echo off
REM ==============================================================================
REM FinSight AI - Single Command Microservices Launcher (Windows Batch)
REM ==============================================================================
echo =================================================================
echo   FinSight AI -- Unified Microservices Ecosystem Launcher
echo =================================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all-microservices.ps1"
pause
