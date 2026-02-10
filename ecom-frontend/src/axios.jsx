import axios from "axios";

// Create a centralized Axios instance for all HTTP requests to the backend
const API = axios.create({
  // The base URL for the Spring Boot REST API
  baseURL: "http://127.0.0.1:8080/api",
});

// Explicitly ensure no Authorization header is sent by default unless needed
delete API.defaults.headers.common["Authorization"];

export default API;

