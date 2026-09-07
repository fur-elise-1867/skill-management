@echo off
title Skill Management - Stop Services
color 0C

echo ============================================================
echo   SKILL MANAGEMENT - STOP ALL SERVICES
echo ============================================================
echo.

:: ---- Stop Backend (Spring Boot / Java process on port 8080) ----
echo [INFO] Stopping Backend (port 8080)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo [INFO] Killing PID %%a (port 8080)
    taskkill /PID %%a /F >nul 2>&1
)

:: Also kill any stray mvnw / java processes related to skill-management
taskkill /FI "WINDOWTITLE eq Skill Management - Backend*" /F >nul 2>&1

:: ---- Stop Frontend (Vite / Node process on port 5173) ----
echo [INFO] Stopping Frontend (port 5173)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":5173 " ^| findstr "LISTENING"') do (
    echo [INFO] Killing PID %%a (port 5173)
    taskkill /PID %%a /F >nul 2>&1
)

taskkill /FI "WINDOWTITLE eq Skill Management - Frontend*" /F >nul 2>&1

echo.
echo ============================================================
echo [DONE] All services stopped.
echo ============================================================
echo.
pause
