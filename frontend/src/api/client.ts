import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:4000",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("shebadesk_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401 && !location.pathname.startsWith("/login")) {
      localStorage.removeItem("shebadesk_token");
      location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export default api;

export interface ProblemDetail {
  type?: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}

export function problemMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ProblemDetail | undefined;
    if (data?.detail) return data.detail;
    if (data?.title) return data.title;
  }
  return "Something went wrong";
}
