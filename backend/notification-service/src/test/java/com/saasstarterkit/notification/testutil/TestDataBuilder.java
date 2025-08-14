package com.saasstarterkit.notification.testutil;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.NotificationTemplate;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Test data builder utility class for creating test notification data
 */
public class TestDataBuilder {
    
    // =============================================
    // NOTIFICATION EVENT BUILDERS
    // =============================================
    
    public static NotificationEvent.NotificationEventBuilder notificationEventBuilder() {
        return NotificationEvent.builder()
                .eventId("test-event-" + System.currentTimeMillis())
                .eventType("test_notification")
                .correlationId("test-correlation-" + System.currentTimeMillis())
                .userId("test-user-123")
                .tenantId("test-tenant-456")
                .organizationId("test-org-789")
                .templateName("test_template")
                .channel(Notification.NotificationChannel.EMAIL)
                .title("Test Notification Title")
                .message("This is a test notification message")
                .subject("Test Subject")
                .recipient("test@example.com")
                .priority(Notification.NotificationPriority.NORMAL)
                .category("test")
                .variables(Map.of("name", "Test User", "action", "test_action"))
                .scheduledAt(LocalDateTime.now().plusMinutes(5))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .sourceEvent("test_event")
                .immediate(true)
                .retry(false)
                .maxRetries(3);
    }
    
    public static NotificationEvent createEmailNotificationEvent() {
        return notificationEventBuilder()
                .channel(Notification.NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .build();
    }
    
    public static NotificationEvent createSmsNotificationEvent() {
        return notificationEventBuilder()
                .channel(Notification.NotificationChannel.SMS)
                .recipient("+1234567890")
                .build();
    }
    
    public static NotificationEvent createPushNotificationEvent() {
        return notificationEventBuilder()
                .channel(Notification.NotificationChannel.PUSH)
                .title("Push Notification")
                .message("This is a test push notification")
                .build();
    }
    
    public static NotificationEvent createInAppNotificationEvent() {
        return notificationEventBuilder()
                .channel(Notification.NotificationChannel.IN_APP)
                .title("In-App Notification")
                .message("This is a test in-app notification")
                .build();
    }
    
    public static NotificationEvent createWebhookNotificationEvent() {
        return notificationEventBuilder()
                .channel(Notification.NotificationChannel.WEBHOOK)
                .message("This is a test webhook notification")
                .build();
    }
    
    // =============================================
    // NOTIFICATION ENTITY BUILDERS
    // =============================================
    
    public static Notification.NotificationBuilder notificationBuilder() {
        return Notification.builder()
                .userId("test-user-123")
                .tenantId("test-tenant-456")
                .organizationId("test-org-789")
                .templateId(1L)
                .type(Notification.NotificationType.EMAIL)
                .title("Test Notification Title")
                .message("This is a test notification message")
                .subject("Test Subject")
                .recipient("test@example.com")
                .data("{\"key\": \"value\"}")
                .status(Notification.NotificationStatus.PENDING)
                .priority(Notification.NotificationPriority.NORMAL)
                .attemptCount(0)
                .maxAttempts(3)
                .scheduledAt(LocalDateTime.now().plusMinutes(5))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .sourceEvent("test_event")
                .correlationId("test-correlation-" + System.currentTimeMillis())
                .channelSpecificData("{\"email_headers\": {\"X-Test\": \"value\"}}");
    }
    
    public static Notification createEmailNotification() {
        return notificationBuilder()
                .type(Notification.NotificationType.EMAIL)
                .recipient("test@example.com")
                .build();
    }
    
    public static Notification createSmsNotification() {
        return notificationBuilder()
                .type(Notification.NotificationType.SMS)
                .recipient("+1234567890")
                .build();
    }
    
    public static Notification createPushNotification() {
        return notificationBuilder()
                .type(Notification.NotificationType.PUSH)
                .title("Push Notification")
                .message("This is a test push notification")
                .build();
    }
    
    public static Notification createInAppNotification() {
        return notificationBuilder()
                .type(Notification.NotificationType.IN_APP)
                .title("In-App Notification")
                .message("This is a test in-app notification")
                .build();
    }
    
    public static Notification createWebhookNotification() {
        return notificationBuilder()
                .type(Notification.NotificationType.WEBHOOK)
                .message("This is a test webhook notification")
                .build();
    }
    
    // =============================================
    // NOTIFICATION TEMPLATE BUILDERS
    // =============================================
    
    public static NotificationTemplate.NotificationTemplateBuilder notificationTemplateBuilder() {
        return NotificationTemplate.builder()
                .name("test_template")
                .type(Notification.NotificationType.EMAIL)
                .subject("Test Subject")
                .content("Hello {{name}}, this is a test notification about {{action}}.")
                .language("en")
                .variables("{\"name\": \"string\", \"action\": \"string\"}")
                .active(true)
                .tenantId("test-tenant-456")
                .category("test")
                .priority(Notification.NotificationPriority.NORMAL)
                .retryCount(3)
                .expiryHours(24);
    }
    
    public static NotificationTemplate createEmailTemplate() {
        return notificationTemplateBuilder()
                .type(Notification.NotificationType.EMAIL)
                .subject("Welcome {{name}}!")
                .content("Hello {{name}}, welcome to our platform!")
                .build();
    }
    
    public static NotificationTemplate createSmsTemplate() {
        return notificationTemplateBuilder()
                .type(Notification.NotificationType.SMS)
                .content("Hi {{name}}, your {{action}} is ready.")
                .build();
    }
    
    public static NotificationTemplate createPushTemplate() {
        return notificationTemplateBuilder()
                .type(Notification.NotificationType.PUSH)
                .title("{{title}}")
                .content("{{message}}")
                .build();
    }
    
    public static NotificationTemplate createInAppTemplate() {
        return notificationTemplateBuilder()
                .type(Notification.NotificationType.IN_APP)
                .title("{{title}}")
                .content("{{message}}")
                .build();
    }
    
    public static NotificationTemplate createWebhookTemplate() {
        return notificationTemplateBuilder()
                .type(Notification.NotificationType.WEBHOOK)
                .content("{\"message\": \"{{message}}\", \"timestamp\": \"{{timestamp}}\"}")
                .build();
    }
    
    // =============================================
    // USER NOTIFICATION PREFERENCES BUILDERS
    // =============================================
    
    public static UserNotificationPreferences.UserNotificationPreferencesBuilder userPreferencesBuilder() {
        return UserNotificationPreferences.builder()
                .userId("test-user-123")
                .tenantId("test-tenant-456")
                .organizationId("test-org-789")
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .inAppEnabled(true)
                .webhookEnabled(true)
                .quietHoursStart("22:00")
                .quietHoursEnd("06:00")
                .timezone("UTC")
                .preferredLanguage("en")
                .fallbackLanguage("en")
                .categoryPreferences("{\"marketing\": false, \"security\": true}")
                .digestFrequency(UserNotificationPreferences.DigestFrequency.DAILY)
                .maxNotificationsPerDay(100)
                .urgentNotificationsAlways(true)
                .channelSpecificPreferences("{\"email\": {\"html\": true}, \"sms\": {\"short\": true}}")
                .deviceTokens("[\"device_token_1\", \"device_token_2\"]")
                .webhookEndpoints("[\"https://webhook1.com\", \"https://webhook2.com\"]")
                .active(true);
    }
    
    public static UserNotificationPreferences createEmailOnlyPreferences() {
        return userPreferencesBuilder()
                .smsEnabled(false)
                .pushEnabled(false)
                .inAppEnabled(false)
                .webhookEnabled(false)
                .build();
    }
    
    public static UserNotificationPreferences createSmsOnlyPreferences() {
        return userPreferencesBuilder()
                .emailEnabled(false)
                .pushEnabled(false)
                .inAppEnabled(false)
                .webhookEnabled(false)
                .build();
    }
    
    public static UserNotificationPreferences createPushOnlyPreferences() {
        return userPreferencesBuilder()
                .emailEnabled(false)
                .smsEnabled(false)
                .inAppEnabled(false)
                .webhookEnabled(false)
                .build();
    }
    
    public static UserNotificationPreferences createInAppOnlyPreferences() {
        return userPreferencesBuilder()
                .emailEnabled(false)
                .smsEnabled(false)
                .pushEnabled(false)
                .webhookEnabled(false)
                .build();
    }
    
    public static UserNotificationPreferences createWebhookOnlyPreferences() {
        return userPreferencesBuilder()
                .emailEnabled(false)
                .smsEnabled(false)
                .pushEnabled(false)
                .inAppEnabled(false)
                .build();
    }
    
    public static UserNotificationPreferences createAllChannelsPreferences() {
        return userPreferencesBuilder().build();
    }
    
    public static UserNotificationPreferences createQuietHoursPreferences() {
        return userPreferencesBuilder()
                .quietHoursStart("22:00")
                .quietHoursEnd("06:00")
                .timezone("America/New_York")
                .build();
    }
    
    public static UserNotificationPreferences createMultiLanguagePreferences() {
        return userPreferencesBuilder()
                .preferredLanguage("es")
                .fallbackLanguage("en")
                .build();
    }
    
    // =============================================
    // TEST DATA HELPERS
    // =============================================
    
    public static List<NotificationEvent> createMultipleNotificationEvents(int count, Notification.NotificationChannel channel) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> notificationEventBuilder()
                        .eventId("test-event-" + i)
                        .channel(channel)
                        .build())
                .toList();
    }
    
    public static List<Notification> createMultipleNotifications(int count, Notification.NotificationType type) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> notificationBuilder()
                        .id((long) i)
                        .type(type)
                        .build())
                .toList();
    }
    
    public static Map<String, Object> createTestVariables() {
        return Map.of(
                "name", "Test User",
                "action", "test_action",
                "timestamp", System.currentTimeMillis(),
                "url", "https://example.com",
                "count", 42
        );
    }
    
    public static Map<String, Object> createEmailHeaders() {
        return Map.of(
                "X-Test-Header", "Test Value",
                "X-Priority", "High",
                "X-Category", "Test"
        );
    }
    
    public static List<String> createTestDeviceTokens() {
        return List.of(
                "device_token_1_android",
                "device_token_2_ios",
                "device_token_3_web"
        );
    }
    
    public static List<String> createTestWebhookEndpoints() {
        return List.of(
                "https://webhook1.example.com/notifications",
                "https://webhook2.example.com/events",
                "https://webhook3.example.com/alerts"
        );
    }
}
