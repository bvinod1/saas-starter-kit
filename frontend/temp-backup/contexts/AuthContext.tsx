import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { userAPI, authAPI } from '../services/api';

interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  tenantId?: string;
  tenantSlug?: string;
  organizationId?: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  currentTenant: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (userData: any) => Promise<void>;
  logout: () => void;
  setCurrentTenant: (tenantSlug: string) => void;
  loading: boolean;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [currentTenant, setCurrentTenantState] = useState<string | null>(localStorage.getItem('currentTenant'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      // Verify token and get user info
      verifyToken();
    } else {
      setLoading(false);
    }
  }, [token]);

  const verifyToken = async () => {
    try {
      const response = await userAPI.getProfile();
      // Type the response properly
      const responseData = response as any;
      setUser(responseData.data?.data || responseData.data);
    } catch (error) {
      console.error('Token verification failed:', error);
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    try {
      const response = await authAPI.login({ email, password });
      // Type the response properly
      const responseData = response as any;
      const { token: accessToken, refreshToken, expiresIn, user: userData } = responseData.data?.data || responseData.data;
      
      setToken(accessToken);
      setUser(userData);
      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      if (expiresIn) {
        localStorage.setItem('tokenExpiry', (Date.now() + expiresIn * 1000).toString());
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const register = async (userData: any) => {
    try {
      // The registration is already handled in the Register component
      // This function is called after successful registration to update the context
      const storedUser = localStorage.getItem('user');
      const storedToken = localStorage.getItem('token');
      const storedRefreshToken = localStorage.getItem('refreshToken');
      
      if (storedUser && storedToken) {
        const user = JSON.parse(storedUser);
        setUser(user);
        setToken(storedToken);
        if (storedRefreshToken) {
          localStorage.setItem('refreshToken', storedRefreshToken);
        }
      }
    } catch (error) {
      console.error('Registration context update failed:', error);
      throw error;
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    setCurrentTenantState(null);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('tokenExpiry');
    localStorage.removeItem('currentTenant');
  };

  const setCurrentTenant = (tenantSlug: string) => {
    setCurrentTenantState(tenantSlug);
    localStorage.setItem('currentTenant', tenantSlug);
  };

  const value: AuthContextType = {
    user,
    token,
    currentTenant,
    login,
    register,
    logout,
    setCurrentTenant,
    loading,
    isAuthenticated: !!user && !!token,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}; 