package com.saasstarterkit.notification.service;

import com.saasstarterkit.notification.entity.NotificationTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing notification templates
 */
public interface NotificationTemplateService {
    
    /**
     * Get a template by name and tenant
     */
    Optional<NotificationTemplate> getTemplateByName(String name, String tenantId);
    
    /**
     * Get all templates for a tenant
     */
    List<NotificationTemplate> getTemplatesByTenant(String tenantId);
    
    /**
     * Get all shared templates
     */
    List<NotificationTemplate> getSharedTemplates();
    
    /**
     * Get templates by category
     */
    List<NotificationTemplate> getTemplatesByCategory(String category, String tenantId);
    
    /**
     * Get templates by channel type
     */
    List<NotificationTemplate> getTemplatesByChannel(NotificationTemplate.NotificationChannel channel, String tenantId);
    
    /**
     * Save a template
     */
    NotificationTemplate saveTemplate(NotificationTemplate template);
    
    /**
     * Update a template
     */
    NotificationTemplate updateTemplate(NotificationTemplate template);
    
    /**
     * Delete a template
     */
    void deleteTemplate(Long templateId);
    
    /**
     * Activate/deactivate a template
     */
    NotificationTemplate toggleTemplateStatus(Long templateId, boolean active);
    
    /**
     * Validate template syntax
     */
    boolean validateTemplate(NotificationTemplate template);
    
    /**
     * Clone a template
     */
    NotificationTemplate cloneTemplate(Long templateId, String newName);
}
