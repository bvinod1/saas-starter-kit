package com.saasstarterkit.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasstarterkit.notification.entity.NotificationTemplate;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import com.saasstarterkit.notification.exception.TemplateProcessingException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Service for processing notification templates with dynamic data
 * Supports multiple template engines and language localization
 */
@Service
public class TemplatingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplatingService.class);
    
    @Autowired
    private Configuration freemarkerConfig;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // =============================================
    // TEMPLATE PROCESSING
    // =============================================
    
    /**
     * Process a notification template with the given variables
     */
    public ProcessedTemplate processTemplate(NotificationTemplate template, 
                                           Map<String, Object> variables,
                                           UserNotificationPreferences preferences) {
        try {
            // Determine the language to use
            String language = determineLanguage(template, preferences);
            
            // Get localized content
            String content = getLocalizedContent(template, language);
            String subject = getLocalizedSubject(template, language);
            
            // Merge variables with common data
            Map<String, Object> mergedVariables = mergeWithCommonVariables(variables, preferences);
            
            // Process content with variables
            String processedContent = processContent(content, mergedVariables);
            String processedSubject = processContent(subject, mergedVariables);
            
            return ProcessedTemplate.builder()
                    .content(processedContent)
                    .subject(processedSubject)
                    .language(language)
                    .variables(mergedVariables)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error processing template: {}", template.getName(), e);
            throw new TemplateProcessingException("Failed to process template: " + template.getName(), e);
        }
    }
    
    /**
     * Process content using FreeMarker template engine
     */
    private String processContent(String content, Map<String, Object> variables) 
            throws IOException, TemplateException {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        
        Template template = new Template("content", content, freemarkerConfig);
        StringWriter writer = new StringWriter();
        template.process(variables, writer);
        return writer.toString();
    }
    
    /**
     * Determine the language to use for this template
     */
    private String determineLanguage(NotificationTemplate template, UserNotificationPreferences preferences) {
        // If template has a specific language, use it
        if (StringUtils.hasText(template.getLanguage())) {
            return template.getLanguage();
        }
        
        // Otherwise use user's preferred language
        if (preferences != null && StringUtils.hasText(preferences.getPreferredLanguage())) {
            return preferences.getPreferredLanguage();
        }
        
        // Fallback to default
        return "en";
    }
    
    /**
     * Get localized content for the specified language
     */
    private String getLocalizedContent(NotificationTemplate template, String language) {
        // For now, we'll use the template's content directly
        // In a more sophisticated implementation, you might have multiple language versions
        return template.getContent();
    }
    
    /**
     * Get localized subject for the specified language
     */
    private String getLocalizedSubject(NotificationTemplate template, String language) {
        // For now, we'll use the template's subject directly
        // In a more sophisticated implementation, you might have multiple language versions
        return template.getSubject();
    }
    
    /**
     * Merge user variables with common system variables
     */
    private Map<String, Object> mergeWithCommonVariables(Map<String, Object> userVariables, 
                                                        UserNotificationPreferences preferences) {
        Map<String, Object> merged = new HashMap<>();
        
        // Add common system variables
        merged.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        merged.put("date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        merged.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        
        // Add user preferences if available
        if (preferences != null) {
            merged.put("userTimezone", preferences.getTimezone());
            merged.put("userLanguage", preferences.getPreferredLanguage());
        }
        
        // Add user variables (these override system variables)
        if (userVariables != null) {
            merged.putAll(userVariables);
        }
        
        return merged;
    }
    
    // =============================================
    // TEMPLATE VALIDATION
    // =============================================
    
    /**
     * Validate that a template can be processed with the given variables
     */
    public boolean validateTemplate(NotificationTemplate template, Map<String, Object> variables) {
        try {
            String content = template.getContent();
            if (!StringUtils.hasText(content)) {
                return false;
            }
            
            // Try to process the template with empty variables to check syntax
            Map<String, Object> emptyVars = new HashMap<>();
            processContent(content, emptyVars);
            
            return true;
        } catch (Exception e) {
            logger.warn("Template validation failed for template: {}", template.getName(), e);
            return false;
        }
    }
    
    /**
     * Extract required variables from a template
     */
    @Cacheable("template-variables")
    public Map<String, String> extractRequiredVariables(NotificationTemplate template) {
        Map<String, String> requiredVars = new HashMap<>();
        
        try {
            if (StringUtils.hasText(template.getVariables())) {
                Map<String, Object> varDefs = objectMapper.readValue(
                    template.getVariables(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                
                for (Map.Entry<String, Object> entry : varDefs.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> varInfo = (Map<String, Object>) entry.getValue();
                        String description = (String) varInfo.get("description");
                        requiredVars.put(entry.getKey(), description != null ? description : "No description");
                    } else {
                        requiredVars.put(entry.getKey(), "No description");
                    }
                }
            }
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse template variables for template: {}", template.getName(), e);
        }
        
        return requiredVars;
    }
    
    // =============================================
    // INNER CLASSES
    // =============================================
    
    /**
     * Result of template processing
     */
    public static class ProcessedTemplate {
        private final String content;
        private final String subject;
        private final String language;
        private final Map<String, Object> variables;
        
        private ProcessedTemplate(Builder builder) {
            this.content = builder.content;
            this.subject = builder.subject;
            this.language = builder.language;
            this.variables = builder.variables;
        }
        
        public String getContent() { return content; }
        public String getSubject() { return subject; }
        public String getLanguage() { return language; }
        public Map<String, Object> getVariables() { return variables; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String content;
            private String subject;
            private String language;
            private Map<String, Object> variables;
            
            public Builder content(String content) {
                this.content = content;
                return this;
            }
            
            public Builder subject(String subject) {
                this.subject = subject;
                return this;
            }
            
            public Builder language(String language) {
                this.language = language;
                return this;
            }
            
            public Builder variables(Map<String, Object> variables) {
                this.variables = variables;
                return this;
            }
            
            public ProcessedTemplate build() {
                return new ProcessedTemplate(this);
            }
        }
    }
}
