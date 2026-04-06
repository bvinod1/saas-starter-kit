# Tasks: Notification Service

**Feature**: `007-notification-service`
**Input**: `specs/007-notification-service/`
**Source**: `backend/notification-service/`

## Phase 1: Setup

- [ ] T001 Add dependencies to `backend/notification-service/pom.xml` (spring-amqp, spring-data-jpa, mustache, sendgrid-java, twilio-java, firebase-admin, opentelemetry-spring-starter)
- [ ] T002 [P] Configure `backend/notification-service/src/main/resources/application.yml` (RabbitMQ, SMTP, SendGrid, Twilio, FCM settings, DLQ)
- [ ] T003 [P] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/provider/EmailProvider.java` interface and `SmsProvider.java` interface

---

## Phase 2: Foundational

- [ ] T004 Add `notifications`, `notification_templates`, `user_notification_preferences` tables to `database/migrations/03-add-notification-preferences.sql`
- [ ] T005 [P] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/entity/Notification.java`, `NotificationTemplate.java`, `UserNotificationPreferences.java` JPA entities
- [ ] T006 [P] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/repository/NotificationRepository.java`, `NotificationTemplateRepository.java`, `UserNotificationPreferencesRepository.java`
- [ ] T007 Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/provider/ChannelProviderRegistry.java` — maps channel → active provider; configurable via properties
- [ ] T008 Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/TemplateRenderService.java` — renders Mustache templates with variable map

**Checkpoint**: Notification entities, provider registry, and template engine ready

> **Provider Contract**: `EmailProvider`, `SmsProvider`, `PushProvider` MUST be consumed from `shared-libs/providers/`. Implementations (SendGridEmailProvider, TwilioSmsProvider, FcmPushProvider) live in shared-libs only.

---

## Phase 3: User Story 1 + 4 — Event-Driven Dispatch & Multi-Channel (Priority: P1) 🎯 MVP

**Goal**: RabbitMQ event consumed → preferences checked → template rendered → delivered to all enabled channels independently.

**Independent Test**: Publish event to `notification.queue`; email channel receives rendered email; SMS not sent if user opted out; delivery status=SENT stored in DB.

- [ ] T009 [P] [US1] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/dto/NotificationEvent.java` (eventId, eventType, userId, tenantId, templateKey, variables)
- [ ] T010 [US1] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/NotificationDispatcher.java` — listens on `notification.queue`, resolves user preferences, selects channels, delegates to channel handlers
- [ ] T011 [US4] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/EmailNotificationService.java` — render template, call `EmailProvider`, persist delivery status
- [ ] T012 [US4] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/SmsNotificationService.java` — same pattern for SMS channel
- [ ] T013 [US4] Implement `InAppNotificationService.java` in same package — persist in-app notification, no external call
- [ ] T014 [US1] Implement channel-level retry with exponential backoff (max 3 retries) and route to `notifications.dlq` on exhaustion
- [ ] T014b [US1] Implement priority-aware dispatch in `NotificationDispatcher` — CRITICAL-priority events bypass user preferences and attempt ALL enabled channels; HIGH-priority events always attempt primary channel regardless of preference; NORMAL/LOW respect user opt-out; set `Notification.priority` field from event payload
- [ ] T014c [US1] Seed four built-in template types in `database/migrations/03-add-notification-preferences.sql`: `welcome`, `billing_reminder`, `security_alert`, `maintenance_notice`; each with default Mustache template body and subject
- [ ] T015 [US1] Add OTel tracing (tenantId, eventId, priority span attributes) in `NotificationDispatcher`

**Checkpoint**: Event-driven multi-channel dispatch independently testable

---

## Phase 4: User Story 2 — Notification Preferences (Priority: P1)

**Goal**: Users can set per-channel opt-in/out preferences.

**Independent Test**: PUT /api/v1/notifications/preferences disables SMS; next SMS event for that user is SKIPPED. Preference record persisted with tenantId.

- [ ] T016 [P] [US2] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/dto/UpdatePreferencesRequest.java`
- [ ] T017 [US2] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/PreferencesService.java` — `getPreferences(userId, tenantId)`, `updatePreferences()` with default-true for missing records
- [ ] T018 [US2] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/controller/NotificationPreferencesController.java` with `GET` and `PUT /api/v1/notifications/preferences`

**Checkpoint**: User preferences independently testable

---

## Phase 5: User Story 3 + 5 — Template Management & History (Priority: P2)

**Goal**: Admins manage templates with Mustache syntax validation; admins view delivery history with retry.

**Independent Test**: POST /api/v1/admin/notification-templates creates template; invalid Mustache returns 400. GET /api/v1/admin/notifications?status=FAILED filters correctly.

- [ ] T019 [P] [US3] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/dto/CreateTemplateRequest.java`
- [ ] T020 [US3] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/TemplateService.java` — `createTemplate()` with Mustache syntax validation, `updateTemplate()`, `deleteTemplate()`
- [ ] T021 [US3] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/controller/NotificationTemplateController.java` with full CRUD under `/api/v1/admin/notification-templates`
- [ ] T022 [US5] Implement `backend/notification-service/src/main/java/com/saasstarterkit/notification/service/NotificationHistoryService.java` — `listNotifications(tenantId, status, channel, dateRange, pageable)`, `retryNotification(notificationId)`
- [ ] T023 [US5] Create `backend/notification-service/src/main/java/com/saasstarterkit/notification/controller/NotificationHistoryController.java` with `GET /api/v1/admin/notifications` and `POST /api/v1/admin/notifications/{id}/retry`

**Checkpoint**: Template management and history independently testable

---

## Phase 6: Polish & Cross-Cutting

- [ ] T024 [P] Implement `SendGridEmailProvider.java` and `TwilioSmsProvider.java` in `backend/notification-service/src/main/java/com/saasstarterkit/notification/provider/impl/`
- [ ] T025 [P] Add Prometheus counters for notifications sent/failed per channel in `NotificationDispatcher`
- [ ] T026 Run `quickstart.md` smoke tests (publish event, verify delivery, check preferences, template CRUD)

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T008): Depends on Phase 1
- **Phase 3** (T009–T015): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T016–T018): Depends on Phase 2
- **Phase 5** (T019–T023): Depends on Phase 2, Phase 3
- **Phase 6** (T024–T026): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working event-driven notification dispatch.
