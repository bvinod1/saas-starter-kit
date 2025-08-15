package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for GDPR compliance reports.
 */
public class GDPRComplianceReport {
    
    private String reportId;
    private String tenantId;
    private LocalDateTime reportDate;
    private String status;
    private Map<String, Object> dataRetention;
    private Map<String, Object> dataProcessing;
    private Map<String, Object> userRights;
    private List<String> violations;
    private String recommendations;
    
    public GDPRComplianceReport() {}
    
    public GDPRComplianceReport(String reportId, String tenantId, LocalDateTime reportDate, String status,
                               Map<String, Object> dataRetention, Map<String, Object> dataProcessing,
                               Map<String, Object> userRights, List<String> violations, String recommendations) {
        this.reportId = reportId;
        this.tenantId = tenantId;
        this.reportDate = reportDate;
        this.status = status;
        this.dataRetention = dataRetention;
        this.dataProcessing = dataProcessing;
        this.userRights = userRights;
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
    
    public Map<String, Object> getDataRetention() { return dataRetention; }
    public void setDataRetention(Map<String, Object> dataRetention) { this.dataRetention = dataRetention; }
    
    public Map<String, Object> getDataProcessing() { return dataProcessing; }
    public void setDataProcessing(Map<String, Object> dataProcessing) { this.dataProcessing = dataProcessing; }
    
    public Map<String, Object> getUserRights() { return userRights; }
    public void setUserRights(Map<String, Object> userRights) { this.userRights = userRights; }
    
    public List<String> getViolations() { return violations; }
    public void setViolations(List<String> violations) { this.violations = violations; }
    
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}
