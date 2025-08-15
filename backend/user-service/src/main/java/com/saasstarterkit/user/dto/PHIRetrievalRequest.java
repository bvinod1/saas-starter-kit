package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;

/**
 * DTO for PHI (Protected Health Information) retrieval requests.
 */
public class PHIRetrievalRequest {
    
    private String requestId;
    private String tenantId;
    private String userId;
    private String phiType;
    private LocalDateTime requestDate;
    private String purpose;
    private String requesterId;
    private String requesterRole;
    
    public PHIRetrievalRequest() {}
    
    public PHIRetrievalRequest(String requestId, String tenantId, String userId, String phiType, 
                              LocalDateTime requestDate, String purpose, String requesterId, String requesterRole) {
        this.requestId = requestId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.phiType = phiType;
        this.requestDate = requestDate;
        this.purpose = purpose;
        this.requesterId = requesterId;
        this.requesterRole = requesterRole;
    }
    
    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getPhiType() { return phiType; }
    public void setPhiType(String phiType) { this.phiType = phiType; }
    
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
    
    public String getRequesterRole() { return requesterRole; }
    public void setRequesterRole(String requesterRole) { this.requesterRole = requesterRole; }
    
    // Additional methods needed by ComplianceService
    public String getPhiCategory() {
        return phiType; // Using existing phiType field
    }
    
    public String getAccessPurpose() {
        return purpose; // Using existing purpose field
    }
}
