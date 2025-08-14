package com.saasstarterkit.notification.test;

import com.saasstarterkit.notification.service.channel.*;
import com.saasstarterkit.notification.service.ChannelProviderRegistry;
import com.saasstarterkit.notification.service.ChannelProviderFactory;
import com.saasstarterkit.notification.service.NotificationHealthService;
import com.saasstarterkit.notification.service.TemplatingService;
import com.saasstarterkit.notification.integration.NotificationServiceIntegrationTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for the notification service
 * Runs all unit tests, integration tests, and component tests
 */
@Suite
@SuiteDisplayName("Notification Service Test Suite")
@SelectClasses({
    // Channel Provider Tests
    EmailChannelProviderTest.class,
    SmsChannelProviderTest.class,
    PushChannelProviderTest.class,
    InAppChannelProviderTest.class,
    WebhookChannelProviderTest.class,
    
    // Service Tests
    ChannelProviderRegistryTest.class,
    ChannelProviderFactoryTest.class,
    NotificationHealthServiceTest.class,
    TemplatingServiceTest.class,
    
    // Integration Tests
    NotificationServiceIntegrationTest.class
})
public class NotificationServiceTestSuite {
    
    /**
     * Test categories covered by this suite:
     * 
     * 1. Channel Provider Tests
     *    - Email (SMTP/SendGrid)
     *    - SMS (Twilio)
     *    - Push (Firebase)
     *    - In-App (WebSocket)
     *    - Webhook (HTTP)
     * 
     * 2. Service Tests
     *    - Provider registry and factory
     *    - Health monitoring
     *    - Template processing
     * 
     * 3. Integration Tests
     *    - End-to-end notification flow
     *    - Database integration
     *    - Message queue integration
     *    - Multi-channel delivery
     * 
     * 4. Performance Tests
     *    - Provider lookup performance
     *    - Multi-channel delivery performance
     *    - Health check performance
     * 
     * 5. Edge Cases
     *    - Invalid configurations
     *    - Service failures
     *    - Network timeouts
     *    - Invalid data handling
     */
}
