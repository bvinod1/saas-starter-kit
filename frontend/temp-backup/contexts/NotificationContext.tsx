import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';

// =============================================
// TYPES
// =============================================

export interface Notification {
  id: string;
  userId: string;
  tenantId?: string;
  organizationId?: string;
  type: 'EMAIL' | 'SMS' | 'PUSH' | 'IN_APP' | 'WEBHOOK';
  title?: string;
  message: string;
  subject?: string;
  recipient: string;
  status: 'PENDING' | 'SCHEDULED' | 'SENDING' | 'SENT' | 'DELIVERED' | 'READ' | 'FAILED' | 'CANCELLED' | 'EXPIRED';
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  createdAt: string;
  readAt?: string;
  data?: Record<string, any>;
}

export interface NotificationPreferences {
  emailEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  inAppEnabled: boolean;
  webhookEnabled: boolean;
  quietHoursStart?: string;
  quietHoursEnd?: string;
  timezone: string;
  preferredLanguage: string;
  fallbackLanguage: string;
  digestFrequency: 'IMMEDIATE' | 'HOURLY' | 'DAILY' | 'WEEKLY' | 'NEVER';
  maxNotificationsPerDay: number;
  urgentNotificationsAlways: boolean;
  categoryPreferences?: Record<string, any>;
}

export interface NotificationTemplate {
  id: string;
  name: string;
  type: 'EMAIL' | 'SMS' | 'PUSH' | 'IN_APP' | 'WEBHOOK';
  subject?: string;
  content: string;
  language: string;
  isActive: boolean;
  tenantId?: string;
  category?: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
}

// =============================================
// STATE
// =============================================

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  preferences: NotificationPreferences | null;
  templates: NotificationTemplate[];
  loading: boolean;
  error: string | null;
}

// =============================================
// ACTIONS
// =============================================

type NotificationAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_NOTIFICATIONS'; payload: Notification[] }
  | { type: 'ADD_NOTIFICATION'; payload: Notification }
  | { type: 'UPDATE_NOTIFICATION'; payload: { id: string; updates: Partial<Notification> } }
  | { type: 'REMOVE_NOTIFICATION'; payload: string }
  | { type: 'SET_PREFERENCES'; payload: NotificationPreferences }
  | { type: 'UPDATE_PREFERENCES'; payload: Partial<NotificationPreferences> }
  | { type: 'SET_TEMPLATES'; payload: NotificationTemplate[] }
  | { type: 'MARK_AS_READ'; payload: string }
  | { type: 'MARK_ALL_AS_READ' }
  | { type: 'SET_UNREAD_COUNT'; payload: number };

// =============================================
// REDUCER
// =============================================

const notificationReducer = (state: NotificationState, action: NotificationAction): NotificationState => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    
    case 'SET_NOTIFICATIONS':
      return { 
        ...state, 
        notifications: action.payload,
        unreadCount: action.payload.filter(n => !n.readAt).length
      };
    
    case 'ADD_NOTIFICATION':
      return { 
        ...state, 
        notifications: [action.payload, ...state.notifications],
        unreadCount: state.unreadCount + 1
      };
    
    case 'UPDATE_NOTIFICATION':
      return {
        ...state,
        notifications: state.notifications.map(n => 
          n.id === action.payload.id ? { ...n, ...action.payload.updates } : n
        )
      };
    
    case 'REMOVE_NOTIFICATION':
      return {
        ...state,
        notifications: state.notifications.filter(n => n.id !== action.payload)
      };
    
    case 'SET_PREFERENCES':
      return { ...state, preferences: action.payload };
    
    case 'UPDATE_PREFERENCES':
      return { 
        ...state, 
        preferences: state.preferences ? { ...state.preferences, ...action.payload } : null 
      };
    
    case 'SET_TEMPLATES':
      return { ...state, templates: action.payload };
    
    case 'MARK_AS_READ':
      return {
        ...state,
        notifications: state.notifications.map(n => 
          n.id === action.payload ? { ...n, status: 'READ', readAt: new Date().toISOString() } : n
        ),
        unreadCount: Math.max(0, state.unreadCount - 1)
      };
    
    case 'MARK_ALL_AS_READ':
      return {
        ...state,
        notifications: state.notifications.map(n => ({ ...n, status: 'READ', readAt: new Date().toISOString() })),
        unreadCount: 0
      };
    
    case 'SET_UNREAD_COUNT':
      return { ...state, unreadCount: action.payload };
    
    default:
      return state;
  }
};

// =============================================
// CONTEXT
// =============================================

interface NotificationContextType {
  state: NotificationState;
  dispatch: React.Dispatch<NotificationAction>;
  // Actions
  fetchNotifications: () => Promise<void>;
  fetchPreferences: () => Promise<void>;
  updatePreferences: (preferences: Partial<NotificationPreferences>) => Promise<void>;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  deleteNotification: (id: string) => Promise<void>;
  sendTestNotification: (type: Notification['type'], message: string) => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

// =============================================
// PROVIDER
// =============================================

interface NotificationProviderProps {
  children: ReactNode;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(notificationReducer, {
    notifications: [],
    unreadCount: 0,
    preferences: null,
    templates: [],
    loading: false,
    error: null
  });

  // =============================================
  // ACTIONS
  // =============================================

  const fetchNotifications = async () => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      // TODO: Implement API call to fetch notifications
      const response = await fetch('/api/notifications');
      if (!response.ok) throw new Error('Failed to fetch notifications');
      
      const notifications = await response.json();
      dispatch({ type: 'SET_NOTIFICATIONS', payload: notifications });
      
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: error instanceof Error ? error.message : 'Unknown error' });
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const fetchPreferences = async () => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_ERROR', payload: null });
      
      // TODO: Implement API call to fetch preferences
      const response = await fetch('/api/notifications/preferences');
      if (!response.ok) throw new Error('Failed to fetch preferences');
      
      const preferences = await response.json();
      dispatch({ type: 'SET_PREFERENCES', payload: preferences });
      
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: error instanceof Error ? error.message : 'Unknown error' });
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const updatePreferences = async (updates: Partial<NotificationPreferences>) => {
    try {
      dispatch({ type: 'SET_ERROR', payload: null });
      
      // TODO: Implement API call to update preferences
      const response = await fetch('/api/notifications/preferences', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      });
      
      if (!response.ok) throw new Error('Failed to update preferences');
      
      dispatch({ type: 'UPDATE_PREFERENCES', payload: updates });
      
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: error instanceof Error ? error.message : 'Unknown error' });
      throw error;
    }
  };

  const markAsRead = async (id: string) => {
    try {
      // TODO: Implement API call to mark as read
      await fetch(`/api/notifications/${id}/read`, { method: 'PUT' });
      dispatch({ type: 'MARK_AS_READ', payload: id });
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      // TODO: Implement API call to mark all as read
      await fetch('/api/notifications/read-all', { method: 'PUT' });
      dispatch({ type: 'MARK_ALL_AS_READ' });
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error);
    }
  };

  const deleteNotification = async (id: string) => {
    try {
      // TODO: Implement API call to delete notification
      await fetch(`/api/notifications/${id}`, { method: 'DELETE' });
      dispatch({ type: 'REMOVE_NOTIFICATION', payload: id });
    } catch (error) {
      console.error('Failed to delete notification:', error);
    }
  };

  const sendTestNotification = async (type: Notification['type'], message: string) => {
    try {
      // TODO: Implement API call to send test notification
      await fetch('/api/notifications/test', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, message })
      });
    } catch (error) {
      console.error('Failed to send test notification:', error);
    }
  };

  // =============================================
  // EFFECTS
  // =============================================

  useEffect(() => {
    fetchNotifications();
    fetchPreferences();
  }, []);

  // =============================================
  // CONTEXT VALUE
  // =============================================

  const contextValue: NotificationContextType = {
    state,
    dispatch,
    fetchNotifications,
    fetchPreferences,
    updatePreferences,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    sendTestNotification
  };

  return (
    <NotificationContext.Provider value={contextValue}>
      {children}
    </NotificationContext.Provider>
  );
};

// =============================================
// HOOK
// =============================================

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
