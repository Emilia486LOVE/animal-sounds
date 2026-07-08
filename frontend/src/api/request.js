import axios from "axios";

const request = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  timeout: 120000,
});

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

request.interceptors.response.use(
  (response) => {
    if (
      response.data &&
      (response.data.code === 401 || response.data.code === 403)
    ) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return response;
  },
  (error) => {
    if (
      error.response &&
      (error.response.status === 401 || error.response.status === 403)
    ) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export default request;
