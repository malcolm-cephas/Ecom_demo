# Ecom Authorization Server

This is a dedicated Authorization Server for the Ecom project, built using Spring Security Authorization Server.

## Features
- OAuth2 and OpenID Connect 1.0 support.
- JWT token issuance with RSA signing.
- In-memory client and user storage (for demo purposes).

## Configuration
- **Port**: 9000
- **Issuer URI**: http://localhost:9000
- **JWKS Endpoint**: http://localhost:9000/oauth2/jwks

## Default Clients

### 1. Web Client (ecom-client)
- **Client ID**: `ecom-client`
- **Client Secret**: `secret`
- **Grant Type**: Authorization Code
- **Redirect URIs**: 
  - `http://127.0.0.1:8080/login/oauth2/code/ecom-client`
  - `https://oauth.pstmn.io/v1/callback`

### 2. MCP Client (mcp-client)
- **Client ID**: `mcp-client`
- **Client Secret**: `secret`
- **Grant Type**: Client Credentials
- **Purpose**: Secure service-to-service SSE streaming.

## Default User
- **Username**: `user`
- **Password**: `password`

## How to use with Postman
1. Select **OAuth 2.0** as Auth Type.
2. Grant Type: **Authorization Code**.
3. Callback URL: `https://oauth.pstmn.io/v1/callback`.
4. Auth URL: `http://localhost:9000/oauth2/authorize`.
5. Access Token URL: `http://localhost:9000/oauth2/token`.
6. Client ID: `ecom-client`.
7. Client Secret: `secret`.
8. Scope: `openid profile products.read`.
