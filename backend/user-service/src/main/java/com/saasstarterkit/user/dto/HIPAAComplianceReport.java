package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for HIPAA compliance reports.
 */
public class HIPAAComplianceReport {
    
    private String reportId;
    private String tenantId;
    private LocalDateTime reportDate;
    private String status;
    private Map<String, Object> privacyRule;
    private Map<String, Object> securityRule;
    private Map<String, Object> breachNotification;
    private List<String> violations;
    private String recommendations;
    
    public HIPAAComplianceReport() {}
    
    public HIPAAComplianceReport(String reportId, String tenantId, LocalDateTime reportDate, String status,
                                Map<String, Object> privacyRule, Map<String, Object> securityRule,
                                Map<String, Object> breachNotification, List<String> violations, String recommendations) {
        this.reportId = reportId;
        this.tenantId = tenantId;
        this.reportDate = reportDate;
        this.status = status;
        this.privacyRule = privacyRule;
        this.securityRule = securityRule;
        this.breachNotification = breachNotification;
        this.violations = violations;
        this.recommendations = recommendations;
    }
    
    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Map<String, Object> getPrivacyRule() { return privacyRule; }
    public void setPrivacyRule(Map<String, Object> privacyRule) { this.privacyRule = privacyRule; }
    
    public Map<String, Object> getSecurityRule() { return securityRule; }
    public void setSecurityRule(Map<String, Object> securityRule) { this.securityRule = securityRule; }
    
    public Map<String, Object> getBreachNotification() { return breachNotification; }
    public void setBreachNotification(Map<String, Object> breachNotification) { this.breachNotification = breachNotification; }
    
    public List<String> getViolations() { return violations; }
    public void setViolations(List<String> violations) { this.violations = violations; }
    
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}
