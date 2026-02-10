import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "./index.css";
import { AppProvider } from "./Context/Context.jsx";

// Standard React 18 entry point
// This file initializes the React application and mounts it to the DOM
ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    {/* Global context provider makes state available to all nested components */}
    <AppProvider>
      <App />
    </AppProvider>
  </React.StrictMode>
);

