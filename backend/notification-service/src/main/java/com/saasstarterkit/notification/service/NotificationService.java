package com.saasstarterkit.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.NotificationTemplate;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import com.saasstarterkit.notification.repository.NotificationRepository;
import com.saasstarterkit.notification.exception.TemplateProcessingException;
import com.saasstarterkit.notification.service.channel.NotificationChannelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core notification service that orchestrates the entire notification process
 * Consumes events from RabbitMQ, processes templates, and delivers via appropriate channels
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private List<NotificationChannelProvider> channelProviders;
    
    @Autowired
    private TemplatingService templatingService;
    
    @Autowired
    private NotificationTemplateService templateService;
    
    @Autowired
    private UserPreferencesService userPreferencesService;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // =============================================
    // EVENT CONSUMPTION
    // =============================================
    
    /**
     * Consume notification events from RabbitMQ
     */
    @RabbitListener(queues = "notification.queue")
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            logger.info("Processing notification event: {} for user: {}", 
                       event.getEventType(), event.getUserId());
            
            // Validate the event
            if (!isValidEvent(event)) {
                logger.warn("Invalid notification event received: {}", event);
                return;
            }
            
            // Get user preferences
            UserNotificationPreferences preferences = userPreferencesService
                    .getUserPreferences(event.getUserId(), event.getTenantId());
            
            if (preferences == null) {
                logger.warn("No user preferences found for user: {}", event.getUserId());
                return;
            }
            
            // Process the notification
            processNotificationEvent(event, preferences);
            
        } catch (Exception e) {
            logger.error("Error processing notification event: {}", event.getEventId(), e);
            // In a production system, you might want to send to dead letter queue
        }
    }
    
    /**
     * Process a notification event and create notifications for enabled channels
     */
    private void processNotificationEvent(NotificationEvent event, 
                                        UserNotificationPreferences preferences) {
        
        // Get or create notification template
        NotificationTemplate template = getOrCreateTemplate(event);
        
        // Process template with variables
        TemplatingService.ProcessedTemplate processedTemplate = processTemplate(event, template, preferences);
        
        // Determine which channels to use
        List<NotificationChannelProvider> enabledProviders = getEnabledChannelProviders(event, preferences);
        
        if (enabledProviders.isEmpty()) {
            logger.warn("No enabled channels found for user: {}", event.getUserId());
            return;
        }
        
        // Create and send notifications for each enabled channel
        for (NotificationChannelProvider provider : enabledProviders) {
            try {
                createAndSendNotification(event, processedTemplate, provider, preferences);
            } catch (Exception e) {
                logger.error("Failed to send notification via {} for user: {}", 
                           provider.getChannelType(), event.getUserId(), e);
            }
        }
    }
    
    /**
     * Create and send a notification via a specific channel
     */
    private void createAndSendNotification(NotificationEvent event,
                                         TemplatingService.ProcessedTemplate processedTemplate,
                                         NotificationChannelProvider provider,
                                         UserNotificationPreferences preferences) {
        
        // Create notification entity
        Notification notification = createNotificationEntity(event, processedTemplate, provider);
        
        // Save to database
        notification = notificationRepository.save(notification);
        
        // Send via channel provider
        NotificationChannelProvider.DeliveryResult result = provider.send(notification, preferences);
        
        // Update notification status based on result
        updateNotificationStatus(notification, result);
        
        // Log the result
        if (result.isSuccess()) {
            logger.info("Notification sent successfully via {} to user: {}", 
                       provider.getChannelType(), event.getUserId());
        } else {
            logger.error("Notification failed via {} to user: {}: {}", 
                        provider.getChannelType(), event.getUserId(), result.getErrorMessage());
        }
    }
    
    // =============================================
    // TEMPLATE PROCESSING
    // =============================================
    
    /**
     * Get or create a notification template
     */
    private NotificationTemplate getOrCreateTemplate(NotificationEvent event) {
        if (event.getTemplateName() != null) {
            Optional<NotificationTemplate> existing = templateService
                    .getTemplateByName(event.getTemplateName(), event.getTenantId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        
        // Create a default template if none exists
        return createDefaultTemplate(event);
    }
    
    /**
     * Process template with event variables
     */
    private TemplatingService.ProcessedTemplate processTemplate(NotificationEvent event,
                                                               NotificationTemplate template,
                                                               UserNotificationPreferences preferences) {
        try {
            return templatingService.processTemplate(template, event.getVariables(), preferences);
        } catch (TemplateProcessingException e) {
            logger.error("Template processing failed for template: {}", template.getName(), e);
            throw e;
        }
    }
    
    /**
     * Create a default template for the event
     */
    private NotificationTemplate createDefaultTemplate(NotificationEvent event) {
        NotificationTemplate template = new NotificationTemplate();
        template.setName("default_" + event.getEventType());
        template.setType(convertToTemplateChannel(event.getChannel()));
        template.setContent(event.getMessage());
        template.setSubject(event.getSubject());
        template.setCategory(event.getCategory());
        template.setTenantId(event.getTenantId());
        template.setIsActive(true);
        
        return templateService.saveTemplate(template);
    }
    
    // =============================================
    // CHANNEL MANAGEMENT
    // =============================================
    
    /**
     * Get enabled channel providers for the event
     */
    private List<NotificationChannelProvider> getEnabledChannelProviders(NotificationEvent event,
                                                                       UserNotificationPreferences preferences) {
        return channelProviders.stream()
                .filter(provider -> provider.canHandle(event, preferences))
                .filter(NotificationChannelProvider::isHealthy)
                .toList();
    }
    
    /**
     * Convert notification channel to template channel
     */
    private NotificationTemplate.NotificationChannel convertToTemplateChannel(com.saasstarterkit.notification.domain.enums.NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return NotificationTemplate.NotificationChannel.EMAIL;
            case SMS:
                return NotificationTemplate.NotificationChannel.SMS;
            case PUSH:
                return NotificationTemplate.NotificationChannel.PUSH;
            case IN_APP:
                return NotificationTemplate.NotificationChannel.IN_APP;
            case WEBHOOK:
                return NotificationTemplate.NotificationChannel.WEBHOOK;
            default:
                return NotificationTemplate.NotificationChannel.EMAIL;
        }
    }
    
    /**
     * Convert domain notification channel to entity notification channel
     */
    private Notification.NotificationChannel convertToEntityChannel(com.saasstarterkit.notification.domain.enums.NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return Notification.NotificationChannel.EMAIL;
            case SMS:
                return Notification.NotificationChannel.SMS;
            case PUSH:
                return Notification.NotificationChannel.PUSH;
            case IN_APP:
                return Notification.NotificationChannel.IN_APP;
            case WEBHOOK:
                return Notification.NotificationChannel.WEBHOOK;
            default:
                return Notification.NotificationChannel.EMAIL;
        }
    }
    
    /**
     * Convert domain notification priority to entity notification priority
     */
    private Notification.NotificationPriority convertToEntityPriority(com.saasstarterkit.notification.domain.enums.NotificationPriority priority) {
        switch (priority) {
            case LOW:
                return Notification.NotificationPriority.LOW;
            case NORMAL:
                return Notification.NotificationPriority.NORMAL;
            case HIGH:
                return Notification.NotificationPriority.HIGH;
            case URGENT:
                return Notification.NotificationPriority.URGENT;
            default:
                return Notification.NotificationPriority.NORMAL;
        }
    }
    
    // =============================================
    // NOTIFICATION ENTITY CREATION
    // =============================================
    
    /**
     * Create a notification entity from the event and processed template
     */
    private Notification createNotificationEntity(NotificationEvent event,
                                                TemplatingService.ProcessedTemplate processedTemplate,
                                                NotificationChannelProvider provider) {
        
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setTenantId(event.getTenantId());
        notification.setOrganizationId(event.getOrganizationId());
        notification.setType(convertToEntityChannel(provider.getChannelType()));
        notification.setTitle(processedTemplate.getContent());
        notification.setMessage(processedTemplate.getContent());
        notification.setSubject(processedTemplate.getSubject());
        notification.setRecipient(event.getRecipient());
        notification.setPriority(event.getPriority() != null ? convertToEntityPriority(event.getPriority()) : Notification.NotificationPriority.NORMAL);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setMaxAttempts(event.getMaxRetries() != null ? event.getMaxRetries() : 3);
        notification.setSourceEvent(event.getSourceEvent());
        notification.setCorrelationId(event.getCorrelationId());
        
        // Set scheduling if specified
        if (event.getScheduledAt() != null) {
            notification.setScheduledAt(event.getScheduledAt());
            notification.setStatus(Notification.NotificationStatus.SCHEDULED);
        }
        
        // Set expiry if specified
        if (event.getExpiresAt() != null) {
            notification.setExpiresAt(event.getExpiresAt());
        }
        
        // Add metadata
        try {
            Map<String, Object> metadata = Map.of(
                "eventId", event.getEventId(),
                "eventType", event.getEventType(),
                "provider", provider.getMetadata().getProviderName(),
                "language", processedTemplate.getLanguage()
            );
            notification.setData(objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize notification metadata", e);
        }
        
        return notification;
    }
    
    // =============================================
    // STATUS UPDATES
    // =============================================
    
    /**
     * Update notification status based on delivery result
     */
    private void updateNotificationStatus(Notification notification, 
                                        NotificationChannelProvider.DeliveryResult result) {
        if (result.isSuccess()) {
            notification.markAsSent();
            notification.setProviderResponse(result.getProviderResponse());
        } else {
            notification.markAsFailed(result.getErrorMessage());
            notification.setProviderResponse(result.getProviderResponse());
        }
        
        notificationRepository.save(notification);
    }
    
    // =============================================
    // VALIDATION
    // =============================================
    
    /**
     * Validate that the notification event is properly formatted
     */
    private boolean isValidEvent(NotificationEvent event) {
        return event != null &&
               event.getUserId() != null &&
               event.getChannel() != null &&
               event.getMessage() != null;
    }
    
    // =============================================
    // DIRECT NOTIFICATION SENDING (SYNCHRONOUS)
    // =============================================
    
    /**
     * Send a notification directly (synchronous)
     * Useful for immediate, critical notifications
     */
    @Transactional
    public Notification sendNotificationDirectly(NotificationEvent event) {
        logger.info("Sending notification directly for user: {}", event.getUserId());
        
        // Get user preferences
        UserNotificationPreferences preferences = userPreferencesService
                .getUserPreferences(event.getUserId(), event.getTenantId());
        
        if (preferences == null) {
            throw new IllegalArgumentException("No user preferences found for user: " + event.getUserId());
        }
        
        // Process the notification
        processNotificationEvent(event, preferences);
        
        // Return the created notification (we'll need to modify the method to return it)
        // For now, return null as we need to refactor the method
        return null;
    }
}
