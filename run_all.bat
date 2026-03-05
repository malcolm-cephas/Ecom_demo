@echo off
setlocal

echo Starting Ecommerce Application Portfolio Ecosystem...
echo ======================================================

:: Setup Logs Directory
if not exist "%~dp0Logs" mkdir "%~dp0Logs"
echo Cleaning old logs...
del /q "%~dp0Logs\*.log" 2>nul
echo. > "%~dp0Logs\auth_server.log"
echo. > "%~dp0Logs\spring_backend.log"
echo. > "%~dp0Logs\mcp_server.log"
echo. > "%~dp0Logs\mcp_client.log"
echo. > "%~dp0Logs\react_frontend.log"

:: 1. Start Authorization Server (Port 9000)
echo [1/5] Launching Authorization Server (Port 9000)...
start "Auth Server (9000)" powershell -NoExit -Command "cd 'ecom-auth-server'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\auth_server.log'"
echo Waiting for Auth Server to stabilize (35s)...
timeout /t 35 /nobreak > nul

:: 2. Start Core Backend (Resource Server, Port 8080)
echo [2/5] Launching Core Backend (Port 8080)...
start "Core Backend (8080)" powershell -NoExit -Command "cd 'ecom-proj'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\spring_backend.log'"
echo Waiting for Core Backend to start (35s)...
timeout /t 35 /nobreak > nul

:: 3. Start MCP Server (AI Interface, Port 9091)
echo [3/5] Launching MCP Server (Port 9091)...
start "MCP Server (9091)" powershell -NoExit -Command "cd 'ecom-ai/mcp-server'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\mcp_server.log'"
echo Waiting for MCP Server (20s)...
timeout /t 20 /nobreak > nul

:: 4. Start MCP Client (Bridge, Port 9090)
echo [4/5] Launching MCP Client (Port 9090)...
start "MCP Client (9090)" powershell -NoExit -Command "cd 'ecom-ai/mcp-client'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\mcp_client.log'"
echo Waiting for MCP Client (15s)...
timeout /t 15 /nobreak > nul

:: 5. Start Frontend (Vite, Port 5173)
echo [5/5] Launching React Frontend (Port 5173)...
start "Frontend (5173)" powershell -NoExit -Command "cd 'ecom-frontend'; npm run dev 2>&1 | Tee-Object -FilePath '%~dp0Logs\react_frontend.log'"

:: 6. Launch Log Monitor
echo [6/5] Starting Log Monitor UI...
start "Unified Log Monitor" powershell -NoExit -ExecutionPolicy Bypass -File "%~dp0monitor_logs.ps1"

echo ======================================================
echo STATUS: Deployment initiated. Check separate terminal windows for errors.
echo Backend API (Resource Server) will be locked until a valid JWT is provided.
echo ======================================================
pause
