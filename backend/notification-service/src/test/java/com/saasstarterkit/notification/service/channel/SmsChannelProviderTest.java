package com.saasstarterkit.notification.service.channel;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import com.saasstarterkit.notification.testutil.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SmsChannelProvider
 */
@ExtendWith(MockitoExtension.class)
class SmsChannelProviderTest {
    
    @InjectMocks
    private SmsChannelProvider smsChannelProvider;
    
    private Notification testNotification;
    private UserNotificationPreferences testPreferences;
    private NotificationEvent testEvent;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testNotification = TestDataBuilder.createSmsNotification();
        testPreferences = TestDataBuilder.createSmsOnlyPreferences();
        testEvent = TestDataBuilder.createSmsNotificationEvent();
        
        // Set up configuration values
        ReflectionTestUtils.setField(smsChannelProvider, "twilioEnabled", true);
        ReflectionTestUtils.setField(smsChannelProvider, "twilioAccountSid", "test_account_sid");
        ReflectionTestUtils.setField(smsChannelProvider, "twilioAuthToken", "test_auth_token");
        ReflectionTestUtils.setField(smsChannelProvider, "twilioFromNumber", "+1234567890");
        ReflectionTestUtils.setField(smsChannelProvider, "maxMessageLength", 160);
        ReflectionTestUtils.setField(smsChannelProvider, "rateLimitPerMinute", 10);
    }
    
    // =============================================
    // CHANNEL TYPE TESTS
    // =============================================
    
    @Test
    void getChannelType_ShouldReturnSms() {
        assertEquals(Notification.NotificationChannel.SMS, smsChannelProvider.getChannelType());
    }
    
    // =============================================
    // CAN HANDLE TESTS
    // =============================================
    
    @Test
    void canHandle_WithValidSmsPreferences_ShouldReturnTrue() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createSmsOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createSmsNotificationEvent();
        
        // When
        boolean result = smsChannelProvider.canHandle(event, preferences);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canHandle_WithSmsDisabled_ShouldReturnFalse() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createSmsOnlyPreferences();
        preferences.setSmsEnabled(false);
        NotificationEvent event = TestDataBuilder.createSmsNotificationEvent();
        
        // When
        boolean result = smsChannelProvider.canHandle(event, preferences);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithNullPreferences_ShouldReturnFalse() {
        // Given
        NotificationEvent event = TestDataBuilder.createSmsNotificationEvent();
        
        // When
        boolean result = smsChannelProvider.canHandle(event, null);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithEmptyRecipient_ShouldReturnFalse() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createSmsOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createSmsNotificationEvent();
        event.setRecipient("");
        
        // When
        boolean result = smsChannelProvider.canHandle(event, preferences);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithInvalidPhoneNumber_ShouldReturnFalse() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createSmsOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createSmsNotificationEvent();
        event.setRecipient("invalid-phone");
        
        // When
        boolean result = smsChannelProvider.canHandle(event, preferences);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithValidPhoneNumber_ShouldReturnTrue() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createSmsOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createSmsNotificationEvent();
        event.setRecipient("+1234567890");
        
        // When
        boolean result = smsChannelProvider.canHandle(event, preferences);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canHandle_WithInternationalPhoneNumber_ShouldReturnTrue() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createSmsOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createSmsNotificationEvent();
        event.setRecipient("+44123456789");
        
        // When
        boolean result = smsChannelProvider.canHandle(event, preferences);
        
        // Then
        assertTrue(result);
    }
    
    // =============================================
    // VALIDATION TESTS
    // =============================================
    
    @Test
    void validate_WithValidNotification_ShouldReturnValid() {
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void validate_WithSmsDisabled_ShouldReturnInvalid() {
        // Given
        testPreferences.setSmsEnabled(false);
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("SMS notifications are disabled for this user", result.getErrorMessage());
    }
    
    @Test
    void validate_WithEmptyRecipient_ShouldReturnInvalid() {
        // Given
        testNotification.setRecipient("");
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("SMS recipient is required", result.getErrorMessage());
    }
    
    @Test
    void validate_WithInvalidPhoneNumber_ShouldReturnInvalid() {
        // Given
        testNotification.setRecipient("invalid-phone");
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Invalid phone number format"));
    }
    
    @Test
    void validate_WithEmptyMessage_ShouldReturnInvalid() {
        // Given
        testNotification.setMessage("");
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("SMS message content is required", result.getErrorMessage());
    }
    
    @Test
    void validate_WithMessageTooLong_ShouldReturnInvalid() {
        // Given
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longMessage.append("a");
        }
        testNotification.setMessage(longMessage.toString());
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("SMS message too long"));
    }
    
    @Test
    void validate_WithMessageWithinLimit_ShouldReturnValid() {
        // Given
        testNotification.setMessage("This is a short message within the 160 character limit.");
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isValid());
    }
    
    // =============================================
    // SEND TESTS
    // =============================================
    
    @Test
    void send_WithValidNotification_ShouldReturnSuccess() throws Exception {
        // When
        DeliveryResult result = smsChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessageId());
        assertTrue(result.getMessageId().startsWith("twilio_"));
        assertTrue(result.getDeliveryTimeMs() > 0);
        assertEquals("Twilio SMS delivery successful", result.getProviderResponse());
    }
    
    @Test
    void send_WithInvalidNotification_ShouldReturnFailure() {
        // Given
        testNotification.setRecipient("invalid-phone");
        
        // When
        DeliveryResult result = smsChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Invalid phone number format"));
    }
    
    @Test
    void send_WithTwilioDisabled_ShouldReturnFailure() {
        // Given
        ReflectionTestUtils.setField(smsChannelProvider, "twilioEnabled", false);
        
        // When
        DeliveryResult result = smsChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("SMS provider not configured", result.getErrorMessage());
    }
    
    @Test
    void send_WithTwilioNotConfigured_ShouldReturnFailure() {
        // Given
        ReflectionTestUtils.setField(smsChannelProvider, "twilioAccountSid", "");
        
        // When
        DeliveryResult result = smsChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("SMS provider not configured", result.getErrorMessage());
    }
    
    // =============================================
    // HEALTH CHECK TESTS
    // =============================================
    
    @Test
    void isHealthy_WithTwilioEnabledAndConfigured_ShouldReturnTrue() {
        // When
        boolean result = smsChannelProvider.isHealthy();
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void isHealthy_WithTwilioDisabled_ShouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(smsChannelProvider, "twilioEnabled", false);
        
        // When
        boolean result = smsChannelProvider.isHealthy();
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void isHealthy_WithTwilioNotConfigured_ShouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(smsChannelProvider, "twilioAccountSid", "");
        
        // When
        boolean result = smsChannelProvider.isHealthy();
        
        // Then
        assertFalse(result);
    }
    
    // =============================================
    // CONFIGURATION TESTS
    // =============================================
    
    @Test
    void getConfiguration_ShouldReturnValidConfiguration() {
        // When
        ChannelConfiguration config = smsChannelProvider.getConfiguration();
        
        // Then
        assertNotNull(config);
        assertEquals("Twilio", config.getProviderName());
        assertEquals("1.0.0", config.getVersion());
        assertTrue(config.isEnabled());
        assertEquals(3, config.getMaxRetries());
        assertEquals(1000, config.getRetryDelayMs());
        assertEquals(10, config.getRateLimitPerMinute());
    }
    
    @Test
    void getConfiguration_WithTwilioDisabled_ShouldReturnDisabled() {
        // Given
        ReflectionTestUtils.setField(smsChannelProvider, "twilioEnabled", false);
        
        // When
        ChannelConfiguration config = smsChannelProvider.getConfiguration();
        
        // Then
        assertFalse(config.isEnabled());
    }
    
    // =============================================
    // METADATA TESTS
    // =============================================
    
    @Test
    void getMetadata_ShouldReturnValidMetadata() {
        // When
        ChannelMetadata metadata = smsChannelProvider.getMetadata();
        
        // Then
        assertNotNull(metadata);
        assertEquals("Twilio", metadata.getProviderName());
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("Global", metadata.getRegion());
        assertTrue(metadata.isSupportsBulk());
        assertTrue(metadata.isSupportsScheduling());
        assertTrue(metadata.isSupportsTracking());
        assertEquals("text", metadata.getSupportedFormats());
        assertEquals(160, metadata.getMaxMessageSize());
    }
    
    // =============================================
    // DELIVERY TIME TESTS
    // =============================================
    
    @Test
    void getEstimatedDeliveryTimeMs_ShouldReturnReasonableValue() {
        // When
        long deliveryTime = smsChannelProvider.getEstimatedDeliveryTimeMs();
        
        // Then
        assertEquals(30000, deliveryTime); // 30 seconds
    }
    
    // =============================================
    // PHONE NUMBER VALIDATION TESTS
    // =============================================
    
    @Test
    void isValidPhoneNumber_WithValidE164Format_ShouldReturnTrue() {
        // Test various valid E.164 formats
        String[] validNumbers = {
            "+1234567890",      // US
            "+44123456789",     // UK
            "+61412345678",     // Australia
            "+81901234567",     // Japan
            "+123456789012345"  // Long number
        };
        
        for (String number : validNumbers) {
            assertTrue(isValidPhoneNumber(number), "Should be valid: " + number);
        }
    }
    
    @Test
    void isValidPhoneNumber_WithInvalidFormat_ShouldReturnFalse() {
        // Test various invalid formats
        String[] invalidNumbers = {
            "1234567890",       // Missing +
            "+123",             // Too short
            "+1234567890123456", // Too long
            "+abc123",          // Contains letters
            "+12-345-6789",     // Contains hyphens
            "+12 345 6789",     // Contains spaces
            "",                 // Empty
            null                // Null
        };
        
        for (String number : invalidNumbers) {
            assertFalse(isValidPhoneNumber(number), "Should be invalid: " + number);
        }
    }
    
    // =============================================
    // MESSAGE LENGTH TESTS
    // =============================================
    
    @Test
    void validate_WithMessageExactlyAtLimit_ShouldReturnValid() {
        // Given
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < 160; i++) {
            message.append("a");
        }
        testNotification.setMessage(message.toString());
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isValid());
    }
    
    @Test
    void validate_WithMessageOneOverLimit_ShouldReturnInvalid() {
        // Given
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < 161; i++) {
            message.append("a");
        }
        testNotification.setMessage(message.toString());
        
        // When
        ValidationResult result = smsChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("SMS message too long"));
    }
    
    // =============================================
    // EDGE CASES
    // =============================================
    
    @Test
    void send_WithNullNotification_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            smsChannelProvider.send(null, testPreferences);
        });
    }
    
    @Test
    void send_WithNullPreferences_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            smsChannelProvider.send(testNotification, null);
        });
    }
    
    @Test
    void validate_WithNullNotification_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            smsChannelProvider.validate(null, testPreferences);
        });
    }
    
    @Test
    void validate_WithNullPreferences_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            smsChannelProvider.validate(testNotification, null);
        });
    }
    
    // =============================================
    // INTEGRATION TESTS
    // =============================================
    
    @Test
    void send_WithSpecialCharacters_ShouldHandleGracefully() throws Exception {
        // Given
        testNotification.setMessage("Message with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?");
        
        // When
        DeliveryResult result = smsChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isSuccess());
    }
    
    @Test
    void send_WithUnicodeCharacters_ShouldHandleGracefully() throws Exception {
        // Given
        testNotification.setMessage("Message with unicode: 🚀📱💬✨");
        
        // When
        DeliveryResult result = smsChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isSuccess());
    }
    
    // =============================================
    // HELPER METHODS
    // =============================================
    
    /**
     * Helper method to test phone number validation
     * This uses reflection to access the private method
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        try {
            return (Boolean) ReflectionTestUtils.invokeMethod(smsChannelProvider, "isValidPhoneNumber", phoneNumber);
        } catch (Exception e) {
            return false;
        }
    }
}
