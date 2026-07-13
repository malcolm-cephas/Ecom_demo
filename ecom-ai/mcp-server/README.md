# E-Commerce MCP Server (Tool Hub)

This module acts as the **Resource Provider** in the MCP architecture. It exposes the capabilities of the Core Backend to the AI ecosystem.

## 🎯 Primary Functions

1.  **MCP Tool Provider**: Exposes standardized tools (like `searchProducts`, `getProductDetails`, `listAllProducts`) via the **Model Context Protocol (MCP)**.
2.  **API Gateway**: Instead of connecting directly to the database, it forwards AI tool invocations as standard REST calls to the **Core Backend** (port 8080).
3.  **SSE Endpoint**: Provides a Server-Sent Events (SSE) stream at `http://localhost:9091/sse` for clients to connect and discover tools.
4.  **Secured Access**: Configured to require an **OAuth2 Bearer Token** (issued by the Ecom Auth Server) for tool discovery and execution.
5.  **Request Logging**: All incoming requests are logged with the `[MCP-SERVER-REQUEST]` prefix, including detailed header tracing.

## 🛠️ Technology Stack

*   **Java 21**
*   **Spring Boot 3.3.2**
*   **Spring AI** (MCP Server Starter)
*   **OAuth2 Resource Server**

## 🏃‍♂️ Setup & Run

### 1. Configuration
Check `src/main/resources/application.properties`. It is pre-configured to point to the core backend:
```properties
server.port=9091
app.backend.url=http://localhost:8080/api
```

### 2. Start Application

#### Option 1: Automated (Recommended)
From the project root, run:
```bash
run_all.bat
```

#### Option 2: Manual
```bash
mvnw spring-boot:run
```
Server starts on port: **9091**

---

## 📊 Logging

Logs are output to both:
- **Console/Terminal**: Real-time monitoring
- **Log File**: `../../Logs/mcp_server.log`

---

## 🤖 MCP Integration

### 🌉 stdio <-> SSE Bridge
Use the included `mcp-bridge.js` to bridge the SSE protocol to Stdio for tools like Claude Desktop.

**Claude Desktop Configuration:**
```json
"ecom-ai-tools": {
  "command": "node",
  "args": [
    "./mcp-bridge.js",
    "http://localhost:9091/sse"
  ]
}
```

### 🛠️ Exposed Tools
*   `searchProducts`: Search inventory by keyword.
*   `getProductDetails`: Fetch details for a specific product ID.
*   `listAllProducts`: List all products in the catalog.
*   `getUserActivity`: Analyze database metrics.
*   `getCurrentTime`: Time utility for context-aware responses.

---

## 📂 Structure
*   `com.malcolm.ecomai.mcp`: Definitions of tools exposed to AI.
*   `com.malcolm.ecomai.config`: OAuth2 resource server security configurations.
