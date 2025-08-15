import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { NotificationProvider } from './contexts/NotificationContext';
import './index.css';

// Layout Components
import Layout from './components/layout/Layout';
import AuthLayout from './components/layout/AuthLayout';

// Auth Pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';

// Main Pages
import Dashboard from './pages/Dashboard';
import Billing from './pages/Billing';
import LandingPage from './pages/LandingPage';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import UserManagement from './pages/admin/UserManagement';

// Protected Route Component
interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requireAdmin?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requireAuth = true, 
  requireAdmin = false 
}) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/auth/login" replace />;
  }

  if (requireAdmin && user?.role !== 'admin') {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

// Public Route Component
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

// Main App Routes
const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<LandingPage />} />
      
      {/* Auth Routes */}
      <Route path="/auth" element={<AuthLayout />}>
        <Route path="login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="register" element={<PublicRoute><Register /></PublicRoute>} />
        <Route path="forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />
      </Route>

      {/* Protected App Routes */}
      <Route path="/app" element={<Layout />}>
        <Route path="dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="billing" element={<ProtectedRoute><Billing /></ProtectedRoute>} />
        <Route path="profile" element={<ProtectedRoute><div>Profile Page</div></ProtectedRoute>} />
        <Route path="settings" element={<ProtectedRoute><div>Settings Page</div></ProtectedRoute>} />
        <Route path="users" element={<ProtectedRoute><div>Users Page</div></ProtectedRoute>} />
        <Route path="organizations" element={<ProtectedRoute><div>Organizations Page</div></ProtectedRoute>} />
      </Route>

      {/* Admin Routes */}
      <Route path="/admin" element={<Layout />}>
        <Route path="dashboard" element={<ProtectedRoute requireAdmin><AdminDashboard /></ProtectedRoute>} />
        <Route path="users" element={<ProtectedRoute requireAdmin><UserManagement /></ProtectedRoute>} />
        <Route path="tenants" element={<ProtectedRoute requireAdmin><div>Tenant Management</div></ProtectedRoute>} />
        <Route path="billing" element={<ProtectedRoute requireAdmin><div>Billing Management</div></ProtectedRoute>} />
        <Route path="notifications" element={<ProtectedRoute requireAdmin><div>Notification Management</div></ProtectedRoute>} />
        <Route path="monitoring" element={<ProtectedRoute requireAdmin><div>System Monitoring</div></ProtectedRoute>} />
        <Route path="api-gateway" element={<ProtectedRoute requireAdmin><div>API Gateway Management</div></ProtectedRoute>} />
        <Route path="logging" element={<ProtectedRoute requireAdmin><div>Logging Management</div></ProtectedRoute>} />
        <Route path="database" element={<ProtectedRoute requireAdmin><div>Database Management</div></ProtectedRoute>} />
        <Route path="security" element={<ProtectedRoute requireAdmin><div>Security & Compliance</div></ProtectedRoute>} />
        <Route path="configuration" element={<ProtectedRoute requireAdmin><div>System Configuration</div></ProtectedRoute>} />
        <Route path="support" element={<ProtectedRoute requireAdmin><div>Support Tools</div></ProtectedRoute>} />
      </Route>

      {/* Legacy Routes - Redirect to new structure */}
      <Route path="/dashboard" element={<Navigate to="/app/dashboard" replace />} />
      <Route path="/billing" element={<Navigate to="/app/billing" replace />} />
      <Route path="/profile" element={<Navigate to="/app/profile" replace />} />
      <Route path="/settings" element={<Navigate to="/app/settings" replace />} />
      <Route path="/users" element={<Navigate to="/app/users" replace />} />
      <Route path="/organizations" element={<Navigate to="/app/organizations" replace />} />

      {/* Catch-all route */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

// Query Client Configuration
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
    mutations: {
      retry: 1,
    },
  },
});

// Main App Component
function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <NotificationProvider>
          <Router>
            <div className="App">
              <AppRoutes />
              
              {/* Global Toast Notifications */}
              <Toaster 
                position="top-right"
                toastOptions={{
                  duration: 4000,
                  style: {
                    background: '#363636',
                    color: '#fff',
                    borderRadius: '8px',
                    fontSize: '14px',
                  },
                  success: {
                    iconTheme: {
                      primary: '#10B981',
                      secondary: '#fff',
                    },
                  },
                  error: {
                    iconTheme: {
                      primary: '#EF4444',
                      secondary: '#fff',
                    },
                  },
                }}
              />
            </div>
          </Router>
        </NotificationProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App; 