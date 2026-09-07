@echo off
title Skill Management - Start All Services
color 0E

echo ============================================================
echo   SKILL MANAGEMENT - START ALL SERVICES
echo ============================================================
echo.

:: ---- Check PostgreSQL ----
echo [INFO] Checking PostgreSQL Docker container...
docker ps --filter "name=skill-mgmt-postgres" --format "{{.Status}}" | findstr "Up" >nul 2>&1
if errorlevel 1 (
    echo [INFO] Starting PostgreSQL Docker container...
    docker start skill-mgmt-postgres
    timeout /t 3 /nobreak >nul
) else (
    echo [OK]   PostgreSQL is already running.
)

echo.

:: ---- Start Backend in new window ----
echo [INFO] Starting Backend in a new window...
start "Skill Management - Backend (Spring Boot :8080)" /D "%~dp0" cmd /c start-backend.bat

echo [INFO] Waiting 10 seconds for Backend to boot...
timeout /t 10 /nobreak >nul

:: ---- Start Frontend in new window ----
echo [INFO] Starting Frontend in a new window...
start "Skill Management - Frontend (Vue + Vite :5173)" /D "%~dp0" cmd /c start-frontend.bat

echo.
echo ============================================================
echo [DONE] All services started in separate windows.
echo.
echo   Backend  : http://localhost:8080
echo   Frontend : http://localhost:5173
echo ============================================================
echo.
pause
