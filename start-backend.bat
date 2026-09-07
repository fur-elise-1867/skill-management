@echo off
title Skill Management - Backend (Spring Boot :8080)
color 0A

echo ============================================================
echo   SKILL MANAGEMENT - BACKEND
echo   Spring Boot 3.4.2 ^| Java 21 ^| Port: 8080
echo ============================================================
echo.

:: Set JAVA_HOME if not set
if "%JAVA_HOME%"=="" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    echo [INFO] JAVA_HOME set to: %JAVA_HOME%
)

:: Check PostgreSQL is reachable on port 5433
echo [INFO] Checking PostgreSQL on localhost:5433 ...
powershell -Command "try { $t = New-Object System.Net.Sockets.TcpClient('localhost', 5433); $t.Close(); Write-Host '[OK]   PostgreSQL is reachable.' } catch { Write-Host '[WARN] PostgreSQL not reachable on port 5433. Make sure Docker container is running.'; exit 1 }"
if errorlevel 1 (
    echo.
    echo  Run: docker start skill-mgmt-postgres
    echo.
    pause
    exit /b 1
)

echo.
echo [INFO] Starting Spring Boot Backend...
echo [INFO] URL: http://localhost:8080
echo [INFO] Health: http://localhost:8080/actuator/health
echo.
echo  Press Ctrl+C to stop the server.
echo ============================================================
echo.

cd /d "%~dp0backend"
call mvnw.cmd spring-boot:run

pause
