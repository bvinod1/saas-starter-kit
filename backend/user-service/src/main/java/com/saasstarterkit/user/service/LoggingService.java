package com.saasstarterkit.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.saasstarterkit.user.config.TenantContext;

import java.util.Map;
import java.util.UUID;
import java.time.Instant;

@Service
public class LoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    
    // Tenant context keys for MDC
    private static final String TENANT_ID_KEY = "tenant_id";
    private static final String TENANT_NAME_KEY = "tenant_name";
    private static final String ORGANIZATION_ID_KEY = "organization_id";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String CORRELATION_ID_KEY = "correlation_id";
    private static final String SESSION_ID_KEY = "session_id";
    private static final String COMPLIANCE_LEVEL_KEY = "compliance_level";
    private static final String DATA_SENSITIVITY_KEY = "data_sensitivity";
    
    /**
     * Set tenant context for all subsequent log entries
     */
    public void setTenantContext(String tenantId, String tenantName, String organizationId) {
        MDC.put(TENANT_ID_KEY, tenantId);
        MDC.put(TENANT_NAME_KEY, tenantName);
        MDC.put(ORGANIZATION_ID_KEY, organizationId);
        
        logger.debug("Tenant context set", Map.of(
            "tenantId", tenantId,
            "tenantName", tenantName,
            "organizationId", organizationId
        ));
    }
    
    /**
     * Set user context for all subsequent log entries
     */
    public void setUserContext(String userId, String userEmail) {
        MDC.put(USER_ID_KEY, userId);
        MDC.put(USER_EMAIL_KEY, userEmail);
        
        logger.debug("User context set", Map.of(
            "userId", userId,
            "userEmail", userEmail
        ));
    }
    
    /**
     * Set compliance and data sensitivity context
     */
    public void setComplianceContext(String complianceLevel, String dataSensitivity) {
        MDC.put(COMPLIANCE_LEVEL_KEY, complianceLevel);
        MDC.put(DATA_SENSITIVITY_KEY, dataSensitivity);
    }
    
    /**
     * Clear all context from MDC
     */
    public void clearContext() {
        MDC.clear();
    }
    
    /**
     * Get current tenant context
     */
    public String getCurrentTenantId() {
        return MDC.get(TENANT_ID_KEY);
    }
    
    /**
     * Validate tenant context is set
     */
    private void validateTenantContext() {
        String tenantId = MDC.get(TENANT_ID_KEY);
        if (tenantId == null || tenantId.trim().isEmpty()) {
            logger.warn("Logging without tenant context - this may violate compliance requirements");
        }
    }
    
    /**
     * Log info message with tenant context validation
     */
    public void logInfo(String message, Map<String, String> context) {
        validateTenantContext();
        addContextToMDC(context);
        logger.info(message);
        clearContextFromMDC(context);
    }
    
    /**
     * Log debug message with tenant context validation
     */
    public void logDebug(String message, Map<String, String> context) {
        validateTenantContext();
        addContextToMDC(context);
        logger.debug(message);
        clearContextFromMDC(context);
    }
    
    /**
     * Log error message with tenant context validation
     */
    public void logError(String message, Throwable throwable, Map<String, String> context) {
        validateTenantContext();
        addContextToMDC(context);
        logger.error(message, throwable);
        clearContextFromMDC(context);
    }
    
    /**
     * Log warning message with tenant context validation
     */
    public void logWarn(String message, Map<String, String> context) {
        validateTenantContext();
        addContextToMDC(context);
        logger.warn(message);
        clearContextFromMDC(context);
    }
    
    /**
     * Log trace message with tenant context validation
     */
    public void logTrace(String message, Map<String, String> context) {
        validateTenantContext();
        addContextToMDC(context);
        logger.trace(message);
        clearContextFromMDC(context);
    }
    
    /**
     * Log business event with comprehensive tenant context
     */
    public void logBusinessEvent(String eventType, String userId, String tenantId, 
                                Map<String, Object> eventData, String complianceLevel) {
        Map<String, String> context = Map.of(
            "eventType", eventType,
            "userId", userId,
            "tenantId", tenantId,
            "eventId", UUID.randomUUID().toString(),
            "complianceLevel", complianceLevel,
            "timestamp", Instant.now().toString(),
            "dataClassification", determineDataClassification(eventData)
        );
        
        addContextToMDC(context);
        logger.info("Business Event: {} - User: {} - Tenant: {} - Compliance: {} - Data: {}", 
                   eventType, userId, tenantId, complianceLevel, eventData);
        clearContextFromMDC(context);
    }
    
    /**
     * Log performance metric with tenant attribution
     */
    public void logPerformanceMetric(String operation, long durationMs, 
                                   Map<String, String> context) {
        Map<String, String> perfContext = Map.of(
            "operation", operation,
            "durationMs", String.valueOf(durationMs),
            "metricType", "performance",
            "tenantId", getCurrentTenantId(),
            "timestamp", Instant.now().toString()
        );
        
        addContextToMDC(perfContext);
        logger.info("Performance Metric: {} took {}ms for tenant {}", 
                   operation, durationMs, getCurrentTenantId());
        clearContextFromMDC(perfContext);
    }
    
    /**
     * Log security event with compliance tracking
     */
    public void logSecurityEvent(String eventType, String userId, String ipAddress, 
                                String details, String complianceLevel, Map<String, String> context) {
        Map<String, String> securityContext = Map.of(
            "securityEventType", eventType,
            "userId", userId,
            "ipAddress", ipAddress,
            "details", details,
            "eventId", UUID.randomUUID().toString(),
            "complianceLevel", complianceLevel,
            "tenantId", getCurrentTenantId(),
            "timestamp", Instant.now().toString(),
            "severity", determineSecuritySeverity(eventType)
        );
        
        addContextToMDC(securityContext);
        logger.warn("Security Event: {} - User: {} - IP: {} - Tenant: {} - Compliance: {} - Details: {}", 
                   eventType, userId, ipAddress, getCurrentTenantId(), complianceLevel, details);
        clearContextFromMDC(securityContext);
    }
    
    /**
     * Log data access event for compliance tracking
     */
    public void logDataAccessEvent(String dataType, String operation, String userId, 
                                  String tenantId, String dataSensitivity, Map<String, Object> accessDetails) {
        Map<String, String> accessContext = Map.of(
            "dataAccessType", dataType,
            "operation", operation,
            "userId", userId,
            "tenantId", tenantId,
            "dataSensitivity", dataSensitivity,
            "accessId", UUID.randomUUID().toString(),
            "timestamp", Instant.now().toString(),
            "complianceRequired", String.valueOf(isComplianceRequired(dataSensitivity))
        );
        
        addContextToMDC(accessContext);
        logger.info("Data Access: {} {} on {} data for tenant {} by user {}", 
                   operation, dataType, dataSensitivity, tenantId, userId);
        clearContextFromMDC(accessContext);
    }
    
    /**
     * Log tenant isolation event
     */
    public void logTenantIsolationEvent(String eventType, String tenantId, 
                                       String details, Map<String, String> context) {
        Map<String, String> isolationContext = Map.of(
            "isolationEventType", eventType,
            "tenantId", tenantId,
            "details", details,
            "eventId", UUID.randomUUID().toString(),
            "timestamp", Instant.now().toString(),
            "isolationLevel", determineIsolationLevel(tenantId)
        );
        
        addContextToMDC(isolationContext);
        logger.info("Tenant Isolation: {} for tenant {} - {}", eventType, tenantId, details);
        clearContextFromMDC(isolationContext);
    }
    
    /**
     * Log compliance audit event
     */
    public void logComplianceAuditEvent(String auditType, String tenantId, 
                                       String userId, String details, String complianceStandard) {
        Map<String, String> auditContext = Map.of(
            "auditType", auditType,
            "tenantId", tenantId,
            "userId", userId,
            "details", details,
            "complianceStandard", complianceStandard,
            "auditId", UUID.randomUUID().toString(),
            "timestamp", Instant.now().toString(),
            "auditRequired", "true"
        );
        
        addContextToMDC(auditContext);
        logger.info("Compliance Audit: {} for tenant {} by user {} - Standard: {} - Details: {}", 
                   auditType, tenantId, userId, complianceStandard, details);
        clearContextFromMDC(auditContext);
    }
    
    /**
     * Determine data classification based on content
     */
    private String determineDataClassification(Map<String, Object> data) {
        // Implement data classification logic based on content analysis
        if (data.containsKey("ssn") || data.containsKey("creditCard")) {
            return "PII_HIGH";
        } else if (data.containsKey("email") || data.containsKey("phone")) {
            return "PII_MEDIUM";
        } else if (data.containsKey("address") || data.containsKey("name")) {
            return "PII_LOW";
        }
        return "PUBLIC";
    }
    
    /**
     * Determine security event severity
     */
    private String determineSecuritySeverity(String eventType) {
        switch (eventType.toLowerCase()) {
            case "login_failure":
            case "unauthorized_access":
            case "data_breach":
                return "HIGH";
            case "suspicious_activity":
            case "privilege_escalation":
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
    
    /**
     * Determine tenant isolation level
     */
    private String determineIsolationLevel(String tenantId) {
        // Implement logic to determine isolation level based on tenant configuration
        // This could be based on tenant tier, compliance requirements, etc.
        return "STRICT"; // Default to strict isolation
    }
    
    /**
     * Check if compliance tracking is required
     */
    private boolean isComplianceRequired(String dataSensitivity) {
        return "PII_HIGH".equals(dataSensitivity) || "PII_MEDIUM".equals(dataSensitivity);
    }
    
    private void addContextToMDC(Map<String, String> context) {
        if (context != null) {
            context.forEach(MDC::put);
        }
    }
    
    private void clearContextFromMDC(Map<String, String> context) {
        if (context != null) {
            context.keySet().forEach(MDC::remove);
        }
    }
}
