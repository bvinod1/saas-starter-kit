package com.saasstarterkit.notification.service;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import com.saasstarterkit.notification.service.channel.NotificationChannelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry service for managing notification channel providers
 * Routes notifications to appropriate providers based on user preferences
 */
@Service
public class ChannelProviderRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(ChannelProviderRegistry.class);
    
    private final Map<Notification.NotificationChannel, NotificationChannelProvider> providers = new ConcurrentHashMap<>();
    
    // =============================================
    // CONSTRUCTOR
    // =============================================
    
    @Autowired
    public ChannelProviderRegistry(List<NotificationChannelProvider> channelProviders) {
        // Register all available channel providers
        channelProviders.forEach(provider -> {
            Notification.NotificationChannel channelType = provider.getChannelType();
            providers.put(channelType, provider);
            logger.info("Registered channel provider: {} for channel: {}", 
                       provider.getClass().getSimpleName(), channelType);
        });
        
        logger.info("Channel provider registry initialized with {} providers", providers.size());
    }
    
    // =============================================
    // PUBLIC METHODS
    // =============================================
    
    /**
     * Get all available channel providers
     */
    public List<NotificationChannelProvider> getAllProviders() {
        return List.copyOf(providers.values());
    }
    
    /**
     * Get provider for a specific channel
     */
    public NotificationChannelProvider getProvider(Notification.NotificationChannel channel) {
        return providers.get(channel);
    }
    
    /**
     * Check if a channel is supported
     */
    public boolean isChannelSupported(Notification.NotificationChannel channel) {
        return providers.containsKey(channel);
    }
    
    /**
     * Get all supported channels
     */
    public List<Notification.NotificationChannel> getSupportedChannels() {
        return List.copyOf(providers.keySet());
    }
    
    /**
     * Get healthy providers only
     */
    public List<NotificationChannelProvider> getHealthyProviders() {
        return providers.values().stream()
                .filter(NotificationChannelProvider::isHealthy)
                .collect(Collectors.toList());
    }
    
    /**
     * Get provider configuration for all channels
     */
    public Map<Notification.NotificationChannel, NotificationChannelProvider.ChannelConfiguration> getAllConfigurations() {
        return providers.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getConfiguration()
                ));
    }
    
    /**
     * Get provider metadata for all channels
     */
    public Map<Notification.NotificationChannel, NotificationChannelProvider.ChannelMetadata> getAllMetadata() {
        return providers.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getMetadata()
                ));
    }
    
    /**
     * Find suitable providers for a notification event and user preferences
     */
    public List<NotificationChannelProvider> findSuitableProviders(NotificationEvent event, 
                                                                 UserNotificationPreferences preferences) {
        return providers.values().stream()
                .filter(provider -> provider.canHandle(event, preferences))
                .collect(Collectors.toList());
    }
    
    /**
     * Get the best provider for a notification (prioritized by channel type)
     */
    public NotificationChannelProvider getBestProvider(NotificationEvent event, 
                                                     UserNotificationPreferences preferences) {
        List<NotificationChannelProvider> suitableProviders = findSuitableProviders(event, preferences);
        
        if (suitableProviders.isEmpty()) {
            return null;
        }
        
        // Priority order: IN_APP > PUSH > SMS > EMAIL > WEBHOOK
        // This can be made configurable in the future
        return suitableProviders.stream()
                .min((p1, p2) -> {
                    int priority1 = getChannelPriority(p1.getChannelType());
                    int priority2 = getChannelPriority(p2.getChannelType());
                    return Integer.compare(priority1, priority2);
                })
                .orElse(suitableProviders.get(0));
    }
    
    /**
     * Send notification through multiple channels (multi-channel delivery)
     */
    public List<NotificationChannelProvider.DeliveryResult> sendToMultipleChannels(
            Notification notification, 
            UserNotificationPreferences preferences, 
            List<Notification.NotificationChannel> channels) {
        
        return channels.stream()
                .filter(providers::containsKey)
                .map(channel -> {
                    NotificationChannelProvider provider = providers.get(channel);
                    if (provider.canHandle(null, preferences)) {
                        try {
                            return provider.send(notification, preferences);
                        } catch (Exception e) {
                            logger.error("Error sending notification via channel: {}", channel, e);
                            return NotificationChannelProvider.DeliveryResult.failure()
                                    .errorMessage("Provider error: " + e.getMessage())
                                    .deliveryTimeMs(0)
                                    .build();
                        }
                    } else {
                        return NotificationChannelProvider.DeliveryResult.failure()
                                .errorMessage("Channel not supported or disabled: " + channel)
                                .deliveryTimeMs(0)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Send notification to all available channels for a user
     */
    public List<NotificationChannelProvider.DeliveryResult> sendToAllAvailableChannels(
            Notification notification, 
            UserNotificationPreferences preferences) {
        
        List<NotificationChannelProvider> availableProviders = findSuitableProviders(null, preferences);
        
        return availableProviders.stream()
                .map(provider -> {
                    try {
                        return provider.send(notification, preferences);
                    } catch (Exception e) {
                        logger.error("Error sending notification via provider: {}", 
                                   provider.getClass().getSimpleName(), e);
                        return NotificationChannelProvider.DeliveryResult.failure()
                                .errorMessage("Provider error: " + e.getMessage())
                                .deliveryTimeMs(0)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Check if any provider is healthy
     */
    public boolean hasHealthyProviders() {
        return providers.values().stream().anyMatch(NotificationChannelProvider::isHealthy);
    }
    
    /**
     * Get provider health status
     */
    public Map<Notification.NotificationChannel, Boolean> getProviderHealthStatus() {
        return providers.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().isHealthy()
                ));
    }
    
    // =============================================
    // PRIVATE METHODS
    // =============================================
    
    /**
     * Get channel priority for provider selection
     * Lower number = higher priority
     */
    private int getChannelPriority(Notification.NotificationChannel channel) {
        return switch (channel) {
            case IN_APP -> 1;    // Highest priority - instant delivery
            case PUSH -> 2;       // High priority - mobile push
            case SMS -> 3;        // Medium priority - reliable
            case EMAIL -> 4;      // Lower priority - slower but reliable
            case WEBHOOK -> 5;    // Lowest priority - external dependency
        };
    }
    
    /**
     * Validate provider registration
     */
    private void validateProvider(NotificationChannelProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        
        if (provider.getChannelType() == null) {
            throw new IllegalArgumentException("Provider channel type cannot be null");
        }
        
        if (providers.containsKey(provider.getChannelType())) {
            logger.warn("Provider for channel {} already exists, overwriting", provider.getChannelType());
        }
    }
}
