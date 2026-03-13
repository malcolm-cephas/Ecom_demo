# 🔐 Unified Security Architecture Demo

This guide explains how to verify the multi-layered security model implemented across the Intelligent E-Commerce Platform. Our architecture uses **OpenID Connect (OIDC)** for users and **OAuth2 Client Credentials** for service-to-service communication.

---

## 🏗️ 1. Security Overview

1.  **Identity Provider (Port 9000)**: The central hub for user identities.
2.  **Resource Server (Port 8080)**: The core backend that validates OIDC tokens.
3.  **MCP Tool Hub (Port 9091)**: Secured with OAuth2 to protect AI tools.
4.  **AI Bridge (Port 9090)**: Authenticates with the Tool Hub using Client Credentials.

---

## 🚫 2. Test: AI Tool Protection (OAuth2)

The MCP Server (Port 9091) protects your product inventory from unauthorized AI agents.

### A. Unauthorized Access (Should Fail)
```powershell
curl.exe -v http://localhost:9091/sse
```
**Expected Result:** `401 Unauthorized`. The server requires a Bearer JWT.

### B. Fetching an Access Token
```powershell
curl.exe -u mcp-client:secret -X POST http://localhost:9091/oauth2/token -d "grant_type=client_credentials&scope=openid"
```
**Expected Result:** A JSON response containing an `"access_token"`.

### C. Authorized Access (Should Succeed)
Copy the token from the previous step:
```powershell
curl.exe -v -H "Authorization: Bearer YOUR_TOKEN_HERE" http://localhost:9091/sse
```
**Expected Result:** `200 OK`. The SSE stream opens successfully.

---

## 👤 3. Test: User Login (OIDC)

The React frontend uses the **Authorization Code Flow with PKCE** to log users in securely.

1.  **Open Browser**: Go to `http://localhost:5173`.
2.  **The Shield**: Notice that if you try to access the "Dashboard" or "Add Product", you are redirected to `http://localhost:9000/login`.
3.  **Authentication**:
    *   **User**: `admin` / `admin1234`
    *   **User**: `client` / `client1234`
4.  **Verification**: After login, check the browser's "Session Storage". You will see an `access_token` issued by the Ecom Auth Server.

---

## 🛡️ 4. Key Configuration Files

| Service | Security File | Protocol |
| :--- | :--- | :--- |
| **Auth Server** | `WebSecurityConfig.java` | OIDC / OAuth2 Provider |
| **Spring Backend** | `SecurityConfig.java` | JWT Resource Server |
| **MCP Server** | `McpSecurityConfig.java` | OAuth2 Provider & RS |
| **Frontend** | `main.jsx` | OIDC Client (PKCE) |

Use this guide to demonstrate that every connection in your ecosystem—from users to AI models—is fully authenticated and authorized.
