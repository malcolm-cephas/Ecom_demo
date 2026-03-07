import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "./index.css";

import { AuthProvider } from "react-oauth2-code-pkce";

const authConfig = {
  clientId: 'ecom-client',
  authorizationEndpoint: 'http://localhost:9000/oauth2/authorize',
  tokenEndpoint: 'http://localhost:9000/oauth2/token',
  redirectUri: 'http://localhost:5173/',
  scope: 'openid profile products.read products.write',
  storage: 'session', // Avoid persistent local tokens to prevent auto-login after logout
  onRefreshTokenExpire: (event) => window.confirm('Session expired. Refresh to login again?') && event.login(),
  logoutEndpoint: 'http://localhost:9000/connect/logout',
  logoutRedirect: 'http://localhost:5173/'
};

import { Toaster } from 'react-hot-toast';

// Standard React 18 entry point
// This file initializes the React application and mounts it to the DOM
ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <AuthProvider authConfig={authConfig}>
      <Toaster position="top-right" reverseOrder={false} />
      <App />
    </AuthProvider>
  </React.StrictMode>
);

