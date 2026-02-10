@echo off
setlocal

echo Starting Ecommerce Application Services...
echo ===========================================

:: 1. Start Frontend (ecom-frontend) on default port (usually 5173)
echo [1/4] Starting Frontend...
start "Frontend (Port 5173)" cmd /k "cd ecom-frontend && npm run dev"
timeout /t 5 /nobreak > nul

:: 2. Start Core Backend API (ecom-proj) on Port 8080
echo [2/4] Starting Core Backend (Hub)...
start "Core Backend (Port 8080)" cmd /k "cd ecom-proj && mvnw spring-boot:run"
echo Waiting for Core Backend to initialize...
timeout /t 20 /nobreak > nul

:: 3. Start MCP Server (ecom-ai/mcp-server) on Port 9091
echo [3/4] Starting MCP Server...
start "MCP Server (Port 9091)" cmd /k "cd ecom-ai/mcp-server && mvnw spring-boot:run"
echo Waiting for MCP Server to initialize...
timeout /t 15 /nobreak > nul

:: 4. Start MCP Client (ecom-ai/mcp-client) on Port 9090
echo [4/4] Starting MCP Client...
start "MCP Client (Port 9090)" cmd /k "cd ecom-ai/mcp-client && mvnw spring-boot:run"

echo ===========================================
echo All services are launching in separate windows.
echo Please check each window for startup logs.
echo ===========================================
pause
