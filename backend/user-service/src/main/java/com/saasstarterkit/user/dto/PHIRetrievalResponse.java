package com.saasstarterkit.user.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for PHI (Protected Health Information) retrieval responses.
 */
public class PHIRetrievalResponse {
    
    private String requestId;
    private String tenantId;
    private String userId;
    private String phiType;
    private LocalDateTime responseDate;
    private String status;
    private Map<String, Object> phiData;
    private String auditTrail;
    private String errorMessage;
    
    public PHIRetrievalResponse() {}
    
    public PHIRetrievalResponse(String requestId, String tenantId, String userId, String phiType, 
                               LocalDateTime responseDate, String status, Map<String, Object> phiData, 
                               String auditTrail) {
        this.requestId = requestId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.phiType = phiType;
        this.responseDate = responseDate;
        this.status = status;
        this.phiData = phiData;
        this.auditTrail = auditTrail;
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
    
    public LocalDateTime getResponseDate() { return responseDate; }
    public void setResponseDate(LocalDateTime responseDate) { this.responseDate = responseDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Map<String, Object> getPhiData() { return phiData; }
    public void setPhiData(Map<String, Object> phiData) { this.phiData = phiData; }
    
    public String getAuditTrail() { return auditTrail; }
    public void setAuditTrail(String auditTrail) { this.auditTrail = auditTrail; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
