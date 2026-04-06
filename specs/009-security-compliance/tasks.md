# Tasks: Security & Compliance

**Feature**: `009-security-compliance`
**Input**: `specs/009-security-compliance/`
**Source**: `backend/user-service/` (compliance domain)

## Phase 1: Setup

- [ ] T001 Add dependencies to `backend/user-service/pom.xml` (vault-spring-starter, spring-kafka, jasypt-spring-boot)
- [ ] T002 [P] Configure `backend/user-service/src/main/resources/application.yml` — Vault address/token, Kafka brokers, AES key path
- [ ] T003 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/SecretsProvider.java` interface + `VaultSecretsProvider.java` impl (no hardcoded secrets)

---

## Phase 2: Foundational

- [ ] T004 Create `database/migrations/05-gdpr-hipaa-compliance.sql` — `data_consents`, `data_subject_requests`, `protected_health_information`, `security_alerts`, `compliance_checks` tables; add `is_encrypted` and `data_hash` columns
- [ ] T005 [P] Create `backend/user-service/src/main/java/com/saasstarterkit/user/entity/DataConsent.java`, `DataSubjectRequest.java`, `SecurityAlert.java`, `ComplianceCheck.java`, `ProtectedHealthInformation.java` JPA entities
- [ ] T006 [P] Create repositories for each compliance entity
- [ ] T007 Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/AesEncryptionService.java` — AES-256-GCM encrypt/decrypt; key fetched from `SecretsProvider`
- [ ] T008 Create `backend/user-service/src/main/java/com/saasstarterkit/user/security/ImmutableAuditLogger.java` — append-only write to `security_alerts` table with separate write-only DB user

**Checkpoint**: Compliance entities, encryption, and immutable audit ready

> **Provider Contract**: All object storage (audit exports, GDPR portability archives) MUST go through `StorageProvider` from `shared-libs/providers/`.

---

## Phase 3: User Story 1 + 4 — GDPR Consent & Security Monitoring (Priority: P1) 🎯 MVP

**Goal**: User manages consent; system detects brute force and raises scored security alerts.

**Independent Test**: New user → ESSENTIAL consent auto-created. Withdraw MARKETING consent → status=WITHDRAWN, timestamp recorded. 5+ failed logins from same IP → HIGH alert in security_alerts.

- [ ] T009 [P] [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/ConsentUpdateRequest.java`
- [ ] T010 [US1] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/ConsentService.java` — `initializeConsents(userId)` on registration, `updateConsent(userId, type, consented)`, `getConsents(userId)`
- [ ] T011 [US1] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/ConsentController.java` with `GET /api/v1/users/{id}/consent` and `PUT /api/v1/users/{id}/consent`
- [ ] T012 [US4] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/security/SecurityMonitoringService.java` — `detectBruteForce(userId, ip)`, `detectAnomalousApiUsage(tenantId, endpoint)`, `createSecurityAlert(type, severity, riskScore)`
- [ ] T013 [US4] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/SecurityAlertController.java` with `GET /api/v1/admin/security/alerts` (filterable by severity, status)

**Checkpoint**: GDPR consent and basic security monitoring independently testable

---

## Phase 4: User Story 2 — Data Subject Rights (Priority: P1)

**Goal**: Users submit ACCESS, ERASURE, or PORTABILITY requests; erasure saga notifies all services.

**Independent Test**: POST /api/v1/users/{id}/data-requests with type=ERASURE → request created, `UserDataErasureEvent` published to Kafka. PORTABILITY request generates JSON export.

- [ ] T014 [P] [US2] Create `backend/user-service/src/main/java/com/saasstarterkit/user/dto/DataSubjectRequestDto.java`
- [ ] T015 [US2] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/DataSubjectRequestService.java` — `submitRequest()`, `processErasure()` (publish `UserDataErasureEvent` to Kafka), `processPortability()` (generate JSON/CSV export)
- [ ] T016 [US2] Create `backend/user-service/src/main/java/com/saasstarterkit/user/kafka/ErasureEventProducer.java` — publish `UserDataErasureEvent`
- [ ] T017 [US2] Add `POST /api/v1/users/{id}/data-requests` and `GET /api/v1/users/{id}/data-requests` to a new `DataSubjectRequestController.java`

**Checkpoint**: Data subject rights independently testable

---

## Phase 5: User Story 3 + 5 — HIPAA PHI & Compliance Dashboard (Priority: P2)

**Goal**: HIPAA tenants store encrypted PHI with access audit; admins view compliance scores.

**Independent Test**: PHI record created with is_encrypted=true, data_hash stored. PHI read → audit log entry. Compliance endpoint returns score per framework.

- [ ] T018 [P] [US3] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/PhiService.java` — `storePhi(userId, category, data)` (encrypt + hash), `readPhi(userId, category, purpose)` (decrypt + audit log)
- [ ] T019 [US3] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/PhiController.java` with `POST` and `GET /api/v1/users/{id}/phi` (HIPAA-enabled tenants only)
- [ ] T020 [US5] Implement `backend/user-service/src/main/java/com/saasstarterkit/user/service/ComplianceService.java` — compute score per framework (GDPR, HIPAA, SOX, PCI-DSS) based on fulfilled checks
- [ ] T021 [US5] Create `backend/user-service/src/main/java/com/saasstarterkit/user/controller/ComplianceController.java` with `GET /api/v1/admin/compliance/scores`

**Checkpoint**: PHI protection and compliance dashboard independently testable

---

## Phase 6: Polish & Cross-Cutting

- [ ] T022 [P] Add Kafka consumer in `notification-service` to handle `UserDataErasureEvent` and anonymise notification history
- [ ] T023 [P] Add SAST scan config (SpotBugs + OWASP dependency check) to `backend/*/pom.xml` in `verify` phase
- [ ] T024 Run `quickstart.md` smoke tests (consent, erasure request, PHI store/read, compliance score)

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T008): Depends on Phase 1
- **Phase 3** (T009–T013): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T014–T017): Depends on Phase 2
- **Phase 5** (T018–T021): Depends on Phase 2, Phase 3
- **Phase 6** (T022–T024): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for GDPR consent and brute-force security alerting.
