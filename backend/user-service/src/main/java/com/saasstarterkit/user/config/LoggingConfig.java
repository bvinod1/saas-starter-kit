package com.saasstarterkit.user.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import com.saasstarterkit.user.service.LoggingService;
import com.saasstarterkit.user.service.TenantService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.Map;

@Configuration
public class LoggingConfig implements WebMvcConfigurer {

    private final LoggingService loggingService;
    private final TenantService tenantService;

    public LoggingConfig(LoggingService loggingService, TenantService tenantService) {
        this.loggingService = loggingService;
        this.tenantService = tenantService;
    }

    /**
     * Request logging filter for detailed HTTP request/response logging
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        filter.setAfterMessageSuffix("");
        return filter;
    }

    /**
     * Interceptor to add correlation ID, tenant context, and compliance tracking to MDC
     * This ensures tenant ID is ALWAYS injected at the boundary for automated propagation
     */
    @Bean
    public HandlerInterceptor mdcInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                // Generate correlation ID if not present
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId == null || correlationId.isEmpty()) {
                    correlationId = generateCorrelationId();
                }
                
                // CRITICAL: Extract tenant context from multiple sources with fallback
                String tenantId = extractTenantId(request);
                String tenantName = extractTenantName(request);
                String organizationId = extractOrganizationId(request);
                String userId = extractUserId(request);
                String userEmail = extractUserEmail(request);
                
                // VALIDATE: Ensure tenant context is always present
                if (tenantId == null || tenantId.trim().isEmpty()) {
                    // Log security warning for missing tenant context
                    loggingService.logSecurityEvent(
                        "MISSING_TENANT_CONTEXT",
                        userId != null ? userId : "unknown",
                        getClientIpAddress(request),
                        "Request without tenant context - potential security violation",
                        "HIGH",
                        Map.of("url", request.getRequestURI(), "method", request.getMethod())
                    );
                    
                    // Set default tenant context for security logging
                    tenantId = "SECURITY_VIOLATION";
                    tenantName = "Security Violation";
                }
                
                // Set tenant context in logging service
                loggingService.setTenantContext(tenantId, tenantName, organizationId);
                
                // Set user context if available
                if (userId != null && userEmail != null) {
                    loggingService.setUserContext(userId, userEmail);
                }
                
                // Add to MDC for logging context - CRITICAL for tenant isolation
                MDC.put("correlationId", correlationId);
                MDC.put("requestId", UUID.randomUUID().toString());
                MDC.put("userAgent", request.getHeader("User-Agent"));
                MDC.put("clientIp", getClientIpAddress(request));
                MDC.put("requestMethod", request.getMethod());
                MDC.put("requestUri", request.getRequestURI());
                MDC.put("tenantId", tenantId);
                MDC.put("tenantName", tenantName);
                MDC.put("organizationId", organizationId);
                MDC.put("userId", userId);
                MDC.put("userEmail", userEmail);
                MDC.put("sessionId", request.getSession().getId());
                MDC.put("requestTimestamp", String.valueOf(System.currentTimeMillis()));
                
                // Add compliance context
                MDC.put("complianceLevel", determineComplianceLevel(tenantId));
                MDC.put("dataSensitivity", determineDataSensitivity(request));
                MDC.put("auditRequired", "true");
                
                // Add security context
                MDC.put("securityLevel", determineSecurityLevel(tenantId));
                MDC.put("accessLevel", determineAccessLevel(request));
                MDC.put("tenantIsolation", "enabled");
                
                // Add correlation ID to response headers
                response.addHeader("X-Correlation-ID", correlationId);
                response.addHeader("X-Request-ID", MDC.get("requestId"));
                response.addHeader("X-Tenant-ID", tenantId);
                response.addHeader("X-User-ID", userId != null ? userId : "");
                
                // Log tenant context injection for audit
                loggingService.logTenantIsolationEvent(
                    "TENANT_CONTEXT_INJECTED",
                    tenantId,
                    "Tenant context injected at request boundary",
                    Map.of("url", request.getRequestURI(), "method", request.getMethod())
                );
                
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                // Add response information to MDC
                MDC.put("responseStatus", String.valueOf(response.getStatus()));
                MDC.put("responseTime", String.valueOf(System.currentTimeMillis()));
                MDC.put("responseSize", String.valueOf(response.getBufferSize()));
                
                // Log compliance audit event for sensitive operations
                if (isSensitiveOperation(request)) {
                    String tenantId = MDC.get("tenantId");
                    String userId = MDC.get("userId");
                    String operation = request.getMethod() + " " + request.getRequestURI();
                    
                    loggingService.logComplianceAuditEvent(
                        "API_ACCESS",
                        tenantId,
                        userId,
                        operation,
                        "GDPR_SOC2"
                    );
                }
                
                // Log tenant isolation completion
                String tenantId = MDC.get("tenantId");
                if (tenantId != null && !"SECURITY_VIOLATION".equals(tenantId)) {
                    loggingService.logTenantIsolationEvent(
                        "REQUEST_COMPLETED",
                        tenantId,
                        "Request completed with tenant isolation maintained",
                        Map.of("status", String.valueOf(response.getStatus()))
                    );
                }
                
                // Clean up MDC
                loggingService.clearContext();
            }

            private String getClientIpAddress(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                
                return request.getRemoteAddr();
            }
            
            private String extractTenantId(HttpServletRequest request) {
                // Try multiple sources for tenant ID with priority order
                String tenantId = request.getHeader("X-Tenant-ID");
                if (tenantId == null || tenantId.isEmpty()) {
                    tenantId = request.getHeader("X-Tenant");
                }
                if (tenantId == null || tenantId.isEmpty()) {
                    tenantId = (String) request.getAttribute("tenantId");
                }
                if (tenantId == null || tenantId.isEmpty()) {
                    // Extract from URL path if using path-based tenancy
                    String path = request.getRequestURI();
                    if (path.startsWith("/api/tenants/")) {
                        String[] pathParts = path.split("/");
                        if (pathParts.length > 3) {
                            tenantId = pathParts[3];
                        }
                    }
                }
                if (tenantId == null || tenantId.isEmpty()) {
                    // Extract from subdomain if using subdomain-based tenancy
                    String host = request.getHeader("Host");
                    if (host != null && host.contains(".")) {
                        String subdomain = host.split("\\.")[0];
                        if (!subdomain.equals("www") && !subdomain.equals("api")) {
                            tenantId = subdomain;
                        }
                    }
                }
                return tenantId;
            }
            
            private String extractTenantName(HttpServletRequest request) {
                String tenantName = request.getHeader("X-Tenant-Name");
                if (tenantName == null || tenantName.isEmpty()) {
                    tenantName = (String) request.getAttribute("tenantName");
                }
                if (tenantName == null || tenantName.isEmpty()) {
                    // Try to get tenant name from tenant service if tenant ID is available
                    String tenantId = extractTenantId(request);
                    if (tenantId != null && !tenantId.isEmpty()) {
                        try {
                            // This would typically come from a cached tenant lookup
                            tenantName = "Tenant-" + tenantId;
                        } catch (Exception e) {
                            tenantName = "Unknown";
                        }
                    }
                }
                return tenantName != null ? tenantName : "Unknown";
            }
            
            private String extractOrganizationId(HttpServletRequest request) {
                String orgId = request.getHeader("X-Organization-ID");
                if (orgId == null || orgId.isEmpty()) {
                    orgId = (String) request.getAttribute("organizationId");
                }
                return orgId;
            }
            
            private String extractUserId(HttpServletRequest request) {
                String userId = request.getHeader("X-User-ID");
                if (userId == null || userId.isEmpty()) {
                    userId = request.getHeader("X-User");
                }
                if (userId == null || userId.isEmpty()) {
                    userId = (String) request.getAttribute("userId");
                }
                return userId;
            }
            
            private String extractUserEmail(HttpServletRequest request) {
                String userEmail = request.getHeader("X-User-Email");
                if (userEmail == null || userEmail.isEmpty()) {
                    userEmail = (String) request.getAttribute("userEmail");
                }
                return userEmail;
            }
            
            private String determineComplianceLevel(String tenantId) {
                // Implement logic to determine compliance level based on tenant configuration
                if (tenantId == null || "SECURITY_VIOLATION".equals(tenantId)) {
                    return "SECURITY_VIOLATION";
                }
                
                // Example logic - in production, this would come from tenant configuration
                if (tenantId.startsWith("enterprise_")) {
                    return "ENTERPRISE";
                } else if (tenantId.startsWith("healthcare_")) {
                    return "HIPAA";
                } else if (tenantId.startsWith("financial_")) {
                    return "SOX_PCI";
                }
                
                return "BASIC";
            }
            
            private String determineSecurityLevel(String tenantId) {
                if (tenantId == null || "SECURITY_VIOLATION".equals(tenantId)) {
                    return "CRITICAL";
                }
                
                if (tenantId.startsWith("enterprise_")) {
                    return "HIGH";
                } else if (tenantId.startsWith("healthcare_")) {
                    return "HIGH";
                } else if (tenantId.startsWith("financial_")) {
                    return "HIGH";
                }
                
                return "MEDIUM";
            }
            
            private String determineAccessLevel(HttpServletRequest request) {
                String path = request.getRequestURI();
                String method = request.getMethod();
                
                if (path.contains("/admin") || path.contains("/system")) {
                    return "ADMIN";
                } else if (path.contains("/users") || path.contains("/data")) {
                    return "USER";
                } else if (path.contains("/public") || path.contains("/health")) {
                    return "PUBLIC";
                }
                
                return "STANDARD";
            }
            
            private String determineDataSensitivity(HttpServletRequest request) {
                // Determine data sensitivity based on endpoint and operation
                String path = request.getRequestURI();
                String method = request.getMethod();
                
                // High sensitivity endpoints
                if (path.contains("/users/profile") || path.contains("/billing") || 
                    path.contains("/admin") || path.contains("/audit")) {
                    return "PII_HIGH";
                }
                
                // Medium sensitivity endpoints
                if (path.contains("/users") || path.contains("/organizations") || 
                    path.contains("/tenants")) {
                    return "PII_MEDIUM";
                }
                
                // Low sensitivity endpoints
                if (path.contains("/public") || path.contains("/health") || 
                    path.contains("/metrics")) {
                    return "PII_LOW";
                }
                
                return "PUBLIC";
            }
            
            private boolean isSensitiveOperation(HttpServletRequest request) {
                String path = request.getRequestURI();
                String method = request.getMethod();
                
                // Consider operations on sensitive data as sensitive operations
                return (path.contains("/users") || path.contains("/admin") || 
                        path.contains("/billing") || path.contains("/audit")) &&
                       ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method));
            }
            
            private String generateCorrelationId() {
                return "req-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcInterceptor());
    }
}
