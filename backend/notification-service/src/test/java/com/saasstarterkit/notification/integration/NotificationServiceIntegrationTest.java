package com.saasstarterkit.notification.integration;

import com.saasstarterkit.notification.dto.NotificationEvent;
import com.saasstarterkit.notification.entity.Notification;
import com.saasstarterkit.notification.entity.NotificationTemplate;
import com.saasstarterkit.notification.entity.UserNotificationPreferences;
import com.saasstarterkit.notification.service.NotificationService;
import com.saasstarterkit.notification.service.channel.ChannelProviderRegistry;
import com.saasstarterkit.notification.testutil.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the notification service
 * Uses TestContainers for PostgreSQL and RabbitMQ
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class NotificationServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");
    
    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.12-management-alpine")
            .withUser("test_user", "test_password")
            .withVhost("test_vhost");
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ChannelProviderRegistry channelProviderRegistry;
    
    // =============================================
    // DYNAMIC PROPERTY SOURCE
    // =============================================
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
        registry.add("spring.rabbitmq.virtual-host", () -> "test_vhost");
    }
    
    // =============================================
    // SETUP
    // =============================================
    
    @BeforeEach
    void setUp() {
        // Wait for containers to be ready
        assertTrue(postgres.isRunning());
        assertTrue(rabbitmq.isRunning());
        
        // Wait a bit for services to be fully ready
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // =============================================
    // CHANNEL PROVIDER REGISTRY TESTS
    // =============================================
    
    @Test
    void channelProviderRegistry_ShouldHaveAllProviders() {
        // When
        List<Notification.NotificationChannel> supportedChannels = channelProviderRegistry.getSupportedChannels();
        
        // Then
        assertNotNull(supportedChannels);
        assertTrue(supportedChannels.size() >= 5); // Email, SMS, Push, In-App, Webhook
        
        // Verify all expected channels are present
        assertTrue(supportedChannels.contains(Notification.NotificationChannel.EMAIL));
        assertTrue(supportedChannels.contains(Notification.NotificationChannel.SMS));
        assertTrue(supportedChannels.contains(Notification.NotificationChannel.PUSH));
        assertTrue(supportedChannels.contains(Notification.NotificationChannel.IN_APP));
        assertTrue(supportedChannels.contains(Notification.NotificationChannel.WEBHOOK));
    }
    
    @Test
    void channelProviderRegistry_ShouldHaveHealthyProviders() {
        // When
        List<NotificationChannelProvider> healthyProviders = channelProviderRegistry.getHealthyProviders();
        
        // Then
        assertNotNull(healthyProviders);
        assertTrue(healthyProviders.size() > 0);
        
        // Verify at least some providers are healthy
        boolean hasHealthyProvider = healthyProviders.stream()
                .anyMatch(NotificationChannelProvider::isHealthy);
        assertTrue(hasHealthyProvider);
    }
    
    @Test
    void channelProviderRegistry_ShouldGetProviderForChannel() {
        // When
        NotificationChannelProvider emailProvider = channelProviderRegistry.getProvider(Notification.NotificationChannel.EMAIL);
        NotificationChannelProvider smsProvider = channelProviderRegistry.getProvider(Notification.NotificationChannel.SMS);
        
        // Then
        assertNotNull(emailProvider);
        assertNotNull(smsProvider);
        assertEquals(Notification.NotificationChannel.EMAIL, emailProvider.getChannelType());
        assertEquals(Notification.NotificationChannel.SMS, smsProvider.getChannelType());
    }
    
    // =============================================
    // PROVIDER CONFIGURATION TESTS
    // =============================================
    
    @Test
    void providers_ShouldHaveValidConfigurations() {
        // When
        var configurations = channelProviderRegistry.getAllConfigurations();
        
        // Then
        assertNotNull(configurations);
        assertTrue(configurations.size() >= 5);
        
        // Verify each provider has valid configuration
        configurations.values().forEach(config -> {
            assertNotNull(config.getProviderName());
            assertNotNull(config.getVersion());
            assertTrue(config.getMaxRetries() > 0);
            assertTrue(config.getRetryDelayMs() > 0);
            assertTrue(config.getRateLimitPerMinute() > 0);
        });
    }
    
    @Test
    void providers_ShouldHaveValidMetadata() {
        // When
        var metadata = channelProviderRegistry.getAllMetadata();
        
        // Then
        assertNotNull(metadata);
        assertTrue(metadata.size() >= 5);
        
        // Verify each provider has valid metadata
        metadata.values().forEach(meta -> {
            assertNotNull(meta.getProviderName());
            assertNotNull(meta.getVersion());
            assertNotNull(meta.getRegion());
            assertNotNull(meta.getSupportedFormats());
            assertTrue(meta.getMaxMessageSize() > 0);
        });
    }
    
    // =============================================
    // PROVIDER SELECTION TESTS
    // =============================================
    
    @Test
    void providerSelection_ShouldFindSuitableProviders() {
        // Given
        NotificationEvent emailEvent = TestDataBuilder.createEmailNotificationEvent();
        UserNotificationPreferences emailPreferences = TestDataBuilder.createEmailOnlyPreferences();
        
        // When
        List<NotificationChannelProvider> suitableProviders = channelProviderRegistry.findSuitableProviders(emailEvent, emailPreferences);
        
        // Then
        assertNotNull(suitableProviders);
        assertTrue(suitableProviders.size() > 0);
        
        // Should find email provider
        boolean hasEmailProvider = suitableProviders.stream()
                .anyMatch(p -> p.getChannelType() == Notification.NotificationChannel.EMAIL);
        assertTrue(hasEmailProvider);
    }
    
    @Test
    void providerSelection_ShouldGetBestProvider() {
        // Given
        NotificationEvent emailEvent = TestDataBuilder.createEmailNotificationEvent();
        UserNotificationPreferences emailPreferences = TestDataBuilder.createEmailOnlyPreferences();
        
        // When
        NotificationChannelProvider bestProvider = channelProviderRegistry.getBestProvider(emailEvent, emailPreferences);
        
        // Then
        assertNotNull(bestProvider);
        assertEquals(Notification.NotificationChannel.EMAIL, bestProvider.getChannelType());
    }
    
    @Test
    void providerSelection_WithMultipleChannels_ShouldFindMultipleProviders() {
        // Given
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        UserNotificationPreferences preferences = TestDataBuilder.createAllChannelsPreferences();
        
        // When
        List<NotificationChannelProvider> suitableProviders = channelProviderRegistry.findSuitableProviders(event, preferences);
        
        // Then
        assertNotNull(suitableProviders);
        assertTrue(suitableProviders.size() >= 2); // Should find multiple providers
    }
    
    // =============================================
    // MULTI-CHANNEL DELIVERY TESTS
    // =============================================
    
    @Test
    void multiChannelDelivery_ShouldSendToMultipleChannels() {
        // Given
        Notification notification = TestDataBuilder.createEmailNotification();
        UserNotificationPreferences preferences = TestDataBuilder.createAllChannelsPreferences();
        List<Notification.NotificationChannel> channels = List.of(
            Notification.NotificationChannel.EMAIL,
            Notification.NotificationChannel.IN_APP
        );
        
        // When
        List<NotificationChannelProvider.DeliveryResult> results = channelProviderRegistry.sendToMultipleChannels(
            notification, preferences, channels
        );
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // At least one should be successful
        boolean hasSuccess = results.stream().anyMatch(NotificationChannelProvider.DeliveryResult::isSuccess);
        assertTrue(hasSuccess);
    }
    
    @Test
    void multiChannelDelivery_ShouldSendToAllAvailableChannels() {
        // Given
        Notification notification = TestDataBuilder.createEmailNotification();
        UserNotificationPreferences preferences = TestDataBuilder.createAllChannelsPreferences();
        
        // When
        List<NotificationChannelProvider.DeliveryResult> results = channelProviderRegistry.sendToAllAvailableChannels(
            notification, preferences
        );
        
        // Then
        assertNotNull(results);
        assertTrue(results.size() >= 2); // Should find multiple available channels
        
        // At least one should be successful
        boolean hasSuccess = results.stream().anyMatch(NotificationChannelProvider.DeliveryResult::isSuccess);
        assertTrue(hasSuccess);
    }
    
    // =============================================
    // PROVIDER HEALTH TESTS
    // =============================================
    
    @Test
    void providerHealth_ShouldReturnValidStatus() {
        // When
        var healthStatus = channelProviderRegistry.getProviderHealthStatus();
        
        // Then
        assertNotNull(healthStatus);
        assertTrue(healthStatus.size() >= 5);
        
        // At least one provider should be healthy
        boolean hasHealthyProvider = healthStatus.values().stream().anyMatch(Boolean::booleanValue);
        assertTrue(hasHealthyProvider);
    }
    
    @Test
    void providerHealth_ShouldHaveHealthyProviders() {
        // When
        boolean hasHealthyProviders = channelProviderRegistry.hasHealthyProviders();
        
        // Then
        assertTrue(hasHealthyProviders);
    }
    
    // =============================================
    // CHANNEL SUPPORT TESTS
    // =============================================
    
    @Test
    void channelSupport_ShouldSupportAllChannels() {
        // Test each expected channel
        Notification.NotificationChannel[] expectedChannels = {
            Notification.NotificationChannel.EMAIL,
            Notification.NotificationChannel.SMS,
            Notification.NotificationChannel.PUSH,
            Notification.NotificationChannel.IN_APP,
            Notification.NotificationChannel.WEBHOOK
        };
        
        for (Notification.NotificationChannel channel : expectedChannels) {
            // When
            boolean isSupported = channelProviderRegistry.isChannelSupported(channel);
            
            // Then
            assertTrue(isSupported, "Channel " + channel + " should be supported");
        }
    }
    
    // =============================================
    // EDGE CASES
    // =============================================
    
    @Test
    void edgeCases_WithNullEvent_ShouldHandleGracefully() {
        // Given
        UserNotificationPreferences preferences = TestDataBuilder.createEmailOnlyPreferences();
        
        // When
        List<NotificationChannelProvider> providers = channelProviderRegistry.findSuitableProviders(null, preferences);
        
        // Then
        assertNotNull(providers);
        // Should still find providers based on preferences only
    }
    
    @Test
    void edgeCases_WithNullPreferences_ShouldHandleGracefully() {
        // Given
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        
        // When
        List<NotificationChannelProvider> providers = channelProviderRegistry.findSuitableProviders(event, null);
        
        // Then
        assertNotNull(providers);
        assertEquals(0, providers.size()); // No providers should be found without preferences
    }
    
    // =============================================
    // PERFORMANCE TESTS
    // =============================================
    
    @Test
    void performance_ProviderLookup_ShouldBeFast() {
        // Given
        NotificationEvent event = TestDataBuilder.createEmailNotificationEvent();
        UserNotificationPreferences preferences = TestDataBuilder.createEmailOnlyPreferences();
        
        // When
        long startTime = System.currentTimeMillis();
        NotificationChannelProvider provider = channelProviderRegistry.getBestProvider(event, preferences);
        long endTime = System.currentTimeMillis();
        
        // Then
        assertNotNull(provider);
        long lookupTime = endTime - startTime;
        assertTrue(lookupTime < 100, "Provider lookup should be fast (< 100ms), took: " + lookupTime + "ms");
    }
    
    @Test
    void performance_MultiChannelDelivery_ShouldBeReasonable() {
        // Given
        Notification notification = TestDataBuilder.createEmailNotification();
        UserNotificationPreferences preferences = TestDataBuilder.createAllChannelsPreferences();
        List<Notification.NotificationChannel> channels = List.of(
            Notification.NotificationChannel.EMAIL,
            Notification.NotificationChannel.IN_APP
        );
        
        // When
        long startTime = System.currentTimeMillis();
        List<NotificationChannelProvider.DeliveryResult> results = channelProviderRegistry.sendToMultipleChannels(
            notification, preferences, channels
        );
        long endTime = System.currentTimeMillis();
        
        // Then
        assertNotNull(results);
        long deliveryTime = endTime - startTime;
        assertTrue(deliveryTime < 5000, "Multi-channel delivery should be reasonable (< 5s), took: " + deliveryTime + "ms");
    }
}
