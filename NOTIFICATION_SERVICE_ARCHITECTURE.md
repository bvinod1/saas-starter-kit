# Notification Service Architecture

## Overview

The Notification Service is a robust, scalable subsystem that handles delivery of messages across multiple channels (email, SMS, push notifications, and in-app) tailored to user and tenant needs. It implements an Event-Driven Architecture (EDA) using RabbitMQ for decoupled, asynchronous processing.

## Architecture Diagram

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   Event Source  │───▶│   RabbitMQ      │───▶│  Notification       │
│   (User Action, │    │   Event Bus      │    │  Service            │
│    System Alert)│    │                  │    │                     │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
                                                         │
                                                         ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   Frontend      │◀───│   Database       │◀───│  Channel Providers  │
│   (Display,     │    │   (Logs,         │    │  (Email, SMS,       │
│    Preferences) │    │    Analytics)    │    │   Push, In-App)     │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
```

## Key Components

### 1. Event-Driven Architecture (EDA)

#### Event Flow
1. **Event Trigger**: User action, system alert, or workflow step
2. **Event Publishing**: Events published to RabbitMQ event bus
3. **Event Consumption**: Notification service consumes events
4. **Processing**: Validation, template processing, and channel routing
5. **Delivery**: Messages sent via appropriate channel providers
6. **Logging**: Status tracking and analytics

#### RabbitMQ Configuration
- **Exchanges**: Topic-based routing for flexible event distribution
- **Queues**: Separate queues for different notification types
- **Dead Letter Queue**: Failed message handling and retry logic
- **Message TTL**: Automatic expiration for old messages

### 2. Notification Service Core

#### Main Components
- **Event Consumer**: RabbitMQ message listener
- **Template Engine**: Dynamic content generation with FreeMarker
- **Channel Router**: Determines appropriate delivery channels
- **Preference Manager**: User notification settings and preferences
- **Status Tracker**: Delivery status and analytics

#### Key Features
- **Multi-tenant Support**: Tenant isolation and customization
- **Language Localization**: Multi-language template support
- **Priority Handling**: Urgent notification bypass for quiet hours
- **Retry Logic**: Automatic retry with exponential backoff
- **Rate Limiting**: Per-user and per-channel rate controls

### 3. Channel Providers

#### Provider Interface
```java
public interface NotificationChannelProvider {
    NotificationChannel getChannelType();
    boolean canHandle(NotificationEvent event, UserPreferences preferences);
    DeliveryResult send(Notification notification, UserPreferences preferences);
    boolean isHealthy();
    ValidationResult validate(Notification notification, UserPreferences preferences);
}
```

#### Supported Channels
- **Email**: SMTP and SendGrid integration
- **SMS**: Twilio integration
- **Push**: Firebase Cloud Messaging
- **In-App**: Real-time web notifications
- **Webhook**: Custom endpoint integration

#### Provider Features
- **Health Monitoring**: Provider availability checks
- **Fallback Logic**: Automatic channel switching on failure
- **Rate Limiting**: Provider-specific rate controls
- **Metrics Collection**: Delivery success/failure tracking

### 4. Templating Engine

#### Template Features
- **Variable Substitution**: Dynamic content with user data
- **Language Support**: Multi-language template versions
- **Category-based**: Different templates for different use cases
- **Priority Levels**: Template priority for delivery timing

#### Template Processing
```java
// Example template
"Hello {{firstName}}, your subscription expires on {{expiryDate}}"

// Processed with variables
Map<String, Object> variables = Map.of(
    "firstName", "John",
    "expiryDate", "2024-12-31"
);

// Result: "Hello John, your subscription expires on 2024-12-31"
```

### 5. User Preferences Management

#### Preference Categories
- **Channel Preferences**: Enable/disable specific channels
- **Timing Preferences**: Quiet hours and timezone settings
- **Language Preferences**: Preferred and fallback languages
- **Frequency Preferences**: Digest settings and rate limits
- **Category Preferences**: Notification type preferences

#### Preference Storage
- **Primary Storage**: PostgreSQL with JSONB columns
- **Fallback**: User entity notification preferences
- **Tenant Isolation**: Per-tenant preference customization
- **Default Values**: Sensible defaults for new users

## Database Schema

### Core Tables

#### 1. Notification Templates
```sql
CREATE TABLE shared.notification_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    variables JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    tenant_id VARCHAR(255),
    category VARCHAR(100),
    priority VARCHAR(20) DEFAULT 'NORMAL'
);
```

#### 2. Notifications
```sql
CREATE TABLE shared.notifications (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    attempt_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. User Notification Preferences
```sql
CREATE TABLE shared.user_notification_preferences (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255),
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    in_app_enabled BOOLEAN DEFAULT true,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50) DEFAULT 'UTC',
    preferred_language VARCHAR(10) DEFAULT 'en'
);
```

### Database Functions

#### 1. Get User Preferences
```sql
SELECT get_user_notification_preferences('user123', 'tenant456');
```

#### 2. Check Notification Availability
```sql
SELECT can_user_receive_notification('user123', 'tenant456', 'NORMAL');
```

## API Endpoints

### Notification Management

#### 1. Send Notification
```http
POST /api/notifications
Content-Type: application/json

{
  "eventType": "user_registration",
  "userId": "user123",
  "channel": "EMAIL",
  "templateName": "welcome_email",
  "variables": {
    "firstName": "John",
    "companyName": "Acme Corp"
  },
  "priority": "NORMAL",
  "immediate": true
}
```

#### 2. Get User Notifications
```http
GET /api/notifications?userId=user123&status=unread&limit=20
```

#### 3. Mark as Read
```http
PUT /api/notifications/{id}/read
```

### Preference Management

#### 1. Get User Preferences
```http
GET /api/notifications/preferences?userId=user123&tenantId=tenant456
```

#### 2. Update Preferences
```http
PUT /api/notifications/preferences
Content-Type: application/json

{
  "userId": "user123",
  "tenantId": "tenant456",
  "emailEnabled": true,
  "smsEnabled": false,
  "quietHoursStart": "22:00",
  "quietHoursEnd": "06:00",
  "preferredLanguage": "en",
  "timezone": "America/New_York"
}
```

### Template Management

#### 1. Create Template
```http
POST /api/notifications/templates
Content-Type: application/json

{
  "name": "billing_reminder",
  "type": "EMAIL",
  "subject": "Billing Reminder",
  "content": "Hello {{firstName}}, your subscription renews on {{renewalDate}}",
  "category": "billing",
  "priority": "NORMAL",
  "variables": {
    "firstName": {"type": "string", "description": "User's first name"},
    "renewalDate": {"type": "date", "description": "Subscription renewal date"}
  }
}
```

## Frontend Integration

### React Components

#### 1. Notification Context
```typescript
const { state, updatePreferences, markAsRead } = useNotifications();
```

#### 2. Preferences Component
```typescript
<NotificationPreferencesComponent />
```

#### 3. Notification List
```typescript
<NotificationList notifications={state.notifications} />
```

### Real-time Updates

#### 1. WebSocket Integration
- Real-time notification delivery
- Live preference updates
- Status change notifications

#### 2. Push Notifications
- Browser push notifications
- Mobile push notifications
- Desktop notifications

## Configuration

### Environment Variables

#### RabbitMQ Configuration
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: saas_user
    password: saas_password
    virtual-host: saas_vhost
```

#### External Service Configuration
```yaml
notification:
  providers:
    email:
      sendgrid:
        api-key: ${SENDGRID_API_KEY}
        from-email: ${SENDGRID_FROM_EMAIL}
      smtp:
        enabled: true
        from-email: ${SMTP_FROM_EMAIL}
    
    sms:
      twilio:
        account-sid: ${TWILIO_ACCOUNT_SID}
        auth-token: ${TWILIO_AUTH_TOKEN}
        from-number: ${TWILIO_FROM_NUMBER}
    
    push:
      firebase:
        project-id: ${FIREBASE_PROJECT_ID}
        private-key: ${FIREBASE_PRIVATE_KEY}
```

### Docker Configuration

#### RabbitMQ Service
```yaml
rabbitmq:
  image: rabbitmq:3.12-management-alpine
  environment:
    RABBITMQ_DEFAULT_USER: saas_user
    RABBITMQ_DEFAULT_PASS: saas_password
    RABBITMQ_DEFAULT_VHOST: saas_vhost
  ports:
    - "5672:5672"
    - "15672:15672"
```

## Monitoring and Analytics

### Metrics Collection

#### 1. Delivery Metrics
- Success/failure rates by channel
- Delivery latency measurements
- Retry attempt statistics
- Channel health status

#### 2. User Engagement
- Notification read rates
- Preference change tracking
- Channel usage patterns
- Quiet hours effectiveness

#### 3. System Performance
- Queue processing rates
- Template processing times
- Database query performance
- Memory and CPU usage

### Health Checks

#### 1. Service Health
- RabbitMQ connectivity
- Database connectivity
- External service availability
- Channel provider health

#### 2. Queue Monitoring
- Queue depth monitoring
- Message processing rates
- Dead letter queue analysis
- Consumer health status

## Security and Compliance

### Data Protection

#### 1. User Privacy
- Preference data encryption
- PII data masking
- Audit trail logging
- GDPR compliance support

#### 2. Access Control
- Tenant isolation
- User-level permissions
- API authentication
- Rate limiting

#### 3. Audit Logging
- All preference changes
- Notification delivery attempts
- Template modifications
- System configuration changes

## Deployment and Scaling

### Horizontal Scaling

#### 1. Service Instances
- Multiple notification service instances
- Load balancer distribution
- Instance health monitoring
- Auto-scaling based on queue depth

#### 2. Database Scaling
- Read replicas for analytics
- Connection pooling
- Query optimization
- Partitioning strategies

### High Availability

#### 1. RabbitMQ Clustering
- Multi-node RabbitMQ cluster
- Queue mirroring
- Automatic failover
- Message persistence

#### 2. Database Redundancy
- Primary-secondary replication
- Automatic failover
- Backup strategies
- Disaster recovery

## Testing and Quality Assurance

### Testing Strategy

#### 1. Unit Tests
- Service layer testing
- Template processing validation
- Channel provider mocking
- Preference management testing

#### 2. Integration Tests
- RabbitMQ integration
- Database operations
- External service mocking
- End-to-end workflows

#### 3. Performance Tests
- Load testing with high message volumes
- Latency benchmarking
- Memory usage profiling
- Database performance testing

### Quality Gates

#### 1. Code Quality
- SonarQube analysis
- Code coverage requirements
- Static code analysis
- Dependency vulnerability scanning

#### 2. Performance Requirements
- Maximum delivery latency: 5 seconds
- 99.9% uptime requirement
- Support for 10,000+ concurrent users
- Handle 1,000+ notifications per minute

## Future Enhancements

### Planned Features

#### 1. Advanced Analytics
- Machine learning for delivery optimization
- User behavior prediction
- A/B testing for templates
- ROI measurement for campaigns

#### 2. Enhanced Channels
- WhatsApp Business API
- Microsoft Teams integration
- Slack notifications
- Voice call notifications

#### 3. Workflow Automation
- Conditional notification chains
- Event-driven workflows
- Approval workflows
- Escalation procedures

### Technical Improvements

#### 1. Performance Optimization
- Redis caching for preferences
- Async processing improvements
- Database query optimization
- Message compression

#### 2. Monitoring Enhancements
- Real-time dashboards
- Predictive alerting
- Custom metrics
- Integration with APM tools

## Conclusion

The Notification Service provides a robust, scalable foundation for all notification needs in the SaaS Starter Kit. Its event-driven architecture ensures loose coupling, high availability, and easy scaling. The comprehensive preference management system allows users to control their notification experience while maintaining system reliability and performance.

The service is designed to be easily extensible for new channels and features, making it future-proof for evolving business requirements. With proper monitoring, testing, and deployment strategies, it provides enterprise-grade notification capabilities for modern SaaS applications.
