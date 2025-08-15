package com.saasstarterkit.user.controller;

import com.saasstarterkit.user.service.ComplianceService;
import com.saasstarterkit.user.dto.*;
import com.saasstarterkit.user.domain.model.*;
import com.saasstarterkit.user.exception.ComplianceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for GDPR and HIPAA compliance operations
 */
@RestController
@RequestMapping("/api/v1/compliance")
@CrossOrigin(origins = "*")
public class ComplianceController {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplianceController.class);
    
    @Autowired
    private ComplianceService complianceService;
    
    // =============================================
    // GDPR COMPLIANCE ENDPOINTS
    // =============================================
    
    /**
     * Record user consent for data processing
     */
    @PostMapping("/consent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<DataConsent> recordConsent(
            @Valid @RequestBody ConsentRequest request,
            @RequestHeader("X-Tenant") String tenantId) {
        
        try {
            logger.info("Recording consent for user: {}, tenant: {}", request.getUserId(), tenantId);
            
            // Set tenant ID from header
            request.setTenantId(tenantId);
            
            DataConsent consent = complianceService.recordConsent(request);
            return ResponseEntity.ok(consent);
            
        } catch (ComplianceException e) {
            logger.error("Failed to record consent: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error recording consent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Withdraw user consent
     */
    @DeleteMapping("/consent/{userId}/{consentType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> withdrawConsent(
            @PathVariable UUID userId,
            @PathVariable ConsentType consentType,
            @RequestParam String reason) {
        
        try {
            logger.info("Withdrawing consent for user: {}, type: {}", userId, consentType);
            
            complianceService.withdrawConsent(userId, consentType, reason);
            return ResponseEntity.ok().build();
            
        } catch (ComplianceException e) {
            logger.error("Failed to withdraw consent: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error withdrawing consent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get user consent status
     */
    @GetMapping("/consent/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<DataConsent>> getUserConsents(@PathVariable UUID userId) {
        try {
            logger.info("Getting consents for user: {}", userId);
            
            // This would need to be implemented in the service
            // List<DataConsent> consents = complianceService.getUserConsents(userId);
            // return ResponseEntity.ok(consents);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Unexpected error getting user consents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Submit data subject rights request
     */
    @PostMapping("/data-subject-request")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<DataSubjectRequest> submitDataSubjectRequest(
            @Valid @RequestBody DataSubjectRequestDto request,
            @RequestHeader("X-Tenant") String tenantId) {
        
        try {
            logger.info("Submitting data subject request for user: {}, tenant: {}", request.getUserId(), tenantId);
            
            // Set tenant ID from header
            request.setTenantId(tenantId);
            
            DataSubjectRequest dsRequest = complianceService.processDataSubjectRequest(request);
            return ResponseEntity.ok(dsRequest);
            
        } catch (ComplianceException e) {
            logger.error("Failed to submit data subject request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error submitting data subject request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Complete data subject rights request (admin only)
     */
    @PutMapping("/data-subject-request/{requestId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> completeDataSubjectRequest(
            @PathVariable UUID requestId,
            @RequestBody Map<String, Object> responseData) {
        
        try {
            logger.info("Completing data subject request: {}", requestId);
            
            complianceService.completeDataSubjectRequest(requestId, responseData);
            return ResponseEntity.ok().build();
            
        } catch (ComplianceException e) {
            logger.error("Failed to complete data subject request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error completing data subject request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get data subject requests for tenant (admin only)
     */
    @GetMapping("/data-subject-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DataSubjectRequest>> getDataSubjectRequests(
            @RequestHeader("X-Tenant") String tenantId,
            @RequestParam(required = false) DataSubjectRequestStatus status,
            @RequestParam(required = false) DataSubjectRequestType type) {
        
        try {
            logger.info("Getting data subject requests for tenant: {}", tenantId);
            
            // This would need to be implemented in the service
            // List<DataSubjectRequest> requests = complianceService.getDataSubjectRequests(tenantId, status, type);
            // return ResponseEntity.ok(requests);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Unexpected error getting data subject requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =============================================
    // HIPAA COMPLIANCE ENDPOINTS
    // =============================================
    
    /**
     * Store Protected Health Information (PHI)
     */
    @PostMapping("/phi")
    @PreAuthorize("hasRole('HEALTHCARE_PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<ProtectedHealthInformation> storePHI(
            @Valid @RequestBody PHIStorageRequest request,
            @RequestHeader("X-Tenant") String tenantId) {
        
        try {
            logger.info("Storing PHI for user: {}, tenant: {}", request.getUserId(), tenantId);
            
            // Set tenant ID from header
            request.setTenantId(tenantId);
            
            ProtectedHealthInformation phi = complianceService.storePHI(request);
            return ResponseEntity.ok(phi);
            
        } catch (ComplianceException e) {
            logger.error("Failed to store PHI: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error storing PHI", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Retrieve PHI with proper access controls
     */
    @GetMapping("/phi/{userId}")
    @PreAuthorize("hasRole('HEALTHCARE_PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<PHIRetrievalResponse> retrievePHI(
            @PathVariable UUID userId,
            @RequestParam PHICategory phiCategory,
            @RequestParam String accessPurpose) {
        
        try {
            logger.info("Retrieving PHI for user: {}, category: {}", userId, phiCategory);
            
            PHIRetrievalRequest request = new PHIRetrievalRequest();
            request.setUserId(userId);
            request.setPhiCategory(phiCategory);
            request.setAccessPurpose(accessPurpose);
            
            PHIRetrievalResponse response = complianceService.retrievePHI(request);
            return ResponseEntity.ok(response);
            
        } catch (ComplianceException e) {
            logger.error("Failed to retrieve PHI: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error retrieving PHI", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete PHI (right to be forgotten)
     */
    @DeleteMapping("/phi/{userId}")
    @PreAuthorize("hasRole('HEALTHCARE_PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePHI(
            @PathVariable UUID userId,
            @RequestParam PHICategory phiCategory,
            @RequestParam String reason) {
        
        try {
            logger.info("Deleting PHI for user: {}, category: {}", userId, phiCategory);
            
            complianceService.deletePHI(userId, phiCategory, reason);
            return ResponseEntity.ok().build();
            
        } catch (ComplianceException e) {
            logger.error("Failed to delete PHI: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error deleting PHI", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get PHI summary for tenant (admin only)
     */
    @GetMapping("/phi/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPHISummary(@RequestHeader("X-Tenant") String tenantId) {
        try {
            logger.info("Getting PHI summary for tenant: {}", tenantId);
            
            // This would need to be implemented in the service
            // Map<String, Object> summary = complianceService.getPHISummary(tenantId);
            // return ResponseEntity.ok(summary);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Unexpected error getting PHI summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =============================================
    // COMPLIANCE REPORTING ENDPOINTS
    // =============================================
    
    /**
     * Generate GDPR compliance report
     */
    @GetMapping("/reports/gdpr")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<GDPRComplianceReport> generateGDPRReport(
            @RequestHeader("X-Tenant") String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        try {
            logger.info("Generating GDPR compliance report for tenant: {}, from: {}, to: {}", 
                tenantId, fromDate, toDate);
            
            GDPRComplianceReport report = complianceService.generateGDPRReport(tenantId, fromDate, toDate);
            return ResponseEntity.ok(report);
            
        } catch (ComplianceException e) {
            logger.error("Failed to generate GDPR report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error generating GDPR report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Generate HIPAA compliance report
     */
    @GetMapping("/reports/hipaa")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<HIPAAComplianceReport> generateHIPAAReport(
            @RequestHeader("X-Tenant") String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        try {
            logger.info("Generating HIPAA compliance report for tenant: {}, from: {}, to: {}", 
                tenantId, fromDate, toDate);
            
            HIPAAComplianceReport report = complianceService.generateHIPAAReport(tenantId, fromDate, toDate);
            return ResponseEntity.ok(report);
            
        } catch (ComplianceException e) {
            logger.error("Failed to generate HIPAA report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error generating HIPAA report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =============================================
    // COMPLIANCE STATUS ENDPOINTS
    // =============================================
    
    /**
     * Get compliance status for tenant
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<Map<String, Object>> getComplianceStatus(@RequestHeader("X-Tenant") String tenantId) {
        try {
            logger.info("Getting compliance status for tenant: {}", tenantId);
            
            // This would need to be implemented in the service
            // Map<String, Object> status = complianceService.getComplianceStatus(tenantId);
            // return ResponseEntity.ok(status);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Unexpected error getting compliance status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get user compliance status
     */
    @GetMapping("/status/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserComplianceStatus(@PathVariable UUID userId) {
        try {
            logger.info("Getting compliance status for user: {}", userId);
            
            // This would need to be implemented in the service
            // Map<String, Object> status = complianceService.getUserComplianceStatus(userId);
            // return ResponseEntity.ok(status);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Unexpected error getting user compliance status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =============================================
    // HEALTH CHECK ENDPOINT
    // =============================================
    
    /**
     * Health check for compliance service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "Compliance Service",
            "timestamp", LocalDateTime.now(),
            "version", "1.0.0"
        );
        
        return ResponseEntity.ok(health);
    }
    
    // =============================================
    // ERROR HANDLING
    // =============================================
    
    @ExceptionHandler(ComplianceException.class)
    public ResponseEntity<Map<String, String>> handleComplianceException(ComplianceException e) {
        logger.error("Compliance exception: {}", e.getMessage());
        
        Map<String, String> error = Map.of(
            "error", "Compliance Error",
            "message", e.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        logger.error("Unexpected error in compliance controller", e);
        
        Map<String, String> error = Map.of(
            "error", "Internal Server Error",
            "message", "An unexpected error occurred",
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
