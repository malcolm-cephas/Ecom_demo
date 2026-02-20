# How to Demonstrate Spring AI Security

This guide explains how to verify that the **Model Context Protocol (MCP)** server is secured with HTTP Basic Authentication.

To protect your AI infrastructure, we have configured the MCP Server (running on port `9091`) to reject any unauthorized connections. Only clients with the correct username and password can access the tools.

---

## 🚀 1. Start the Application

First, ensure all services are running. Open a terminal in the project root and run:

```powershell
.\run_all.bat
```

Wait until the logs settle and you see messages indicating the services have started (specifically the MCP Server on port 9091).

---

## 🚫 2. Verify Unauthorized Access (Failure Test)

Open a new PowerShell window or Command Prompt. Try to access the server **without** credentials. This simulates a malicious actor or unauthenticated user.

```powershell
curl.exe -v http://localhost:9091/sse
```

**Expected Result:**
You should see a **`401 Unauthorized`** response.

```
< HTTP/1.1 401
< Set-Cookie: JSESSIONID=...
< WWW-Authenticate: Basic realm="Realm"
< Content-Type: application/json
...
{"status":401,"error":"Unauthorized","message":"Unauthorized","path":"/sse"}
```

This confirms that the security filter chain is actively blocking requests.

---

## ✅ 3. Verify Authorized Access (Success Test)

Now, provide the correct credentials (`client-01` / `ecom-secret-key-123`) using the `-u` flag. This simulates the authorized MCP Client.

```powershell
curl.exe -v -u client-01:ecom-secret-key-123 http://localhost:9091/sse
```

**Expected Result:**
You should see a **`200 OK`** response and the connection will stay open (as it is an SSE stream).

```
< HTTP/1.1 200
< Content-Type: text/event-stream
< Cache-Control: no-cache
< Connection: keep-alive
...
data: {"jsonrpc":"2.0","method":"notifications/initialized"}
```

*(Press `Ctrl+C` to stop the stream)*.

---

## 📝 4. Check the Application Logs

The application is configured to log security events. Check the `Logs/mcp_server.log` file.

You will see authorized requests being processed:

```log
[MCP-SERVER-REQUEST] GET /sse
Authorization: Basic Y2xpZW50LTAxOmVjb20tc2VjcmV0LWtleS0xMjM=
```

---

## 🔐 Configuration Files

The security logic is defined in these two key files:

1.  **Server Config** (`McpSecurityConfig.java`):
    *   Enforces `httpBasic()` authentication.
    *   Defines the user `client-01`.

2.  **Client Config** (`McpClientConfig.java`):
    *   Adds the `Authorization: Basic ...` header to every request it makes to the server.

Use this guide to demonstrate the end-to-end security flow of your Spring AI application!
