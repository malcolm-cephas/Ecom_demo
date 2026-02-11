import axios from "axios";

// Create a centralized Axios instance for all HTTP requests to the backend
const API = axios.create({
  // The base URL for the Spring Boot REST API
  baseURL: "http://127.0.0.1:8080/api",
});


// Explicitly ensure no Authorization header is sent by default unless needed
delete API.defaults.headers.common["Authorization"];

// Add a request interceptor to log outgoing requests
API.interceptors.request.use(
  (config) => {
    console.log(`[Frontend Request] ${config.method.toUpperCase()} ${config.url}`, config.data || "");
    return config;
  },
  (error) => {
    console.error("[Frontend Request Error]", error);
    return Promise.reject(error);
  }
);

// Add a response interceptor to log incoming responses
API.interceptors.response.use(
  (response) => {
    console.log(`[Frontend Response] ${response.status} ${response.config.url}`, response.data);
    return response;
  },
  (error) => {
    console.error(`[Frontend Response Error] ${error.response?.status} ${error.config?.url}`, error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default API;

