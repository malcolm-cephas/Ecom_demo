import axios from "axios";

// Create a centralized Axios instance for all HTTP requests to the backend
// This allows us to configure base URLs and interceptors in one place
const API = axios.create({
  // The base URL for the Spring Boot REST API
  // Ensure this matches your backend server's port (default 8080)
  baseURL: "http://localhost:8080/api",
});


API.interceptors.request.use(
  (config) => {
    const rawToken = localStorage.getItem('ROCP_token');

    if (rawToken) {
      let token = rawToken;
      try {
        const tokenObj = JSON.parse(rawToken);
        // If it's a JSON object, extract the token; otherwise if it's just a string, use it
        token = typeof tokenObj === 'string' ? tokenObj : (tokenObj.token || tokenObj.access_token || rawToken);
      } catch (e) {
        // Not JSON, use rawToken directly
      }

      // Final check: Remove any double-quotes that might have leaked from a bad JSON parse/storage
      token = token.toString().replace(/^"(.*)"$/, '$1');

      if (token && token.length > 20) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
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

