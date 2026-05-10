import { createContext, useContext, useState, type ReactNode } from 'react';

interface AuthUser {
  email: string;
  businessId: number | null;
  roles: string[];
}

interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

function parseJwt(token: string): AuthUser {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return {
    email: payload.sub,
    businessId: payload.businessId ?? null,
    roles: payload.roles ?? [],
  };
}

function isExpired(token: string): boolean {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.exp * 1000 < Date.now();
}

function loadStoredAuth(): { token: string; user: AuthUser } | null {
  const t = localStorage.getItem('token');
  if (!t || isExpired(t)) {
    localStorage.removeItem('token');
    return null;
  }
  return { token: t, user: parseJwt(t) };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const stored = loadStoredAuth();
  const [token, setToken] = useState<string | null>(stored?.token ?? null);
  const [user, setUser] = useState<AuthUser | null>(stored?.user ?? null);

  const login = async (email: string, password: string) => {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usernameOrEmail: email, password }),
    });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      throw new Error(data.message || 'Credenciales incorrectas');
    }
    const data = await res.json();
    localStorage.setItem('token', data.accessToken);
    setToken(data.accessToken);
    setUser(parseJwt(data.accessToken));
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}
