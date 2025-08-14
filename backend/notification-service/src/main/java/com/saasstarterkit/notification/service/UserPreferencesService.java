package com.saasstarterkit.notification.service;

import com.saasstarterkit.notification.entity.UserNotificationPreferences;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user notification preferences
 */
public interface UserPreferencesService {
    
    /**
     * Get user preferences by user ID and tenant
     */
    UserNotificationPreferences getUserPreferences(String userId, String tenantId);
    
    /**
     * Get user preferences by user ID (cross-tenant)
     */
    List<UserNotificationPreferences> getUserPreferencesByUserId(String userId);
    
    /**
     * Save or update user preferences
     */
    UserNotificationPreferences savePreferences(UserNotificationPreferences preferences);
    
    /**
     * Update specific preference fields
     */
    UserNotificationPreferences updatePreferences(String userId, String tenantId, 
                                               UserNotificationPreferences updates);
    
    /**
     * Enable/disable a specific channel for a user
     */
    UserNotificationPreferences toggleChannel(String userId, String tenantId, 
                                           String channel, boolean enabled);
    
    /**
     * Set quiet hours for a user
     */
    UserNotificationPreferences setQuietHours(String userId, String tenantId, 
                                            String startTime, String endTime);
    
    /**
     * Set preferred language for a user
     */
    UserNotificationPreferences setPreferredLanguage(String userId, String tenantId, 
                                                   String language);
    
    /**
     * Set timezone for a user
     */
    UserNotificationPreferences setTimezone(String userId, String tenantId, String timezone);
    
    /**
     * Get users with specific channel enabled
     */
    List<String> getUsersWithChannelEnabled(String channel, String tenantId);
    
    /**
     * Get users who can receive notifications at a specific time
     */
    List<String> getUsersAvailableAtTime(String tenantId, String time);
    
    /**
     * Bulk update preferences for multiple users
     */
    void bulkUpdatePreferences(String tenantId, List<String> userIds, 
                             UserNotificationPreferences template);
    
    /**
     * Reset user preferences to defaults
     */
    UserNotificationPreferences resetToDefaults(String userId, String tenantId);
    
    /**
     * Check if user can receive notifications at current time
     */
    boolean canReceiveNotification(String userId, String tenantId);
    
    /**
     * Get notification digest settings for a user
     */
    UserNotificationPreferences.DigestFrequency getDigestFrequency(String userId, String tenantId);
}
