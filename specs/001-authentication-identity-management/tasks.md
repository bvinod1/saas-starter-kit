# Tasks: Authentication & Identity Management

**Feature**: `001-authentication-identity-management`
**Input**: `specs/001-authentication-identity-management/`
**Source**: `backend/auth-service/`

## Phase 1: Setup

- [ ] T001 Add Spring Boot 3.x dependencies to `backend/auth-service/pom.xml` (spring-security, keycloak-admin-client, jjwt, spring-data-jpa, spring-web, micrometer-registry-prometheus, opentelemetry-spring-starter)
- [ ] T002 [P] Configure `backend/auth-service/src/main/resources/application.yml` with Keycloak, PostgreSQL, Redis, and OTel settings
- [ ] T003 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/config/SecurityConfig.java` (disable session, stateless JWT)

---

## Phase 2: Foundational

- [ ] T004 Create `database/init/01-init-schema.sql` auth schema tables: `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `refresh_tokens`, `audit_log`
- [ ] T005 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/User.java` JPA entity
- [ ] T006 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/Role.java` JPA entity
- [ ] T007 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/service/KeycloakService.java` interface + impl (create realm, create client, create user)
- [ ] T008 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/config/KeycloakConfig.java` (Keycloak admin client bean)
- [ ] T009 [P] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/repository/UserRepository.java` and `RoleRepository.java`
- [ ] T010 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/util/JwtUtil.java` (generate + validate JWT using JJWT)

**Checkpoint**: Foundation complete — all P1/P2/P3 stories can proceed

> **Provider Contract**: All Keycloak calls MUST go through `IdentityProvider` from `shared-libs/providers/` — no direct Keycloak admin-client imports in service code.

---

## Phase 3: User Story 1 + 2 — Registration & Login (Priority: P1) 🎯 MVP

**Goal**: A user can register (creates Keycloak user + tenant schema) and login to receive JWT tokens.

**Independent Test**: POST /api/v1/auth/register returns 201 with tokens; POST /api/v1/auth/login returns 200 with tokens; duplicate email returns 409.

- [ ] T011 [P] [US1] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/dto/RegisterRequest.java` and `RegisterResponse.java`
- [ ] T012 [P] [US1] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/dto/LoginRequest.java` and `TokenResponse.java`
- [ ] T013 [US1] Implement `backend/auth-service/src/main/java/com/saasstarterkit/auth/service/AuthService.java` — `register()` method: validate input, create Keycloak user, call `create_tenant_schema()`, persist User entity, return tokens
- [ ] T014 [US1] Implement `AuthService.login()` — authenticate via Keycloak, issue JWT access + refresh tokens, persist refresh token hash
- [ ] T015 [US1] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/controller/AuthController.java` with `POST /api/v1/auth/register` and `POST /api/v1/auth/login`
- [ ] T016 [US1] Add account lockout logic in `AuthService` (track failed attempts in `audit_log`, lock after configurable threshold)
- [ ] T017 [US1] Add OTel span instrumentation and MDC logging (tenantId, correlationId) in `AuthService`

**Checkpoint**: Registration and login independently testable

---

## Phase 4: User Story 3 — Token Refresh + Validation (Priority: P1)

**Goal**: Clients can refresh expired access tokens and validate any token.

**Independent Test**: POST /api/v1/auth/refresh with valid refresh token returns new access token; expired token returns 401. POST /api/v1/auth/validate returns valid/expired flags.

- [ ] T018 [P] [US3] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/RefreshToken.java` entity and `RefreshTokenRepository.java`
- [ ] T019 [US3] Implement `AuthService.refreshToken()` — verify token hash, check expiry, rotate refresh token, return new access token
- [ ] T020 [US3] Implement `AuthService.validateToken()` — parse JWT claims, check expiry, return `TokenValidationResponse`
- [ ] T021 [US3] Add `POST /api/v1/auth/refresh` and `POST /api/v1/auth/validate` to `AuthController`
- [ ] T022 [US3] Add logout endpoint `POST /api/v1/auth/logout` — revoke refresh token in DB

**Checkpoint**: Full token lifecycle independently testable

---

## Phase 5: User Story 4 + 5 — OAuth2 Social Login + Token Validation (Priority: P2)

**Goal**: Users can authenticate via Google, Apple, or Microsoft OAuth2.

**Independent Test**: Callback to `/api/v1/auth/login-success` creates session; `/api/v1/auth/login-failure` returns error.

- [ ] T023 [P] [US4] Add `spring-security-oauth2-client` to `backend/auth-service/pom.xml`
- [ ] T024 [P] [US4] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/config/OAuth2Config.java` (Google, Apple, Microsoft provider config)
- [ ] T025 [US4] Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/entity/OAuthProvider.java` entity (user_id, provider, external_id)
- [ ] T026 [US4] Implement `OAuth2AuthenticationSuccessHandler` — link provider identity to User, issue JWT tokens
- [ ] T027 [US4] Implement `OAuth2AuthenticationFailureHandler` — return structured error response

**Checkpoint**: OAuth2 social login independently testable

---

## Phase 6: User Story 6 — MFA Enrolment & Enforcement (Priority: P3)

**Goal**: Users can enrol TOTP MFA; subsequent logins require the OTP code.

**Independent Test**: POST /api/v1/auth/mfa/enroll returns QR code; login without OTP returns 401 requesting OTP; login with valid OTP returns 200.

- [ ] T028 [P] [US6] Add `GoogleAuthenticator` TOTP library to `backend/auth-service/pom.xml`
- [ ] T029 [US6] Implement `MfaService.java` in `backend/auth-service/src/main/java/com/saasstarterkit/auth/service/` — enroll TOTP (generate secret, QR URI), verify code
- [ ] T030 [US6] Update `AuthService.login()` — if `mfa_enabled=true`, require OTP code before issuing tokens
- [ ] T031 [US6] Add `POST /api/v1/auth/mfa/enroll` and `POST /api/v1/auth/mfa/verify` to `AuthController`
- [ ] T032 [US6] Update `User` entity — add `mfa_enabled`, `mfa_secret` (encrypted), `mfa_verified` fields

**Checkpoint**: MFA enrolment and enforcement independently testable

---

## Phase 6b: User Data Erasure Consumer (Cross-Cutting)

**Goal**: auth-service consumes `UserDataErasureEvent` from Kafka and anonymises stored credentials/tokens.

**Independent Test**: Publish `UserDataErasureEvent` to Kafka → auth-service deletes Keycloak user + sets login_history records to anonymised state within 30 s.

- [ ] TErasure-001 Add `spring-kafka` dependency to `backend/auth-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-002 Create `backend/auth-service/src/main/java/com/saasstarterkit/auth/kafka/ErasureEventConsumer.java` — `@KafkaListener(topics = "user.erasure")` — calls `KeycloakIdentityProvider.deleteUser(userId)` and anonymises `login_history` rows for the user

**Checkpoint**: Erasure consumer independently verifiable by publishing test event

---

## Phase 7: Polish & Cross-Cutting

- [ ] T033 [P] Add Prometheus metrics in `AuthController` (login count, registration count, failure rate)
- [ ] T034 [P] Add email verification flow: `POST /api/v1/auth/verify-email/{token}` in `AuthController`
- [ ] T035 [P] Add password reset flow: `POST /api/v1/auth/password-reset/request` and `POST /api/v1/auth/password-reset/confirm`
- [ ] T036 Run `quickstart.md` smoke tests against the running service

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T010): Depends on Phase 1
- **Phase 3** (T011–T017): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T018–T022): Depends on Phase 2
- **Phase 5** (T023–T027): Depends on Phase 2
- **Phase 6** (T028–T032): Depends on Phase 3 (login flow)
- **Phase 7** (T033–T036): Depends on all phases

### Parallel Opportunities per Story
- T005, T006, T009 can run in parallel (different entity files)
- T011, T012 can run in parallel (different DTOs)
- T023, T024, T025 can run in parallel

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for a fully working registration and login.
