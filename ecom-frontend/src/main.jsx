import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "./index.css";
import { AppProvider } from "./Context/Context.jsx";

import { AuthProvider } from "react-oauth2-code-pkce";

const authConfig = {
  clientId: 'ecom-client',
  authorizationEndpoint: 'http://localhost:9000/oauth2/authorize',
  tokenEndpoint: 'http://localhost:9000/oauth2/token',
  redirectUri: 'http://localhost:5173/',
  scope: 'openid profile products.read products.write',
  storage: 'local', // Forces react-oauth2-code-pkce to use localStorage instead of sessionStorage
  onRefreshTokenExpire: (event) => window.confirm('Session expired. Refresh to login again?') && event.login(),
};

// Standard React 18 entry point
// This file initializes the React application and mounts it to the DOM
ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <AuthProvider authConfig={authConfig}>
      {/* Global context provider makes state available to all nested components */}
      <AppProvider>
        <App />
      </AppProvider>
    </AuthProvider>
  </React.StrictMode>
);

