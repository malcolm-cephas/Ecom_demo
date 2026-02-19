# E-Commerce MCP Client (AI & Chat logic)

This module acts as the **AI Orchestrator**. It contains the LLM integration, chat endpoints, and connects to the MCP Server to "borrow" tools.

## 🎯 Primary Functions

1.  **AI Chat Assistant**: Provides a REST API (`POST /api/ai/chat`) for frontend applications to interact with the AI.
2.  **MCP Client**: Establishes a synchronized connection to the **MCP Server** (on port 9091) using manual bean configuration for stability.
3.  **Basic Auth Integration**: Employs **pre-emptive Basic Authentication** to reliably handle the SSE handshake and protocol messaging.
4.  **LLM Gateway**: Connects to **Groq** with automatic model rotation across 18 available models.
5.  **Intelligent Failover**: Automatically switches models when rate limits are encountered.
6.  **Request Logging**: All incoming requests are logged with `[MCP-CLIENT-REQUEST]` prefix.

## 🛠️ Technology Stack

*   **Java 21**
*   **Spring Boot 3.3.2**
*   **Spring AI** (MCP Client Starter, OpenAI Starter)
*   **LLM Provider**: Groq (18 models with automatic rotation)

## 🏃‍♂️ Setup & Run

### 1. Configuration
Check `src/main/resources/application.properties`. You **must** provide a valid Groq API Key:
```properties
server.port=9090
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.api-key=YOUR_GROQ_API_KEY
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
spring.ai.mcp.client.sse.connections.mcp-server.url=http://localhost:9091/sse
```

### 2. MCP Authentication (Optional)
The client connects to the MCP Server using Basic Auth.
- **Default User**: `client-01`
- **Default Key**: `ecom-secret-key-123`
- **Override**: Set `MCP_CLIENT_USER` and `MCP_API_KEY` environment variables.

### 3. Model Configuration
Available models are defined in `src/main/resources/groq_models.json`. The system will:
- Load all non-audio models on startup (18 models total)
- Start with the first model in the list
- Automatically rotate to the next model when rate limits are hit
- Log all model switches for transparency

**Model Hierarchy:**
1. meta-llama/llama-guard-4-12b (Primary)
2. allam-2-7b
3. meta-llama/llama-4-scout-17b-16e-instruct
4. qwen/qwen3-32b
5. openai/gpt-oss-120b
6. ... (13 more models)

### 3. Dependency
This client requires the **MCP Server** to be running on port **9091** to function correctly.

### 4. Start Application

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
3.  The LLM decides which tool to use.
4.  The Client executes the tool call *through* the MCP connection to the Server.
5.  If rate limit is hit, the system automatically switches to the next model and retries.
6.  The Server returns data, and the Client generates the final AI response.

---

## 📊 Logging

Logs are output to both:
- **Console/Terminal**: Real-time monitoring
- **Log File**: `../../Logs/mcp_client.log`

Request format:
```
[MCP-CLIENT-REQUEST] POST /api/ai/chat
Attempting chat with model: llama-3.3-70b-versatile
Rate limit exceeded for model: llama-3.3-70b-versatile. Switching to next model...
Rotated to model index 1: allam-2-7b
```

---

## 📂 Structure
*   `com.malcolm.ecomai.ai`: Chat service, AI prompt logic, and model rotation
*   `com.malcolm.ecomai.controller`: REST endpoints for the AI chat
*   `com.malcolm.ecomai.tools`: MCP session management
*   `resources/groq_models.json`: Available model definitions
