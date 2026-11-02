import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi } from '../api/auth';
import { setUnauthorizedHandler } from '../api/client';
import { loadSession, saveSession } from '../api/session';
import type { LoginRequest, RegisterRequest, Session } from '../types';

interface AuthContextValue {
  user: Session | null;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (details: RegisterRequest) => Promise<string>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Session | null>(loadSession);

  const store = useCallback((session: Session | null) => {
    saveSession(session);
    setUser(session);
  }, []);

  const logout = useCallback(() => store(null), [store]);

  useEffect(() => setUnauthorizedHandler(logout), [logout]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      logout,
      login: async (credentials) => store(await authApi.login(credentials)),
      register: async (details) => (await authApi.register(details)).message,
    }),
    [user, logout, store],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
