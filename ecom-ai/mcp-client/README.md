# E-Commerce MCP Client (AI & Chat logic)

This module acts as the **AI Orchestrator**. It contains the LLM integration, chat endpoints, and connects to the MCP Server to "borrow" tools.

## 🎯 Primary Functions

1.  **AI Chat Assistant**: Provides a REST API (`POST /api/ai/chat`) for frontend applications to interact with the AI.
2.  **MCP Client**: Establishes a connection to the **MCP Server** (on port 9091) to discover and execute tools dynamically.
3.  **LLM Gateway**: Connects to AI providers like **Groq** (Llama 3) or **OpenAI**.

## 🛠️ Technology Stack

*   **Java 21**
*   **Spring Boot 3.3.2**
*   **Spring AI** (MCP Client Starter, OpenAI Starter)
*   **LLM Provider**: Groq (compatible via OpenAI API).

## 🏃‍♂️ Setup & Run

### 1. Configuration
Check `src/main/resources/application.properties`. You **must** provide a valid Groq/OpenAI API Key:
```properties
server.port=9090
spring.ai.openai.api-key=YOUR_API_KEY
mcp.server.url=http://localhost:9091/sse
```

### 2. Dependency
This client requires the **MCP Server** to be running on port **9091** to function correctly.

### 3. Start Application
```bash
mvn spring-boot:run
```
AI API starts on port: **9090**

---

## 🤖 Interaction Flow

1.  User sends a question to `http://localhost:9090/api/ai/chat`.
2.  The Client connects to the Server (9091) via SSE to get the list of tools.
3.  The LLM decides which tool to use.
4.  The Client executes the tool call *through* the MCP connection to the Server.
5.  The Server returns data, and the Client generates the final AI response.

---

## 📂 Structure
*   `com.malcolm.ecomai.ai`: Chat service and AI prompt logic.
*   `com.malcolm.ecomai.controller`: REST endpoints for the AI chat.
*   `com.malcolm.ecomai.config`: MCP Client connection settings.
