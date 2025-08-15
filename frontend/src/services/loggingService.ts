import { v4 as uuidv4 } from 'uuid';

export enum LogLevel {
  TRACE = 0,
  DEBUG = 1,
  INFO = 2,
  WARN = 3,
  ERROR = 4,
  FATAL = 5,
}

export interface LogContext {
  correlationId?: string;
  userId?: string;
  tenantId?: string;
  tenantName?: string;
  organizationId?: string;
  sessionId?: string;
  userAgent?: string;
  url?: string;
  component?: string;
  action?: string;
  complianceLevel?: string;
  dataSensitivity?: string;
  auditRequired?: boolean;
  [key: string]: any;
}

export interface LogEntry {
  timestamp: string;
  level: LogLevel;
  message: string;
  context: LogContext;
  error?: Error;
  data?: any;
}

class LoggingService {
  private logLevel: LogLevel = LogLevel.INFO;
  private sessionId: string;
  private userId?: string;
  private tenantId?: string;
  private tenantName?: string;
  private organizationId?: string;
  private correlationId?: string;
  private complianceLevel: string = 'BASIC';
  private dataSensitivity: string = 'PUBLIC';
  private isProduction: boolean;

  constructor() {
    this.sessionId = uuidv4();
    this.isProduction = process.env.NODE_ENV === 'production';
    this.initializeLogging();
  }

  private initializeLogging() {
    // Set log level based on environment
    if (process.env.REACT_APP_LOG_LEVEL) {
      this.logLevel = parseInt(process.env.REACT_APP_LOG_LEVEL) as LogLevel;
    } else if (this.isProduction) {
      this.logLevel = LogLevel.WARN;
    } else {
      this.logLevel = LogLevel.DEBUG;
    }

    // Set up global error handler
    window.addEventListener('error', (event) => {
      this.error('Global error occurred', {
        url: window.location.href,
        userAgent: navigator.userAgent,
        component: 'GlobalErrorHandler',
        error: event.error
      });
    });

    // Set up unhandled promise rejection handler
    window.addEventListener('unhandledrejection', (event) => {
      this.error('Unhandled promise rejection', {
        url: window.location.href,
        userAgent: navigator.userAgent,
        component: 'GlobalErrorHandler',
        error: event.reason
      });
    });

    this.info('Logging service initialized', {
      logLevel: LogLevel[this.logLevel],
      sessionId: this.sessionId,
      environment: process.env.NODE_ENV
    });
  }

  setTenantContext(tenantId: string, tenantName?: string, organizationId?: string) {
    this.tenantId = tenantId;
    this.tenantName = tenantName || `Tenant-${tenantId}`;
    this.organizationId = organizationId;
    
    // Determine compliance level based on tenant
    this.complianceLevel = this.determineComplianceLevel(tenantId);
    
    this.info('Tenant context set', { 
      tenantId, 
      tenantName: this.tenantName, 
      organizationId,
      complianceLevel: this.complianceLevel
    });
  }

  setUserContext(userId: string, userEmail?: string) {
    this.userId = userId;
    this.info('User context set', { 
      userId, 
      userEmail,
      tenantId: this.tenantId,
      complianceLevel: this.complianceLevel
    });
  }

  setCorrelationId(correlationId: string) {
    this.correlationId = correlationId;
  }

  setComplianceContext(complianceLevel: string, dataSensitivity: string) {
    this.complianceLevel = complianceLevel;
    this.dataSensitivity = dataSensitivity;
  }

  private shouldLog(level: LogLevel): boolean {
    return level >= this.logLevel;
  }

  private createLogEntry(level: LogLevel, message: string, context: LogContext, error?: Error, data?: any): LogEntry {
    return {
      timestamp: new Date().toISOString(),
      level,
      message,
      context: {
        ...context,
        sessionId: this.sessionId,
        userId: this.userId,
        tenantId: this.tenantId,
        tenantName: this.tenantName,
        organizationId: this.organizationId,
        correlationId: this.correlationId,
        userAgent: navigator.userAgent,
        url: window.location.href,
        complianceLevel: this.complianceLevel,
        dataSensitivity: this.dataSensitivity,
        auditRequired: this.isAuditRequired(),
      },
      error,
      data
    };
  }

  private log(level: LogLevel, message: string, context: LogContext = {}, error?: Error, data?: any) {
    if (!this.shouldLog(level)) return;

    // Validate tenant context for compliance
    this.validateTenantContext();

    const logEntry = this.createLogEntry(level, message, context, error, data);
    
    // Console logging
    const logMethod = this.getConsoleMethod(level);
    if (logMethod) {
      logMethod(message, logEntry);
    }

    // Send to backend logging service if available
    this.sendToBackend(logEntry);

    // Store in localStorage for debugging (development only)
    if (!this.isProduction) {
      this.storeInLocalStorage(logEntry);
    }
  }

  private validateTenantContext() {
    if (!this.tenantId && this.isProduction) {
      console.warn('Logging without tenant context - this may violate compliance requirements');
    }
  }

  private isAuditRequired(): boolean {
    return this.complianceLevel !== 'BASIC' || this.dataSensitivity !== 'PUBLIC';
  }

  private determineComplianceLevel(tenantId: string): string {
    if (!tenantId) return 'BASIC';
    
    // Example logic - in production, this would come from tenant configuration
    if (tenantId.startsWith('enterprise_')) {
      return 'ENTERPRISE';
    } else if (tenantId.startsWith('healthcare_')) {
      return 'HIPAA';
    } else if (tenantId.startsWith('financial_')) {
      return 'SOX_PCI';
    }
    
    return 'BASIC';
  }

  private getConsoleMethod(level: LogLevel): ((message: string, data?: any) => void) | null {
    switch (level) {
      case LogLevel.TRACE:
        return console.trace;
      case LogLevel.DEBUG:
        return console.debug;
      case LogLevel.INFO:
        return console.info;
      case LogLevel.WARN:
        return console.warn;
      case LogLevel.ERROR:
      case LogLevel.FATAL:
        return console.error;
      default:
        return console.log;
    }
  }

  private async sendToBackend(logEntry: LogEntry) {
    try {
      // Only send ERROR and FATAL logs to backend in production
      if (this.isProduction && logEntry.level < LogLevel.ERROR) {
        return;
      }

      const response = await fetch('/api/logs', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Correlation-ID': this.correlationId || '',
          'X-Tenant-ID': this.tenantId || '',
          'X-User-ID': this.userId || '',
        },
        body: JSON.stringify(logEntry),
      });

      if (!response.ok) {
        console.warn('Failed to send log to backend:', response.status);
      }
    } catch (error) {
      // Don't log logging errors to avoid infinite loops
      console.warn('Failed to send log to backend:', error);
    }
  }

  private storeInLocalStorage(logEntry: LogEntry) {
    try {
      const logs = JSON.parse(localStorage.getItem('app_logs') || '[]');
      logs.push(logEntry);
      
      // Keep only last 100 logs
      if (logs.length > 100) {
        logs.splice(0, logs.length - 100);
      }
      
      localStorage.setItem('app_logs', JSON.stringify(logs));
    } catch (error) {
      console.warn('Failed to store log in localStorage:', error);
    }
  }

  trace(message: string, context: LogContext = {}) {
    this.log(LogLevel.TRACE, message, context);
  }

  debug(message: string, context: LogContext = {}) {
    this.log(LogLevel.DEBUG, message, context);
  }

  info(message: string, context: LogContext = {}) {
    this.log(LogLevel.INFO, message, context);
  }

  warn(message: string, context: LogContext = {}) {
    this.log(LogLevel.WARN, message, context);
  }

  error(message: string, context: LogContext = {}, error?: Error) {
    this.log(LogLevel.ERROR, message, context, error);
  }

  fatal(message: string, context: LogContext = {}, error?: Error) {
    this.log(LogLevel.FATAL, message, context, error);
  }

  // Business event logging with tenant context
  logBusinessEvent(eventType: string, userId: string, tenantId: string, eventData: any) {
    this.info(`Business Event: ${eventType}`, {
      eventType,
      userId,
      tenantId,
      eventData,
      component: 'BusinessEvent',
      complianceLevel: this.complianceLevel,
      auditRequired: this.isAuditRequired()
    });
  }

  // Performance logging with tenant attribution
  logPerformanceMetric(operation: string, durationMs: number, context: LogContext = {}) {
    this.info(`Performance: ${operation} took ${durationMs}ms`, {
      ...context,
      operation,
      durationMs,
      metricType: 'performance',
      tenantId: this.tenantId,
      complianceLevel: this.complianceLevel
    });
  }

  // Security event logging with compliance tracking
  logSecurityEvent(eventType: string, userId: string, details: string, context: LogContext = {}) {
    this.warn(`Security Event: ${eventType}`, {
      ...context,
      securityEventType: eventType,
      userId,
      details,
      tenantId: this.tenantId,
      complianceLevel: this.complianceLevel,
      auditRequired: true
    });
  }

  // API call logging with tenant context
  logApiCall(method: string, url: string, status: number, durationMs: number, context: LogContext = {}) {
    const level = status >= 400 ? LogLevel.ERROR : LogLevel.DEBUG;
    this.log(level, `API ${method} ${url} - ${status} (${durationMs}ms)`, {
      ...context,
      method,
      url,
      status,
      durationMs,
      component: 'ApiCall',
      tenantId: this.tenantId,
      complianceLevel: this.complianceLevel
    });
  }

  // Data access logging for compliance
  logDataAccess(dataType: string, operation: string, dataSensitivity: string, context: LogContext = {}) {
    this.info(`Data Access: ${operation} ${dataType}`, {
      ...context,
      dataAccessType: dataType,
      operation,
      dataSensitivity,
      tenantId: this.tenantId,
      complianceLevel: this.complianceLevel,
      auditRequired: this.isAuditRequired()
    });
  }

  // Tenant isolation event logging
  logTenantIsolation(eventType: string, details: string, context: LogContext = {}) {
    this.info(`Tenant Isolation: ${eventType}`, {
      ...context,
      isolationEventType: eventType,
      details,
      tenantId: this.tenantId,
      complianceLevel: this.complianceLevel,
      auditRequired: true
    });
  }

  // Compliance audit logging
  logComplianceAudit(auditType: string, details: string, complianceStandard: string, context: LogContext = {}) {
    this.info(`Compliance Audit: ${auditType}`, {
      ...context,
      auditType,
      details,
      complianceStandard,
      tenantId: this.tenantId,
      complianceLevel: this.complianceLevel,
      auditRequired: true
    });
  }

  // Get stored logs for debugging
  getStoredLogs(): LogEntry[] {
    try {
      return JSON.parse(localStorage.getItem('app_logs') || '[]');
    } catch (error) {
      return [];
    }
  }

  // Clear stored logs
  clearStoredLogs() {
    localStorage.removeItem('app_logs');
  }

  // Get current tenant context
  getCurrentTenantId(): string | undefined {
    return this.tenantId;
  }

  // Get current compliance level
  getCurrentComplianceLevel(): string {
    return this.complianceLevel;
  }
}

// Create singleton instance
export const loggingService = new LoggingService();

// Export for use in components
export default loggingService;
