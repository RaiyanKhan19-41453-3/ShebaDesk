import { createContext, useCallback, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import api from "../api/client";

interface AuthState {
  token: string | null;
  username: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

function decodeUsername(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.sub ?? null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("shebadesk_token"));

  const login = useCallback(async (username: string, password: string) => {
    const { data } = await api.post("/auth/login", { username, password });
    localStorage.setItem("shebadesk_token", data.accessToken);
    setToken(data.accessToken);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("shebadesk_token");
    setToken(null);
  }, []);

  const value = useMemo<AuthState>(
    () => ({ token, username: token ? decodeUsername(token) : null, login, logout }),
    [token, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
