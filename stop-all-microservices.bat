@echo off
REM ==============================================================================
REM FinSight AI - Single Command Microservices Stopper (Windows Batch)
REM ==============================================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-all-microservices.ps1"
pause
