# E-Commerce MCP Client (AI & Chat logic)

This module acts as the **AI Orchestrator** for user-facing chat. It handles LLM integration, exposes chat endpoints, and connects to the MCP Server to "borrow" tools.

## 🎯 Primary Functions

1.  **AI Chat Assistant**: Provides a REST API (`POST /api/ai/chat`) for frontend applications to interact with the AI.
2.  **MCP Client**: Establishes a synchronized connection to the **MCP Server** (on port 9091) using manual bean configuration for stability.
3.  **OAuth2 Integration**: Employs **Client Credentials** grant type to reliably handle the SSE handshake and protocol messaging securely.
4.  **LLM Gateway**: Connects to the **LiteLLM Proxy Server** (port 4000) for intelligent routing and fallback logic across multiple LLM providers.
5.  **Request Logging**: All incoming requests are logged with the `[MCP-CLIENT-REQUEST]` prefix.

## 🛠️ Technology Stack

*   **Java 21**
*   **Spring Boot 3.3.2**
*   **Spring AI** (MCP Client Starter, OpenAI Starter)
*   **LiteLLM Proxy** (External router)

## 🏃‍♂️ Setup & Run

### 1. Configuration
Check `src/main/resources/application.properties`. It is pre-configured to use the local LiteLLM proxy:
```properties
server.port=9090
spring.ai.openai.base-url=http://localhost:4000
spring.ai.openai.api-key=dummy-key
spring.ai.openai.chat.options.model=llama-4-scout
```

### 2. MCP Authentication
The client connects to the MCP Server using an OAuth2 Client Credentials flow, verifying its identity with the auth server to securely access the tool definitions via SSE.

### 3. Model Configuration & Legacy Failover
The system is designed to route requests to `http://localhost:4000` (LiteLLM).

**Legacy Fallback Config:** 
If you choose to bypass the LiteLLM proxy and connect to Groq directly, the system retains legacy configurations in `src/main/resources/groq_models.json`. This file defines 18 models and the client includes an internal failover mechanism (`AIAssistantService.java`) that will automatically rotate to the next model when rate limits are encountered.

### 4. Dependency
This client requires the **MCP Server** to be running on port **9091** to function correctly.

### 5. Start Application

#### Option 1: Automated (Recommended)
From the project root, run:
```bash
run_all.bat
```

#### Option 2: Manual
```bash
mvnw spring-boot:run
```
AI API starts on port: **9090**

---

## 🤖 Interaction Flow

1.  User sends a question to `http://localhost:9090/api/ai/chat`.
2.  The Client connects to the Server (9091) via SSE to get the list of tools.
3.  The LLM (via LiteLLM proxy) decides which tool to use.
4.  The Client executes the tool call *through* the MCP connection to the Server.
5.  The Server returns data, and the Client generates the final AI response.

---

## 📊 Logging

Logs are output to both:
- **Console/Terminal**: Real-time monitoring
- **Log File**: `../../Logs/mcp_client.log`

---

## 📂 Structure
*   `com.malcolm.ecomai.ai`: Chat service, AI prompt logic, and legacy model rotation.
*   `com.malcolm.ecomai.controller`: REST endpoints for the AI chat.
*   `com.malcolm.ecomai.tools`: MCP session management.
*   `resources/groq_models.json`: Legacy model definitions.
