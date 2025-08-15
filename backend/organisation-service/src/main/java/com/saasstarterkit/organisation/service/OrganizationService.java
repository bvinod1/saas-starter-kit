package com.saasstarterkit.organisation.service;

import com.saasstarterkit.organisation.domain.*;
import com.saasstarterkit.organisation.dto.*;
import com.saasstarterkit.organisation.repository.*;
import com.saasstarterkit.organisation.exception.OrganizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private OrganizationMemberRepository memberRepository;
    
    @Autowired
    private OrganizationAuditLogRepository auditLogRepository;

    // =============================================================================
    // ORGANIZATION ONBOARDING & PROVISIONING
    // =============================================================================

    /**
     * Create a new organization with automatic provisioning
     */
    public Organization createOrganization(OrganizationCreateRequest request, UUID createdByUserId) {
        // Validate request
        validateOrganizationCreateRequest(request);

        // Create organization
        Organization organization = new Organization(request.getName(), request.getSlug());
        organization.setDomain(request.getDomain());
        organization.setContactEmail(request.getContactEmail());
        organization.setContactPhone(request.getContactPhone());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setWebsiteUrl(request.getWebsiteUrl());
        organization.setTier(OrganizationTier.fromName(request.getTier()));
        organization.setStatus(OrganizationStatus.ONBOARDING);
        organization.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        organization.setTrialEndsAt(LocalDateTime.now().plusDays(14)); // Default 14-day trial

        // Set tier-specific limits
        setTierLimits(organization, organization.getTier());

        // Save the organization
        organization = organizationRepository.save(organization);
        
        // Create audit log
        createAuditLog(organization, AuditAction.ORGANIZATION_CREATED, "Organization created", createdByUserId);
        
        return organization;
    }

    /**
     * Complete organization onboarding
     */
    public Organization completeOnboarding(UUID organizationId, OnboardingCompleteRequest request, UUID completedByUserId) {
        // TODO: Implement organization retrieval and onboarding completion
        Organization organization = new Organization("Temp", "temp");
        organization.setOnboardingCompleted(true);
        organization.setOnboardingStep("COMPLETED");
        organization.setStatus(OrganizationStatus.ACTIVE);
        return organization;
    }

    // =============================================================================
    // ORGANIZATION LIFECYCLE MANAGEMENT
    // =============================================================================

    /**
     * Update organization status
     */
    public Organization updateOrganizationStatus(UUID organizationId, OrganizationStatus newStatus, String reason, UUID updatedByUserId) {
        // TODO: Implement status update logic
        Organization organization = new Organization("Temp", "temp");
        organization.setStatus(newStatus);
        return organization;
    }

    /**
     * Upgrade/downgrade organization tier
     */
    public Organization changeOrganizationTier(UUID organizationId, OrganizationTier newTier, UUID changedByUserId) {
        // TODO: Implement tier change logic
        Organization organization = new Organization("Temp", "temp");
        organization.setTier(newTier);
        setTierLimits(organization, newTier);
        return organization;
    }

    // =============================================================================
    // ORGANIZATION MEMBER MANAGEMENT
    // =============================================================================

    /**
     * Add member to organization
     */
    public OrganizationMember addMember(UUID organizationId, MemberAddRequest request, UUID addedByUserId) {
        // TODO: Implement member addition logic
        Organization organization = new Organization("Temp", "temp");
        OrganizationMember member = new OrganizationMember(request.getUserId(), organization, 
            MemberRole.fromName(request.getRole()));
        member.setInvitedBy(addedByUserId);
        member.setDepartment(request.getDepartment());
        member.setJobTitle(request.getJobTitle());
        member.setAccessLevel(request.getAccessLevel());
        return member;
    }

    // =============================================================================
    // QUERY METHODS
    // =============================================================================

    @Cacheable(value = "organizations", key = "#organizationId")
    public Organization getOrganizationById(UUID organizationId) {
        return organizationRepository.findById(organizationId)
            .orElseThrow(() -> new OrganizationException("Organization not found: " + organizationId));
    }

    @Cacheable(value = "organizations", key = "#slug")
    public Organization getOrganizationBySlug(String slug) {
        return organizationRepository.findBySlug(slug)
            .orElseThrow(() -> new OrganizationException("Organization not found: " + slug));
    }

    public List<Organization> getOrganizationsByStatus(OrganizationStatus status) {
        return organizationRepository.findByStatus(status);
    }

    public List<Organization> getOrganizationsByTier(OrganizationTier tier) {
        return organizationRepository.findByTier(tier);
    }

    public List<Organization> getOrganizationsNeedingAttention() {
        LocalDateTime now = LocalDateTime.now();
        List<Organization> attentionNeeded = new ArrayList<>();
        
        // Add organizations with expired trials
        attentionNeeded.addAll(organizationRepository.findOrganizationsWithExpiredTrial(now));
        
        // Add organizations still in onboarding
        attentionNeeded.addAll(organizationRepository.findByOnboardingCompleted(false));
        
        return attentionNeeded;
    }

    // =============================================================================
    // PRIVATE HELPER METHODS
    // =============================================================================

    private void validateOrganizationCreateRequest(OrganizationCreateRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new OrganizationException("Organization name is required");
        }
        if (!StringUtils.hasText(request.getSlug())) {
            throw new OrganizationException("Organization slug is required");
        }
        if (request.getSlug().length() < 2 || request.getSlug().length() > 100) {
            throw new OrganizationException("Organization slug must be between 2 and 100 characters");
        }
    }

    private void setTierLimits(Organization organization, OrganizationTier tier) {
        organization.setMaxUsers(tier.getMaxUsers());
        organization.setStorageLimitGB(tier.getStorageLimitGB());
        organization.setApiRateLimit(tier.getApiRateLimit());
    }

    // =============================================================================
    // CACHE MANAGEMENT
    // =============================================================================

    @CacheEvict(value = "organizations", key = "#organizationId")
    public void evictOrganizationCache(UUID organizationId) {
        // Method to evict organization cache
    }

    @CacheEvict(value = "organizations", allEntries = true)
    public void evictAllOrganizationsCache() {
        // Method to evict all organization caches
    }
    
    // =============================================================================
    // AUDIT LOGGING
    // =============================================================================
    
    private void createAuditLog(Organization organization, AuditAction action, String description, UUID userId) {
        OrganizationAuditLog auditLog = new OrganizationAuditLog();
        auditLog.setOrganization(organization);
        auditLog.setAction(action);
        auditLog.setDetails(description);
        auditLog.setUserId(userId);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLog.setIpAddress("127.0.0.1"); // TODO: Get from request context
        auditLog.setUserAgent("System"); // TODO: Get from request context
        
        auditLogRepository.save(auditLog);
    }
}
