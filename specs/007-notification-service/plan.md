# Implementation Plan: Notification Service

**Branch**: `007-notification-service` | **Date**: 2026-04-04 | **Spec**: [spec.md](spec.md)

## Summary

Multi-channel notification delivery backed by RabbitMQ. The service consumes NotificationEvent
from notification.queue, resolves user preferences, renders Mustache templates, and delivers
via email (SendGrid/Mailgun/SES), SMS (Twilio/Plivo), push (FCM), in-app, and webhook channels.
All outbound email goes exclusively through the EmailProvider interface (no direct SMTP).

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.x
**Primary Dependencies**: Spring AMQP (RabbitMQ), Spring Web, Mustache, Spring Mail, Firebase Admin SDK
**Storage**: PostgreSQL 15 (notifications, notification_templates, user_notification_preferences)
**Testing**: JUnit 5, Testcontainers (RabbitMQ, PostgreSQL), MockMvc
**Target Platform**: Linux server
**Project Type**: East-West async consumer + North-South webhook delivery
**Performance Goals**: Email delivery < 5 s p95; 1 000 events/minute throughput
**Constraints**: At-least-once delivery; all providers swappable via config only
**Scale/Scope**: All tenants; 5 channels; template-driven

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Interface-First | PASS | NotificationChannelProvider + EmailProvider interfaces; all channel impls pluggable |
| II. Cloud Agnostic | PASS | Queue: RabbitMQ (local) / SQS (AWS) / Pub/Sub (GCP); email/SMS providers swappable |
| V. Traffic-Aware | PASS | Event consumption East-West (RabbitMQ); webhook delivery North-South outbound |
| VI. Tenant-Centric | PASS | Every notification carries tenantId; preferences per tenant-user |
| VII. Observability | PASS | Delivery result logged per channel; RED metrics on queue consumer |
| VIII. Security | PASS | Webhook retried with backoff; all outbound email via EmailProvider interface only |

## Project Structure

### Documentation (this feature)

```
specs/007-notification-service/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/asyncapi/notification-events.yaml
```

### Source Code (repository root)

```
backend/notification-service/
├── src/main/java/com/saasstarterkit/notification/
│   ├── domain/enums/     # NotificationChannel, NotificationPriority
│   ├── dto/              # NotificationEvent
│   ├── entity/           # Notification, NotificationTemplate, UserNotificationPreferences
│   ├── repository/
│   └── service/
│       ├── NotificationService.java
│       ├── TemplatingService.java
│       ├── NotificationTemplateService.java
│       ├── UserPreferencesService.java
│       ├── ChannelProviderRegistry.java
│       └── channel/      # EmailChannelProvider, SmsChannelProvider, PushChannelProvider,
│                           InAppChannelProvider, WebhookChannelProvider

frontend/src/pages/admin/NotificationManagement.tsx
contracts/asyncapi/notification-events.yaml
```

**Structure Decision**: Standalone async consumer service. AsyncAPI contract for event schema.

## Complexity Tracking

> No constitution violations.
