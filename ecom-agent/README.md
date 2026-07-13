# Ecom Agent (Autonomous Shopping Assistant)

The `ecom-agent` module is the "brain" of the intelligent shopping experience. It operates as an autonomous agent that can understand complex user goals, break them down into actionable steps, and execute tools against the backend to fulfill those goals.

## Architecture

The Agent runs on **Port 8082** and interfaces heavily with the LiteLLM Proxy Server and the Core Backend.

- **Decision Engine**: Replaces traditional sequential logic with an LLM-driven loop. It determines the next best action (e.g., search, check stock, recommend) based on the user's intent and current memory.
- **Tools**: The agent is equipped with several capabilities:
  - `FETCH_WISHLIST`: Seed recommendations based on user history.
  - `SEARCH_PRODUCTS`: Query the catalog database.
  - `CHECK_STOCK`: Verify inventory levels in real-time.
  - `RECOMMEND_PRODUCT`: Finalize an item selection for the user.
  - `ASK_USER`: Request clarification if constraints are too ambiguous.

## LiteLLM Integration

Previously, this agent managed its own model fallback logic by parsing JSON files (`groq_models.json`). This logic has been decoupled.

Now, the agent relies entirely on the **LiteLLM Proxy Server** (configured to run on `localhost:4000`).
- The `DecisionEngine.java` delegates routing and failovers to LiteLLM.
- The `application.properties` sets the proxy URL and the default model.

## Running the Agent

This agent is automatically launched via the root `run_all.bat` script.

If running manually:
```bash
cd ecom-agent
mvnw spring-boot:run
```

Ensure your LiteLLM Proxy is running, otherwise the agent will fail to resolve its `chatClient` calls.
