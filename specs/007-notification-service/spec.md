# Feature Specification: Notification Service

**Feature Branch**: `007-notification-service`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — System dispatches event-driven notification (Priority: P1)

A service publishes a NotificationEvent to RabbitMQ. The notification service consumes it,
resolves the user's preferences, renders the template, and delivers via all enabled channels.

**Acceptance Scenarios**:
1. **Given** valid event on notification.queue, **When** consumed, **Then** notification delivered to enabled channels.
2. **Given** user with email channel disabled, **When** event processed, **Then** email NOT sent.
3. **Given** template processing failure, **When** event consumed, **Then** error logged, notification status = FAILED.

---

### User Story 2 — User sets notification preferences (Priority: P1)

A user configures per-channel opt-in/opt-out preferences (email, SMS, push, in-app, webhook).

**Acceptance Scenarios**:
1. **Given** user opts out of SMS, **When** SMS notification triggered for that user, **Then** SMS NOT sent.
2. **Given** user opts into push notifications, **When** push sent, **Then** FCM token used for delivery.

---

### User Story 3 — Admin manages templates (Priority: P2)

An admin creates, edits, and deletes notification templates with Mustache-style variable substitution.

**Acceptance Scenarios**:
1. **Given** admin, **When** template created with variables `{{firstName}}, {{planName}}`, **Then** template saved with variable list.
2. **Given** template update, **When** existing notifications queued, **Then** in-flight notifications use old template version.
3. **Given** template with invalid Mustache syntax, **When** saved, **Then** 400 validation error.

---

### User Story 4 — Multi-channel delivery (Priority: P1)

The service routes a single notification event to multiple channels (email + in-app + SMS) simultaneously.

**Acceptance Scenarios**:
1. **Given** CRITICAL priority event, **When** processed, **Then** all enabled channels attempted regardless of preferences.
2. **Given** email delivery failed, **When** processing, **Then** in-app channel not blocked; each channel independent.

---

### User Story 5 — Admin monitors notification history (Priority: P2)

An admin views notification delivery history per tenant filtered by status, channel, and date range.

**Acceptance Scenarios**:
1. **Given** admin, **When** filter by status=FAILED, **Then** only failed notifications returned.
2. **Given** failed notification, **When** admin triggers retry, **Then** notification re-queued for delivery.

---

### Edge Cases
- What happens when user preferences record does not exist?
- How are bounced email addresses handled?
- What is the retry strategy for failed channel delivery?

## Requirements

### Functional Requirements
- **FR-001**: System MUST consume events from RabbitMQ `notification.queue`.
- **FR-002**: System MUST support channels: EMAIL, SMS, IN_APP, PUSH, WEBHOOK.
- **FR-003**: System MUST resolve user preferences before delivering.
- **FR-004**: Email providers MUST be pluggable: SendGrid, Mailgun, AWS SES.
- **FR-005**: SMS providers MUST be pluggable: Twilio, Plivo.
- **FR-006**: Push MUST use Firebase FCM.
- **FR-007**: All outbound email MUST go through the `EmailProvider` interface (no direct SMTP).
- **FR-008**: Templates MUST support Mustache-style variable substitution.
- **FR-009**: System MUST record delivery status per notification per channel.
- **FR-010**: Webhook delivery MUST use at-least-once semantics with exponential backoff retry.
- **FR-011**: Notification priority levels MUST be: CRITICAL, HIGH, NORMAL, LOW.
- **FR-012**: Templates types MUST include: welcome, billing_reminder, security_alert, maintenance_notice.

### Key Entities
- **NotificationEvent**: eventId, eventType, userId, tenantId, channel hints, templateKey, variables
- **Notification**: userId, tenantId, channel, status, subject, body, deliveredAt, failureReason
- **NotificationTemplate**: name, type, channel, subject, content, variables, version
- **UserNotificationPreferences**: userId, tenantId, emailEnabled, smsEnabled, pushEnabled, inAppEnabled
- **ChannelProviderRegistry**: maps channel type → active provider

### Non-Functional Requirements
- Notification delivery MUST be at-least-once
- Email delivery MUST complete in < 5 s p95
- Service MUST handle 1 000 events/minute
