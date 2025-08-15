package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for tenant compliance reports.
 */
public class TenantComplianceReport {
    
    private String reportId;
    private String tenantId;
    private String reportType;
    private LocalDateTime reportDate;
    private String status;
    private Map<String, Object> complianceData;
    private List<String> violations;
    private List<String> recommendations;
    private String generatedBy;
    private LocalDateTime nextReviewDate;
    
    public TenantComplianceReport() {}
    
    public TenantComplianceReport(String reportId, String tenantId, String reportType, LocalDateTime reportDate,
                                 String status, Map<String, Object> complianceData, List<String> violations,
                                 List<String> recommendations, String generatedBy, LocalDateTime nextReviewDate) {
        this.reportId = reportId;
        this.tenantId = tenantId;
        this.reportType = reportType;
        this.reportDate = reportDate;
        this.status = status;
        this.complianceData = complianceData;
        this.violations = violations;
        this.recommendations = recommendations;
        this.generatedBy = generatedBy;
        this.nextReviewDate = nextReviewDate;
    }
    
    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Map<String, Object> getComplianceData() { return complianceData; }
    public void setComplianceData(Map<String, Object> complianceData) { this.complianceData = complianceData; }
    
    public List<String> getViolations() { return violations; }
    public void setViolations(List<String> violations) { this.violations = violations; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    
    public LocalDateTime getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(LocalDateTime nextReviewDate) { this.nextReviewDate = nextReviewDate; }
}
