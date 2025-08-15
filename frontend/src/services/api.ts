import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import loggingService from './loggingService';
import openTelemetryService from './opentelemetry';

// Create axios instance with default configuration
const api: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8081',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add correlation ID, tracing, and logging
api.interceptors.request.use(
  (config) => {
    const correlationId = generateCorrelationId();
    if (!config.headers) {
      config.headers = {} as any;
    }
    config.headers['X-Correlation-ID'] = correlationId;
    
    // Create OpenTelemetry span for the API request
    const span = openTelemetryService.createApiSpan(
      `API ${config.method?.toUpperCase()} ${config.url}`,
      config.method || 'GET',
      config.url || '',
      {
        correlationId,
        component: 'ApiService',
        tenantId: config.headers['X-Tenant-ID'] as string || 'unknown',
        userId: config.headers['X-User-ID'] as string || 'unknown'
      }
    );
    
    // Store span in config for response interceptor
    (config as any).span = span;
    (config as any).startTime = Date.now();
    
    // Log the request
    loggingService.debug(`API Request: ${config.method?.toUpperCase()} ${config.url}`, {
      method: config.method,
      url: config.url,
      correlationId,
      component: 'ApiService',
      traceId: openTelemetryService.getCurrentTraceId(),
      spanId: openTelemetryService.getCurrentSpanId()
    });

    return config;
  },
  (error) => {
    loggingService.error('API Request Error', {
      component: 'ApiService',
      error: error.message
    });
    
    // Add error to current span
    if (error instanceof Error) {
      openTelemetryService.addError(error);
    }
    
    return Promise.reject(error);
  }
);

// Response interceptor for logging, error handling, and tracing
api.interceptors.response.use(
  (response: AxiosResponse) => {
    const startTime = (response.config as any).startTime || Date.now();
    const duration = Date.now() - startTime;
    const span = (response.config as any).span;
    
    // Add performance metrics to span
    if (span) {
      openTelemetryService.addPerformanceMetrics(
        `${response.config.method} ${response.config.url}`,
        duration,
        'SUCCESS'
      );
      
      // Add business context if available
      const tenantId = response.config.headers['X-Tenant-ID'] as string;
      const userId = response.config.headers['X-User-ID'] as string;
      if (tenantId && userId) {
        openTelemetryService.addBusinessContext(tenantId, userId, 'API_RESPONSE', {
          status: response.status,
          duration: duration,
          url: response.config.url
        });
      }
      
      // End the span
      span.end();
    }
    
    // Log successful response
    loggingService.logApiCall(
      response.config.method || 'GET',
      response.config.url || '',
      response.status,
      duration,
      {
        correlationId: response.config.headers['X-Correlation-ID'],
        component: 'ApiService'
      }
    );

    return response;
  },
  (error: AxiosError) => {
    const startTime = (error.config as any)?.startTime || Date.now();
    const duration = Date.now() - startTime;
    const span = (error.config as any)?.span;
    
    // Add error to span
    if (span) {
      openTelemetryService.addError(error);
      openTelemetryService.addPerformanceMetrics(
        `${error.config?.method} ${error.config?.url}`,
        duration,
        'ERROR'
      );
      
      // End the span
      span.end();
    }
    
    // Log error response
    loggingService.error(`API Error: ${error.config?.method} ${error.config?.url}`, {
      status: error.response?.status,
      statusText: error.response?.statusText,
      duration,
      error: error.message,
      correlationId: error.config?.headers['X-Correlation-ID'],
      component: 'ApiService'
    });

    return Promise.reject(error);
  }
);

// Generate correlation ID for request tracking
function generateCorrelationId(): string {
  return `frontend-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

// Enhanced API methods with OpenTelemetry tracing
export const apiService = {
  // GET request with tracing
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return openTelemetryService.executeAsyncWithSpan(
      `API GET ${url}`,
      {
        method: 'GET',
        url,
        component: 'ApiService'
      },
      async () => {
        try {
          const response = await api.get<T>(url, config);
          return response.data;
        } catch (error) {
          loggingService.error(`GET ${url} failed`, {
            url,
            component: 'ApiService',
            error: error instanceof Error ? error.message : String(error)
          });
          throw error;
        }
      }
    );
  },

  // POST request with tracing
  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return openTelemetryService.executeAsyncWithSpan(
      `API POST ${url}`,
      {
        method: 'POST',
        url,
        component: 'ApiService'
      },
      async () => {
        try {
          const response = await api.post<T>(url, data, config);
          return response.data;
        } catch (error) {
          loggingService.error(`POST ${url} failed`, {
            url,
            data,
            component: 'ApiService',
            error: error instanceof Error ? error.message : String(error)
          });
          throw error;
        }
      }
    );
  },

  // PUT request with tracing
  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return openTelemetryService.executeAsyncWithSpan(
      `API PUT ${url}`,
      {
        method: 'PUT',
        url,
        component: 'ApiService'
      },
      async () => {
        try {
          const response = await api.put<T>(url, data, config);
          return response.data;
        } catch (error) {
          loggingService.error(`PUT ${url} failed`, {
            url,
            data,
            component: 'ApiService',
            error: error instanceof Error ? error.message : String(error)
          });
          throw error;
        }
      }
    );
  },

  // DELETE request with tracing
  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return openTelemetryService.executeAsyncWithSpan(
      `API DELETE ${url}`,
      {
        method: 'DELETE',
        url,
        component: 'ApiService'
      },
      async () => {
        try {
          const response = await api.delete<T>(url, config);
          return response.data;
        } catch (error) {
          loggingService.error(`DELETE ${url} failed`, {
            url,
            component: 'ApiService',
            error: error instanceof Error ? error.message : String(error)
          });
          throw error;
        }
      }
    );
  },

  // PATCH request with tracing
  async patch<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return openTelemetryService.executeAsyncWithSpan(
      `API PATCH ${url}`,
      {
        method: 'PATCH',
        url,
        component: 'ApiService'
      },
      async () => {
        try {
          const response = await api.patch<T>(url, data, config);
          return response.data;
        } catch (error) {
          loggingService.error(`PATCH ${url} failed`, {
            url,
            data,
            component: 'ApiService',
            error: error instanceof Error ? error.message : String(error)
          });
          throw error;
        }
      }
    );
  },

  // Set authentication token
  setAuthToken(token: string) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    loggingService.info('Authentication token set', {
      component: 'ApiService'
    });
  },

  // Clear authentication token
  clearAuthToken() {
    delete api.defaults.headers.common['Authorization'];
    loggingService.info('Authentication token cleared', {
      component: 'ApiService'
    });
  },

  // Set tenant context
  setTenantContext(tenantId: string) {
    api.defaults.headers.common['X-Tenant-ID'] = tenantId;
    loggingService.info('Tenant context set', {
      tenantId,
      component: 'ApiService'
    });
  },

  // Get axios instance for advanced usage
  getInstance(): AxiosInstance {
    return api;
  },

  // Get current trace information
  getTraceInfo() {
    return {
      traceId: openTelemetryService.getCurrentTraceId(),
      spanId: openTelemetryService.getCurrentSpanId(),
      isReady: openTelemetryService.isReady()
    };
  }
};

// Export specific API interfaces for backward compatibility
export const userAPI = {
  getProfile: async () => {
    const response = await apiService.get('/api/users/profile');
    return { data: response };
  },
  getUserTenants: async () => {
    const response = await apiService.get('/api/users/tenants');
    return { data: response };
  },
  updateProfile: async (data: any) => {
    const response = await apiService.put('/api/users/profile', data);
    return { data: response };
  },
  getUsers: async () => {
    const response = await apiService.get('/api/users');
    return { data: response };
  },
  createUser: async (data: any) => {
    const response = await apiService.post('/api/users', data);
    return { data: response };
  },
  getUser: async (id: string) => {
    const response = await apiService.get(`/api/users/${id}`);
    return { data: response };
  },
  updateUser: async (id: string, data: any) => {
    const response = await apiService.put(`/api/users/${id}`, data);
    return { data: response };
  },
  deleteUser: async (id: string) => {
    const response = await apiService.delete(`/api/users/${id}`);
    return { data: response };
  }
};

export const authAPI = {
  login: async (credentials: { email: string; password: string }) => {
    const response = await apiService.post('/api/auth/login', credentials);
    return { data: response };
  },
  register: async (userData: any) => {
    const response = await apiService.post('/api/auth/register', userData);
    return { data: response };
  },
  logout: async () => {
    const response = await apiService.post('/api/auth/logout');
    return { data: response };
  },
  refreshToken: async () => {
    const response = await apiService.post('/api/auth/refresh');
    return { data: response };
  },
  forgotPassword: async (email: string) => {
    const response = await apiService.post('/api/auth/forgot-password', { email });
    return { data: response };
  },
  resetPassword: async (token: string, password: string) => {
    const response = await apiService.post('/api/auth/reset-password', { token, password });
    return { data: response };
  },
  verifyEmail: async (token: string) => {
    const response = await apiService.post('/api/auth/verify-email', { token });
    return { data: response };
  },
  validate: async (token: string) => {
    const response = await apiService.post('/api/auth/validate', { token });
    return { data: response };
  }
};

export const adminAPI = {
  getDashboardStats: async () => {
    const response = await apiService.get('/api/admin/dashboard');
    return { data: response };
  },
  getAllUsers: async () => {
    const response = await apiService.get('/api/admin/users');
    return { data: response };
  },
  activateUser: async (userId: string) => {
    const response = await apiService.put(`/api/admin/users/${userId}/activate`);
    return { data: response };
  },
  deactivateUser: async (userId: string) => {
    const response = await apiService.put(`/api/admin/users/${userId}/deactivate`);
    return { data: response };
  },
  deleteUser: async (userId: string) => {
    const response = await apiService.delete(`/api/admin/users/${userId}`);
    return { data: response };
  },
  createUser: async (userData: any) => {
    const response = await apiService.post('/api/admin/users', userData);
    return { data: response };
  },
  updateUser: async (userId: string, userData: any) => {
    const response = await apiService.put(`/api/admin/users/${userId}`, userData);
    return { data: response };
  }
};

export default apiService;
