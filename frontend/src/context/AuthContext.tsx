
// import { useEffect } from "react";
import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";

import { setAuthToken } from "../api/axios";
import type { LoginResponse } from "../types";

interface AuthContextType {
  user: LoginResponse | null;
  token: string | null;
  login: (user: LoginResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(
  undefined
);

export function AuthProvider({
  children,
}: {
  children: ReactNode;
}) {
  const [user, setUser] = useState<LoginResponse | null>(null);

  const login = (userData: LoginResponse) => {
    setUser(userData);
  };

  const logout = () => {
    setUser(null);
  };

  const token = user?.token ?? null;
  useEffect(() => {
    setAuthToken(token);
  }, [token]);

  return (
    <AuthContext.Provider
      value={{ user, token, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error(
      "useAuth must be used inside AuthProvider"
    );
  }

  return context;
}
