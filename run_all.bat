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
echo [1/7] Launching Authorization Server (Port 9000)...
start "Auth Server (9000)" powershell -NoExit -Command "cd 'ecom-auth-server'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\auth_server.log'"
echo Waiting for Auth Server to start on port 9000...
set /a count=0
:wait_auth_server
timeout /t 2 /nobreak > nul
set /a count+=1
netstat -ano | findstr LISTENING | findstr :9000 >nul
if %errorlevel% neq 0 (
    if %count% geq 60 (
        echo [WARNING] Auth Server did not start within 120 seconds. Proceeding anyway...
        goto next_auth
    )
    goto wait_auth_server
)
:next_auth
echo Auth Server is UP!
echo.

:: 2. Start Core Backend (Resource Server, Port 8080)
echo [2/7] Launching Core Backend (Port 8080)...
start "Core Backend (8080)" powershell -NoExit -Command "cd 'ecom-proj'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\spring_backend.log'"
echo Waiting for Core Backend to start on port 8080...
set /a count=0
:wait_core_backend
timeout /t 2 /nobreak > nul
set /a count+=1
netstat -ano | findstr LISTENING | findstr :8080 >nul
if %errorlevel% neq 0 (
    if %count% geq 60 (
        echo [WARNING] Core Backend did not start within 120 seconds. Proceeding anyway...
        goto next_core
    )
    goto wait_core_backend
)
:next_core
echo Core Backend is UP!
echo.

:: 3. Start MCP Server (AI Interface, Port 9091)
echo [3/7] Launching MCP Server (Port 9091)...
start "MCP Server (9091)" powershell -NoExit -Command "cd 'ecom-ai/mcp-server'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\mcp_server.log'"
echo Waiting for MCP Server to start on port 9091...
set /a count=0
:wait_mcp_server
timeout /t 2 /nobreak > nul
set /a count+=1
netstat -ano | findstr LISTENING | findstr :9091 >nul
if %errorlevel% neq 0 (
    if %count% geq 60 (
        echo [WARNING] MCP Server did not start within 120 seconds. Proceeding anyway...
        goto next_mcp_srv
    )
    goto wait_mcp_server
)
:next_mcp_srv
echo MCP Server is UP!
echo.

:: 4. Start MCP Client (Bridge, Port 9090)
echo [4/7] Launching MCP Client (Port 9090)...
start "MCP Client (9090)" powershell -NoExit -Command "cd 'ecom-ai/mcp-client'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\mcp_client.log'"
echo Waiting for MCP Client to start on port 9090...
set /a count=0
:wait_mcp_client
timeout /t 2 /nobreak > nul
set /a count+=1
netstat -ano | findstr LISTENING | findstr :9090 >nul
if %errorlevel% neq 0 (
    if %count% geq 60 (
        echo [WARNING] MCP Client did not start within 120 seconds. Proceeding anyway...
        goto next_mcp_cl
    )
    goto wait_mcp_client
)
:next_mcp_cl
echo MCP Client is UP!
echo.

:: 5. Start Ecom Agent (Port 8082)
echo [5/7] Launching Ecom Agent (Port 8082)...
start "Ecom Agent (8082)" powershell -NoExit -Command "cd 'ecom-agent'; & '.\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0Logs\ecom_agent.log'"
echo Waiting for Ecom Agent to start on port 8082...
set /a count=0
:wait_ecom_agent
timeout /t 2 /nobreak > nul
set /a count+=1
netstat -ano | findstr LISTENING | findstr :8082 >nul
if %errorlevel% neq 0 (
    if %count% geq 60 (
        echo [WARNING] Ecom Agent did not start within 120 seconds. Proceeding anyway...
        goto next_ecom_agent
    )
    goto wait_ecom_agent
)
:next_ecom_agent
echo Ecom Agent is UP!
echo.

:: 6. Start Frontend (Vite, Port 5173)
echo [6/7] Launching React Frontend (Port 5173)...
start "Frontend (5173)" powershell -NoExit -Command "cd 'ecom-frontend'; npm run dev 2>&1 | Tee-Object -FilePath '%~dp0Logs\react_frontend.log'"

:: 7. Launch Log Monitor
echo [7/7] Starting Log Monitor UI...
start "Unified Log Monitor" powershell -NoExit -ExecutionPolicy Bypass -File "%~dp0monitor_logs.ps1"

echo ======================================================
echo STATUS: Deployment initiated. Check separate terminal windows for errors.
echo Backend API (Resource Server) will be locked until a valid JWT is provided.
echo ======================================================
pause
