@echo off
setlocal

echo Starting Ecommerce Application Services...
echo ===========================================

echo Clearing and initializing log files...
if not exist "%~dp0Logs" mkdir "%~dp0Logs"
type nul > "%~dp0Logs\react_frontend.log"
type nul > "%~dp0Logs\spring_backend.log"
type nul > "%~dp0Logs\mcp_server.log"
type nul > "%~dp0Logs\mcp_client.log"

:: 1. Start Core Backend API (ecom-proj) on Port 8080
echo [1/4] Starting Core Backend (Hub)...
start "Core Backend (Port 8080)" powershell -NoExit -Command "cd 'ecom-proj'; .\mvnw.cmd spring-boot:run '-Dspring-boot.run.jvmArguments=-Dapp.db.refresh=true' 2>&1 | Tee-Object -FilePath '%~dp0Logs\spring_backend.log'"
echo Waiting for Core Backend to initialize...
timeout /t 20 /nobreak > nul

:: 2. Start MCP Server (ecom-ai/mcp-server) on Port 9091 (Basic Auth)
echo [2/4] Starting MCP Server (Secured)...
start "MCP Server (Port 9091)" powershell -NoExit -Command "cd 'ecom-ai/mcp-server'; .\mvnw.cmd spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\mcp_server.log'"
echo Waiting for MCP Server to start...
timeout /t 15 /nobreak > nul

:: 3. Start MCP Client (ecom-ai/mcp-client) on Port 9090 (Pre-emptive Basic Auth)
echo [3/4] Starting MCP Client...
start "MCP Client (Port 9090)" powershell -NoExit -Command "cd 'ecom-ai/mcp-client'; .\mvnw.cmd spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\mcp_client.log'"
echo Waiting for MCP Client...
timeout /t 10 /nobreak > nul

:: 4. Start Frontend (ecom-frontend) on default port (usually 5173)
echo [4/4] Starting Frontend...
start "Frontend (Port 5173)" powershell -NoExit -Command "cd 'ecom-frontend'; npm run dev 2>&1 | Tee-Object -FilePath '%~dp0Logs\react_frontend.log'"

:: 5. Start Log Monitor
echo [5/5] Starting Log Monitor...
start "Log Monitor" powershell -NoExit -ExecutionPolicy Bypass -File "%~dp0monitor_logs.ps1"

echo ===========================================
echo All services are launching in separate windows.
echo Please check each window for startup logs.
echo ===========================================
pause
