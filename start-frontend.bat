@echo off
title Skill Management - Frontend (Vue + Vite :5173)
color 0B

echo ============================================================
echo   SKILL MANAGEMENT - FRONTEND
echo   Vue 3 + Vite ^| Node 24 ^| Port: 5173
echo ============================================================
echo.

:: Check Backend is reachable on port 8080
echo [INFO] Checking Backend on localhost:8080 ...
powershell -Command "try { $t = New-Object System.Net.Sockets.TcpClient('localhost', 8080); $t.Close(); Write-Host '[OK]   Backend is reachable.' } catch { Write-Host '[WARN] Backend not reachable on port 8080. You may want to start the backend first.'; }"

echo.
echo [INFO] Starting Frontend Dev Server...
echo [INFO] URL:  http://localhost:5173
echo [INFO] API requests proxied to: http://localhost:8080
echo.
echo  Press Ctrl+C to stop the server.
echo ============================================================
echo.

cd /d "%~dp0frontend"
call npm.cmd run dev

pause
