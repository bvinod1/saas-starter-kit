package com.saasstarterkit.notification.exception;

/**
 * Exception thrown when there's an error processing notification templates
 */
public class TemplateProcessingException extends RuntimeException {
    
    public TemplateProcessingException(String message) {
        super(message);
    }
    
    public TemplateProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
