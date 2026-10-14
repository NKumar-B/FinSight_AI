import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, LoginResponseData } from '@/types/auth';
import { authApi, RegisterPayload } from '../api/authApi';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, pass: string) => Promise<LoginResponseData>;
  register: (payload: RegisterPayload) => Promise<{ success: boolean; message: string; user?: User }>;
  completeSession: (data: LoginResponseData) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('finsight_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('finsight_jwt');
  });
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const handleAuthExpired = () => {
      logout();
    };
    window.addEventListener('auth:expired', handleAuthExpired);
    return () => window.removeEventListener('auth:expired', handleAuthExpired);
  }, []);

  const login = async (username: string, pass: string): Promise<LoginResponseData> => {
    setIsLoading(true);
    try {
      const data = await authApi.login(username, pass);
      return data;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (payload: RegisterPayload) => {
    setIsLoading(true);
    try {
      return await authApi.register(payload);
    } finally {
      setIsLoading(false);
    }
  };

  const completeSession = (data: LoginResponseData) => {
    setToken(data.accessToken);
    setUser(data.user);
    localStorage.setItem('finsight_jwt', data.accessToken);
    localStorage.setItem('finsight_refresh_token', data.refreshToken);
    localStorage.setItem('finsight_user', JSON.stringify(data.user));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('finsight_jwt');
    localStorage.removeItem('finsight_refresh_token');
    localStorage.removeItem('finsight_user');
  };

  const hasRole = (role: string): boolean => {
    if (!user || !user.roles) return false;
    if (user.roles.includes('ROLE_ADMIN' as any)) return true;
    return user.roles.includes(role as any);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        completeSession,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
