import axios from "axios";

// Create a centralized Axios instance for all HTTP requests to the backend
// This allows us to configure base URLs and interceptors in one place
const API = axios.create({
  // The base URL for the Spring Boot REST API
  // Ensure this matches your backend server's port (default 8080)
  baseURL: "http://localhost:8080/api",
});


// Add a request interceptor to inject the JWT token if available and log requests
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log(`[Frontend Request] ${config.method.toUpperCase()} ${config.url}`, config.data || "");
    return config;
  },
  (error) => {
    console.error("[Frontend Request Error]", error);
    return Promise.reject(error);
  }
);

// Add a response interceptor to log incoming responses
// Useful for viewing server responses without manually logging everywhere
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

