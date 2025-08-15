package com.saasstarterkit.notification.domain.enums;

/**
 * NotificationChannel - Defines the different notification channels
 * 
 * This enum represents the various channels through which notifications
 * can be delivered to users.
 */
public enum NotificationChannel {
    
    /**
     * Email notification channel
     */
    EMAIL("email"),
    
    /**
     * SMS notification channel
     */
    SMS("sms"),
    
    /**
     * Push notification channel
     */
    PUSH("push"),
    
    /**
     * In-app notification channel
     */
    IN_APP("in_app"),
    
    /**
     * Webhook notification channel
     */
    WEBHOOK("webhook"),
    
    /**
     * Slack notification channel
     */
    SLACK("slack"),
    
    /**
     * Microsoft Teams notification channel
     */
    TEAMS("teams"),
    
    /**
     * Discord notification channel
     */
    DISCORD("discord"),
    
    /**
     * WhatsApp notification channel
     */
    WHATSAPP("whatsapp"),
    
    /**
     * Telegram notification channel
     */
    TELEGRAM("telegram"),
    
    /**
     * Voice call notification channel
     */
    VOICE("voice"),
    
    /**
     * Fax notification channel
     */
    FAX("fax"),
    
    /**
     * Letter notification channel
     */
    LETTER("letter"),
    
    /**
     * Mobile app notification channel
     */
    MOBILE_APP("mobile_app"),
    
    /**
     * Desktop app notification channel
     */
    DESKTOP_APP("desktop_app"),
    
    /**
     * Browser notification channel
     */
    BROWSER("browser"),
    
    /**
     * Social media notification channel
     */
    SOCIAL_MEDIA("social_media"),
    
    /**
     * Custom notification channel
     */
    CUSTOM("custom");
    
    private final String value;
    
    NotificationChannel(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static NotificationChannel fromValue(String value) {
        for (NotificationChannel channel : values()) {
            if (channel.value.equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown NotificationChannel: " + value);
    }
    
    /**
     * Check if this channel is digital
     */
    public boolean isDigital() {
        return this == EMAIL || this == SMS || this == PUSH || this == IN_APP ||
               this == WEBHOOK || this == SLACK || this == TEAMS || this == DISCORD ||
               this == WHATSAPP || this == TELEGRAM || this == MOBILE_APP ||
               this == DESKTOP_APP || this == BROWSER || this == SOCIAL_MEDIA;
    }
    
    /**
     * Check if this channel is real-time
     */
    public boolean isRealTime() {
        return this == PUSH || this == IN_APP || this == WEBHOOK || this == SLACK ||
               this == TEAMS || this == DISCORD || this == WHATSAPP || this == TELEGRAM ||
               this == MOBILE_APP || this == DESKTOP_APP || this == BROWSER;
    }
    
    /**
     * Check if this channel requires internet connectivity
     */
    public boolean requiresInternet() {
        return this == EMAIL || this == WEBHOOK || this == SLACK || this == TEAMS ||
               this == DISCORD || this == WHATSAPP || this == TELEGRAM ||
               this == MOBILE_APP || this == DESKTOP_APP || this == BROWSER ||
               this == SOCIAL_MEDIA;
    }
    
    /**
     * Check if this channel supports rich content
     */
    public boolean supportsRichContent() {
        return this == EMAIL || this == IN_APP || this == WEBHOOK || this == SLACK ||
               this == TEAMS || this == DISCORD || this == MOBILE_APP ||
               this == DESKTOP_APP || this == BROWSER;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
