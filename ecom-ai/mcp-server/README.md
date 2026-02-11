# E-Commerce MCP Server (Tools & Services)

This module acts as the **Resource Provider** in the MCP architecture. It hosts all the business logic, database interactions, and specialized tools that an AI agent can use.

## 🎯 Primary Functions

1.  **MCP Tool Provider**: Exposes standardized tools (like `searchProducts`, `getProductDetails`, `listAllProducts`) via the **Model Context Protocol (MCP)**.
2.  **Resource Host**: Manages the product inventory and data analytics database.
3.  **SSE Endpoint**: Provides a Server-Sent Events (SSE) stream at `http://localhost:9091/sse` for clients to connect and discover tools.
4.  **Request Logging**: All incoming requests are logged with `[MCP-SERVER-REQUEST]` prefix.

## 🛠️ Technology Stack

*   **Java 21**
*   **Spring Boot 3.3.2**
*   **Spring AI** (MCP Server Starter)
*   **MySQL**: For product and analytics data.

## 🏃‍♂️ Setup & Run

### 1. Database Configuration
Ensure your MySQL database is running and credentials in `src/main/resources/application.properties` are correct.

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

Request format:
```
[MCP-SERVER-REQUEST] POST /mcp/message
[MCP-SERVER-REQUEST] GET /sse
```

---

## 🤖 MCP Integration

### 🌉 stdio <-> SSE Bridge
Use the included `mcp-bridge.js` to bridge the SSE protocol to Stdio for tools like Claude Desktop.

**Claude Desktop Configuration:**
```json
"ecom-ai-tools": {
  "command": "node",
  "args": [
    "D:/Malcolm/DSCE/Internship/SENSEI/ecommerce/ecom-ai/mcp-server/mcp-bridge.js",
    "http://localhost:9091/sse"
  ]
}
```

### 🛠️ Exposed Tools
*   `searchProducts`: Search inventory by keyword.
*   `getProductDetails`: Fetch details for a specific product ID.
*   `listAllProducts`: List all products in the catalog.
*   `analyzeSales`: SQL-based analytics for product data.

---

## 📂 Structure
*   `com.malcolm.ecomai.mcp`: Definitions of tools exposed to AI.
*   `com.malcolm.ecomai.service`: Core business logic.
*   `com.malcolm.ecomai.repo`: JPA repositories for database access.
