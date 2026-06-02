import axios from "axios";
import useUserStore from "./store/useUserStore";

// Create a centralized Axios instance for all HTTP requests to the backend
// This allows us to configure base URLs and interceptors in one place
const API = axios.create({
  // The base URL for the Spring Boot REST API
  // Ensure this matches your backend server's port (default 8080)
  baseURL: "http://localhost:8080/api",
});


API.interceptors.request.use(
  (config) => {
    // 1. Try to get token from state management first (Zustand)
    let token = useUserStore.getState().token;

    // 2. Fallback to localStorage / sessionStorage if store is not populated yet
    if (!token) {
      const storageKeys = ['ROCP_token', 'token', 'access_token', 'id_token'];
      let rawToken = null;

      // Search through common keys in both storage locations
      for (const key of storageKeys) {
        rawToken = sessionStorage.getItem(key) || localStorage.getItem(key);
        if (rawToken) break;
      }

      // Fallback: search for ANY key that LOOKS like a ROCP token key
      if (!rawToken) {
        const allStorages = [sessionStorage, localStorage];
        for (const storage of allStorages) {
          for (let i = 0; i < storage.length; i++) {
            const key = storage.key(i);
            if (key && (key.includes('token') || key.includes('access_token'))) {
              rawToken = storage.getItem(key);
              break;
            }
          }
          if (rawToken) break;
        }
      }

      if (rawToken) {
        try {
          const tokenObj = JSON.parse(rawToken);
          token = tokenObj.token || tokenObj.access_token || tokenObj.accessToken || (typeof tokenObj === 'string' ? tokenObj : rawToken);
        } catch (e) {
          token = rawToken;
        }
      }
    }

    if (token && typeof token === 'string' && token.length > 10) {
      // Clean up quotes if present
      token = token.replace(/^"(.*)"$/, '$1');
      config.headers.Authorization = `Bearer ${token}`;
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

