import axios from "axios";

const API = axios.create({
  baseURL: "http://127.0.0.1:8080/api",
});
delete API.defaults.headers.common["Authorization"];
export default API;
