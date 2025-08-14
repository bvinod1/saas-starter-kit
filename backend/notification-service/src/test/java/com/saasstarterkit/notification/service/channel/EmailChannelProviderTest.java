package com.saasstarterkit.notification.service.channel;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import com.saasstarterkit.notification.testutil.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailChannelProvider
 */
@ExtendWith(MockitoExtension.class)
class EmailChannelProviderTest {
    
    @Mock
    private JavaMailSender javaMailSender;
    
    @Mock
    private MimeMessage mimeMessage;
    
    @InjectMocks
    private EmailChannelProvider emailChannelProvider;
    
    private Notification testNotification;
    private UserNotificationPreferences testPreferences;
    private NotificationEvent testEvent;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testNotification = TestDataBuilder.createEmailNotification();
        testPreferences = TestDataBuilder.createEmailOnlyPreferences();
        testEvent = TestDataBuilder.createEmailNotificationEvent();
        
        // Set up mock responses
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        
        // Set up configuration values
        ReflectionTestUtils.setField(emailChannelProvider, "smtpEnabled", true);
        ReflectionTestUtils.setField(emailChannelProvider, "smtpFromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailChannelProvider, "smtpFromName", "Test App");
    }
    
    // =============================================
    // CHANNEL TYPE TESTS
    // =============================================
    
    @Test
    void getChannelType_ShouldReturnEmail() {
        assertEquals(Notification.NotificationChannel.EMAIL, emailChannelProvider.getChannelType());
    }
    
    // =============================================
    // CAN HANDLE TESTS
    // =============================================
    
    @Test
    void canHandle_WithValidEmailPreferences_ShouldReturnTrue() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createEmailOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        
        // When
        boolean result = emailChannelProvider.canHandle(event, preferences);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void canHandle_WithEmailDisabled_ShouldReturnFalse() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createEmailOnlyPreferences();
        preferences.setEmailEnabled(false);
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        
        // When
        boolean result = emailChannelProvider.canHandle(event, preferences);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithNullPreferences_ShouldReturnFalse() {
        // Given
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        
        // When
        boolean result = emailChannelProvider.canHandle(event, null);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithEmptyRecipient_ShouldReturnFalse() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createEmailOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        event.setRecipient("");
        
        // When
        boolean result = emailChannelProvider.canHandle(event, preferences);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithInvalidEmailFormat_ShouldReturnFalse() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createEmailOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        event.setRecipient("invalid-email");
        
        // When
        boolean result = emailChannelProvider.canHandle(event, preferences);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void canHandle_WithValidEmailFormat_ShouldReturnTrue() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createEmailOnlyPreferences();
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        event.setRecipient("valid.email@example.com");
        
        // When
        boolean result = emailChannelProvider.canHandle(event, preferences);
        
        // Then
        assertTrue(result);
    }
    
    // =============================================
    // VALIDATION TESTS
    // =============================================
    
    @Test
    void validate_WithValidNotification_ShouldReturnValid() {
        // When
        ValidationResult result = emailChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void validate_WithEmailDisabled_ShouldReturnInvalid() {
        // Given
        testPreferences.setEmailEnabled(false);
        
        // When
        ValidationResult result = emailChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("Email notifications are disabled for this user", result.getErrorMessage());
    }
    
    @Test
    void validate_WithEmptyRecipient_ShouldReturnInvalid() {
        // Given
        testNotification.setRecipient("");
        
        // When
        ValidationResult result = emailChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("Email recipient is required", result.getErrorMessage());
    }
    
    @Test
    void validate_WithInvalidEmailFormat_ShouldReturnInvalid() {
        // Given
        testNotification.setRecipient("invalid-email");
        
        // When
        ValidationResult result = emailChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Invalid email format"));
    }
    
    @Test
    void validate_WithEmptyMessage_ShouldReturnInvalid() {
        // Given
        testNotification.setMessage("");
        
        // When
        ValidationResult result = emailChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("Email message content is required", result.getErrorMessage());
    }
    
    @Test
    void validate_WithEmptySubject_ShouldReturnInvalid() {
        // Given
        testNotification.setSubject("");
        
        // When
        ValidationResult result = emailChannelProvider.validate(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isValid());
        assertEquals("Email subject is required", result.getErrorMessage());
    }
    
    // =============================================
    // SEND TESTS
    // =============================================
    
    @Test
    void send_WithValidNotification_ShouldReturnSuccess() throws Exception {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        
        // When
        DeliveryResult result = emailChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessageId());
        assertTrue(result.getDeliveryTimeMs() > 0);
        assertEquals("Email sent successfully via SMTP", result.getProviderResponse());
        
        // Verify email was sent
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
    
    @Test
    void send_WithInvalidNotification_ShouldReturnFailure() {
        // Given
        testNotification.setRecipient("invalid-email");
        
        // When
        DeliveryResult result = emailChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Invalid email format"));
    }
    
    @Test
    void send_WithSmtpDisabled_ShouldReturnFailure() {
        // Given
        ReflectionTestUtils.setField(emailChannelProvider, "smtpEnabled", false);
        
        // When
        DeliveryResult result = emailChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("SMTP email provider not configured or disabled", result.getErrorMessage());
    }
    
    @Test
    void send_WithSmtpException_ShouldReturnFailure() throws Exception {
        // Given
        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));
        
        // When
        DeliveryResult result = emailChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Failed to send email"));
        assertTrue(result.getErrorMessage().contains("SMTP error"));
    }
    
    // =============================================
    // HEALTH CHECK TESTS
    // =============================================
    
    @Test
    void isHealthy_WithSmtpEnabled_ShouldReturnTrue() {
        // Given
        ReflectionTestUtils.setField(emailChannelProvider, "smtpEnabled", true);
        
        // When
        boolean result = emailChannelProvider.isHealthy();
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void isHealthy_WithSmtpDisabled_ShouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(emailChannelProvider, "smtpEnabled", false);
        
        // When
        boolean result = emailChannelProvider.isHealthy();
        
        // Then
        assertFalse(result);
    }
    
    // =============================================
    // CONFIGURATION TESTS
    // =============================================
    
    @Test
    void getConfiguration_ShouldReturnValidConfiguration() {
        // When
        ChannelConfiguration config = emailChannelProvider.getConfiguration();
        
        // Then
        assertNotNull(config);
        assertEquals("SMTP", config.getProviderName());
        assertEquals("1.0.0", config.getVersion());
        assertTrue(config.isEnabled());
        assertEquals(3, config.getMaxRetries());
        assertEquals(1000, config.getRetryDelayMs());
        assertEquals(100, config.getRateLimitPerMinute());
    }
    
    // =============================================
    // METADATA TESTS
    // =============================================
    
    @Test
    void getMetadata_ShouldReturnValidMetadata() {
        // When
        ChannelMetadata metadata = emailChannelProvider.getMetadata();
        
        // Then
        assertNotNull(metadata);
        assertEquals("SMTP", metadata.getProviderName());
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("Local", metadata.getRegion());
        assertTrue(metadata.isSupportsBulk());
        assertTrue(metadata.isSupportsScheduling());
        assertTrue(metadata.isSupportsTracking());
        assertEquals("text,html", metadata.getSupportedFormats());
    }
    
    // =============================================
    // DELIVERY TIME TESTS
    // =============================================
    
    @Test
    void getEstimatedDeliveryTimeMs_ShouldReturnReasonableValue() {
        // When
        long deliveryTime = emailChannelProvider.getEstimatedDeliveryTimeMs();
        
        // Then
        assertTrue(deliveryTime > 0);
        assertTrue(deliveryTime <= 300000); // Should be reasonable (5 minutes max)
    }
    
    // =============================================
    // EDGE CASES
    // =============================================
    
    @Test
    void send_WithNullNotification_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            emailChannelProvider.send(null, testPreferences);
        });
    }
    
    @Test
    void send_WithNullPreferences_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            emailChannelProvider.send(testNotification, null);
        });
    }
    
    @Test
    void validate_WithNullNotification_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            emailChannelProvider.validate(null, testPreferences);
        });
    }
    
    @Test
    void validate_WithNullPreferences_ShouldHandleGracefully() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            emailChannelProvider.validate(testNotification, null);
        });
    }
    
    // =============================================
    // INTEGRATION TESTS
    // =============================================
    
    @Test
    void send_WithLongMessage_ShouldHandleGracefully() throws Exception {
        // Given
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long message. ");
        }
        testNotification.setMessage(longMessage.toString());
        
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        
        // When
        DeliveryResult result = emailChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
    
    @Test
    void send_WithSpecialCharacters_ShouldHandleGracefully() throws Exception {
        // Given
        testNotification.setSubject("Test Subject with Special Characters: !@#$%^&*()");
        testNotification.setMessage("Message with special chars: <>&\"'");
        
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        
        // When
        DeliveryResult result = emailChannelProvider.send(testNotification, testPreferences);
        
        // Then
        assertTrue(result.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}
