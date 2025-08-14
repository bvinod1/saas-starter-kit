# Notification Service Testing Guide

## Overview

This guide provides comprehensive information about testing the notification service, including unit tests, integration tests, and test utilities. The testing framework ensures all notification channels, services, and integrations work correctly.

## Test Structure

### 1. Unit Tests
- **Location**: `src/test/java/com/saasstarterkit/notification/service/channel/`
- **Purpose**: Test individual channel providers in isolation
- **Coverage**: All public methods, edge cases, and error scenarios

### 2. Integration Tests
- **Location**: `src/test/java/com/saasstarterkit/notification/integration/`
- **Purpose**: Test end-to-end notification flow with real services
- **Coverage**: Database, message queue, and service interactions

### 3. Test Utilities
- **Location**: `src/test/java/com/saasstarterkit/notification/testutil/`
- **Purpose**: Provide test data builders and helper methods
- **Coverage**: Test data creation, mock setup, and assertions

## Running Tests

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker (for integration tests with TestContainers)

### Command Line

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*UnitTest"

# Run only integration tests
mvn test -Dtest="*IntegrationTest"

# Run specific test class
mvn test -Dtest=EmailChannelProviderTest

# Run tests with coverage
mvn test jacoco:report

# Run tests in parallel
mvn test -Dparallel=true
```

### IDE Integration

#### IntelliJ IDEA
1. Right-click on test class or method
2. Select "Run Test" or "Debug Test"
3. Use Ctrl+Shift+F10 (Windows/Linux) or Cmd+Shift+R (Mac)

#### Eclipse
1. Right-click on test class or method
2. Select "Run As" > "JUnit Test"
3. Use Alt+Shift+X, T

#### VS Code
1. Install Java Extension Pack
2. Use Test Explorer view
3. Click play button next to test methods

## Test Categories

### 1. Channel Provider Tests

#### Email Channel Provider (`EmailChannelProviderTest`)
- **SMTP Configuration**: Tests SMTP settings and fallbacks
- **Email Validation**: Tests recipient format validation
- **Message Processing**: Tests email content handling
- **Error Handling**: Tests SMTP failures and exceptions
- **Health Checks**: Tests provider health status

**Key Test Methods**:
```java
@Test
void send_WithValidNotification_ShouldReturnSuccess()

@Test
void validate_WithInvalidEmailFormat_ShouldReturnInvalid()

@Test
void isHealthy_WithSmtpEnabled_ShouldReturnTrue()
```

#### SMS Channel Provider (`SmsChannelProviderTest`)
- **Twilio Integration**: Tests Twilio API configuration
- **Phone Validation**: Tests E.164 format validation
- **Message Length**: Tests SMS character limits
- **Rate Limiting**: Tests SMS rate limiting
- **Multi-part SMS**: Tests long message handling

**Key Test Methods**:
```java
@Test
void validate_WithMessageTooLong_ShouldReturnInvalid()

@Test
void isValidPhoneNumber_WithValidE164Format_ShouldReturnTrue()

@Test
void send_WithValidNotification_ShouldReturnSuccess()
```

#### Push Channel Provider (`PushChannelProviderTest`)
- **Firebase Integration**: Tests FCM API configuration
- **Device Tokens**: Tests multiple device support
- **Rich Notifications**: Tests title, body, and data payloads
- **Priority Handling**: Tests high-priority notifications
- **Cross-platform**: Tests iOS, Android, and web support

#### In-App Channel Provider (`InAppChannelProviderTest`)
- **WebSocket Integration**: Tests real-time delivery
- **User Targeting**: Tests user-specific notifications
- **Tenant Broadcasting**: Tests organization-wide notifications
- **Priority Routing**: Tests urgent message handling
- **Connection Management**: Tests WebSocket lifecycle

#### Webhook Channel Provider (`WebhookChannelProviderTest`)
- **HTTP Integration**: Tests external endpoint delivery
- **Async Processing**: Tests non-blocking delivery
- **Timeout Management**: Tests request timeouts
- **Header Customization**: Tests custom HTTP headers
- **Multi-endpoint**: Tests multiple webhook URLs

### 2. Service Tests

#### Channel Provider Registry (`ChannelProviderRegistryTest`)
- **Provider Discovery**: Tests automatic provider registration
- **Provider Selection**: Tests intelligent routing
- **Health Monitoring**: Tests provider health status
- **Multi-channel Delivery**: Tests sending to multiple channels
- **Priority Management**: Tests channel priority selection

#### Channel Provider Factory (`ChannelProviderFactoryTest`)
- **Dynamic Creation**: Tests runtime provider creation
- **Configuration Validation**: Tests provider configuration
- **Provider Caching**: Tests provider instance caching
- **Cache Management**: Tests cache lifecycle operations

#### Notification Health Service (`NotificationHealthServiceTest`)
- **Health Checks**: Tests system health monitoring
- **Provider Health**: Tests individual provider health
- **Queue Monitoring**: Tests notification queue status
- **Database Health**: Tests database connectivity
- **System Metrics**: Tests overall system performance

### 3. Integration Tests

#### Notification Service Integration (`NotificationServiceIntegrationTest`)
- **End-to-End Flow**: Tests complete notification delivery
- **Database Integration**: Tests PostgreSQL operations
- **Message Queue**: Tests RabbitMQ integration
- **Multi-channel Delivery**: Tests sending to multiple channels
- **Performance Testing**: Tests system performance under load

## Test Data Management

### Test Data Builder (`TestDataBuilder`)

The `TestDataBuilder` class provides factory methods for creating test data:

```java
// Create notification events
NotificationEvent emailEvent = TestDataBuilder.createEmailNotificationEvent();
NotificationEvent smsEvent = TestDataBuilder.createSmsNotificationEvent();

// Create notifications
Notification emailNotification = TestDataBuilder.createEmailNotification();
Notification smsNotification = TestDataBuilder.createSmsNotification();

// Create user preferences
UserNotificationPreferences emailOnlyPrefs = TestDataBuilder.createEmailOnlyPreferences();
UserNotificationPreferences allChannelsPrefs = TestDataBuilder.createAllChannelsPreferences();

// Create templates
NotificationTemplate emailTemplate = TestDataBuilder.createEmailTemplate();
NotificationTemplate smsTemplate = TestDataBuilder.createSmsTemplate();
```

### Test Configuration (`TestConfiguration`)

The `TestConfiguration` class provides test-specific beans and utilities:

```java
@Autowired
private TestConfiguration.TestEnvironmentConfig testConfig;

@Autowired
private TestConfiguration.TestDataFactory testDataFactory;

// Configure test environment
testConfig.enableChannels(true, false, false, false, false); // Email only

// Create test data
TestConfiguration.TestNotificationData notificationData = testDataFactory.createTestNotificationData();
TestConfiguration.TestUserPreferences userPrefs = testDataFactory.createTestUserPreferences();
```

## Test Configuration

### Test Profile (`application-test.yml`)

The test profile uses:
- **In-memory Database**: H2 or TestContainers PostgreSQL
- **Mock Services**: Mock external service integrations
- **Test Containers**: Real PostgreSQL and RabbitMQ for integration tests
- **Reduced Timeouts**: Faster test execution
- **Debug Logging**: Detailed test output

### Environment Variables

```bash
# Test-specific environment variables
TEST_PROFILE=test
TEST_DATABASE_URL=jdbc:h2:mem:testdb
TEST_RABBITMQ_HOST=localhost
TEST_RABBITMQ_PORT=5672
```

## Mocking and Stubbing

### External Service Mocks

```java
@Mock
private JavaMailSender javaMailSender;

@Mock
private RestTemplate restTemplate;

@Mock
private MimeMessage mimeMessage;

// Setup mock behavior
when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
doNothing().when(javaMailSender).send(any(MimeMessage.class));
```

### Configuration Mocks

```java
// Mock configuration values
ReflectionTestUtils.setField(emailProvider, "smtpEnabled", true);
ReflectionTestUtils.setField(emailProvider, "smtpFromEmail", "test@example.com");
```

## Assertions and Validations

### Basic Assertions

```java
// Success assertions
assertTrue(result.isSuccess());
assertNotNull(result.getMessageId());
assertTrue(result.getDeliveryTimeMs() > 0);

// Error assertions
assertFalse(result.isSuccess());
assertNotNull(result.getErrorMessage());
assertTrue(result.getErrorMessage().contains("expected error"));
```

### Complex Validations

```java
// Validate provider configuration
ChannelConfiguration config = provider.getConfiguration();
assertNotNull(config);
assertEquals("Provider Name", config.getProviderName());
assertTrue(config.isEnabled());

// Validate provider metadata
ChannelMetadata metadata = provider.getMetadata();
assertNotNull(metadata);
assertEquals("1.0.0", metadata.getVersion());
assertTrue(metadata.isSupportsBulk());
```

## Performance Testing

### Response Time Assertions

```java
@Test
void performance_ProviderLookup_ShouldBeFast() {
    long startTime = System.currentTimeMillis();
    NotificationChannelProvider provider = registry.getBestProvider(event, preferences);
    long endTime = System.currentTimeMillis();
    
    long lookupTime = endTime - startTime;
    assertTrue(lookupTime < 100, "Provider lookup should be fast (< 100ms), took: " + lookupTime + "ms");
}
```

### Load Testing

```java
@Test
void performance_MultiChannelDelivery_ShouldBeReasonable() {
    long startTime = System.currentTimeMillis();
    List<DeliveryResult> results = registry.sendToMultipleChannels(notification, preferences, channels);
    long endTime = System.currentTimeMillis();
    
    long deliveryTime = endTime - startTime;
    assertTrue(deliveryTime < 5000, "Multi-channel delivery should be reasonable (< 5s), took: " + deliveryTime + "ms");
}
```

## Error Handling Tests

### Exception Scenarios

```java
@Test
void send_WithSmtpException_ShouldReturnFailure() throws Exception {
    // Given
    when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));
    
    // When
    DeliveryResult result = emailProvider.send(notification, preferences);
    
    // Then
    assertFalse(result.isSuccess());
    assertTrue(result.getErrorMessage().contains("Failed to send email"));
    assertTrue(result.getErrorMessage().contains("SMTP error"));
}
```

### Graceful Degradation

```java
@Test
void send_WithSmtpDisabled_ShouldReturnFailure() {
    // Given
    ReflectionTestUtils.setField(emailProvider, "smtpEnabled", false);
    
    // When
    DeliveryResult result = emailProvider.send(notification, preferences);
    
    // Then
    assertFalse(result.isSuccess());
    assertEquals("SMTP email provider not configured or disabled", result.getErrorMessage());
}
```

## Test Coverage

### Coverage Goals

- **Line Coverage**: > 90%
- **Branch Coverage**: > 85%
- **Method Coverage**: > 95%
- **Class Coverage**: 100%

### Coverage Reports

```bash
# Generate coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Coverage Exclusions

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/entity/**</exclude>
            <exclude>**/config/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

## Continuous Integration

### GitHub Actions

```yaml
name: Test Notification Service
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn test jacoco:report
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Coverage') {
            steps {
                sh 'mvn test jacoco:report'
            }
        }
    }
    post {
        always {
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Test Coverage Report'
            ])
        }
    }
}
```

## Troubleshooting

### Common Test Issues

#### 1. Test Container Startup Failures
```bash
# Check Docker status
docker ps

# Restart Docker
sudo systemctl restart docker

# Check container logs
docker logs <container_id>
```

#### 2. Database Connection Issues
```bash
# Check database status
docker exec -it <postgres_container> psql -U test_user -d test_db

# Check connection string
echo $TEST_DATABASE_URL
```

#### 3. RabbitMQ Connection Issues
```bash
# Check RabbitMQ status
docker exec -it <rabbitmq_container> rabbitmqctl status

# Check management interface
curl http://localhost:15672/api/overview
```

#### 4. Mock Setup Issues
```java
// Verify mock setup
verify(javaMailSender, times(1)).send(any(MimeMessage.class));

// Reset mocks between tests
@BeforeEach
void setUp() {
    reset(javaMailSender);
    // Setup mocks...
}
```

### Debug Mode

Enable debug logging for tests:

```yaml
# application-test.yml
logging:
  level:
    com.saasstarterkit.notification: DEBUG
    org.springframework.amqp: DEBUG
    org.springframework.mail: DEBUG
    org.testcontainers: INFO
```

### Test Isolation

Ensure tests don't interfere with each other:

```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotificationServiceTest {
    // Tests...
}
```

## Best Practices

### 1. Test Organization
- Group related tests in the same class
- Use descriptive test method names
- Follow AAA pattern (Arrange, Act, Assert)
- Keep tests focused and single-purpose

### 2. Test Data Management
- Use builders for complex test data
- Create reusable test utilities
- Avoid hardcoded test values
- Use meaningful test data

### 3. Mock Management
- Mock only external dependencies
- Use realistic mock responses
- Verify mock interactions
- Reset mocks between tests

### 4. Assertions
- Use specific assertions
- Validate all relevant properties
- Include meaningful error messages
- Test both success and failure scenarios

### 5. Performance Testing
- Set realistic performance thresholds
- Test under normal and load conditions
- Monitor resource usage
- Use performance profiling tools

## Conclusion

This testing guide provides a comprehensive framework for testing the notification service. By following these guidelines, you can ensure:

- **Reliability**: All notification channels work correctly
- **Performance**: System meets performance requirements
- **Maintainability**: Tests are easy to understand and modify
- **Coverage**: All critical functionality is tested
- **Quality**: High-quality, production-ready code

For additional questions or issues, refer to the test code examples or contact the development team.
