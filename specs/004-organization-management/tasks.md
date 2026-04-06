# Tasks: Organisation Management

**Feature**: `004-organization-management`
**Input**: `specs/004-organization-management/`
**Source**: `backend/organisation-service/`

## Phase 1: Setup

- [ ] T001 Verify/add dependencies in `backend/organisation-service/pom.xml` (spring-data-jpa, spring-amqp, spring-web, opentelemetry-spring-starter)
- [ ] T002 [P] Configure `backend/organisation-service/src/main/resources/application.yml` (datasource, RabbitMQ, OTel)
- [ ] T003 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/config/RabbitMqConfig.java`

---

## Phase 2: Foundational

- [ ] T004 Verify `database/init/03-organizational-management-schema.sql` has `organizations`, `organization_members`, `org_audit_log` tables; add if missing
- [ ] T005 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/Organisation.java` JPA entity (id, tenant_id, name, slug, tier, status, max_users, soft-delete fields, onboarding fields)
- [ ] T006 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/OrganisationMember.java` JPA entity (org_id, user_id, role, status, department, job_title, invited_by, accepted_at, deleted_at)
- [ ] T007 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/OrgAuditLog.java` JPA entity
- [ ] T008 [P] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/repository/OrganisationRepository.java` and `OrganisationMemberRepository.java`
- [ ] T009 Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/service/OrgAuditService.java` — async audit log writes via `@Async` to avoid adding latency

**Checkpoint**: Entity + audit foundation ready

---

## Phase 3: User Story 1 + 2 — Org CRUD & Listing (Priority: P1) 🎯 MVP

**Goal**: Users can create and list organisations within their tenant.

**Independent Test**: POST /api/v1/organizations creates org under tenant schema; GET lists all; duplicate slug returns unique constraint error; missing X-Tenant-ID returns 400.

- [ ] T010 [P] [US1] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/dto/CreateOrgRequest.java`, `UpdateOrgRequest.java`, `OrgResponse.java`
- [ ] T011 [US1] Implement `backend/organisation-service/src/main/java/com/saasstarterkit/org/service/OrganisationService.java` — `createOrg()`: validate tenant context, check slug uniqueness, persist, write audit log async
- [ ] T012 [US1] Implement `OrganisationService.updateOrg()` and `OrganisationService.archiveOrg()` (soft-delete: set deleted_at)
- [ ] T013 [US2] Implement `OrganisationService.listOrgs(tenantId)` and `OrganisationService.getOrg(id)` — exclude soft-deleted
- [ ] T014 [US1] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/controller/OrganisationController.java` with `POST`, `GET`, `PATCH`, `DELETE /api/v1/organizations` and `GET /api/v1/organizations/{id}`
- [ ] T015 [US1] Add OTel tracing and MDC tenantId logging in `OrganisationService`

**Checkpoint**: Org CRUD independently testable

---

## Phase 4: User Story 3 — Member Management (Priority: P2)

**Goal**: Org owners can add/remove members with role assignment and invite tracking.

**Independent Test**: POST /api/v1/organizations/{id}/members adds member with invited_by + invited_at; DELETE soft-deletes; accepted_at set on acceptance.

- [ ] T016 [P] [US3] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/dto/AddMemberRequest.java` and `MemberResponse.java`
- [ ] T017 [US3] Implement `OrganisationService.addMember(orgId, request)` — create `OrganisationMember`, write audit log, publish `MemberAddedEvent`
- [ ] T018 [US3] Implement `OrganisationService.removeMember(orgId, userId)` — soft-delete (set deleted_at), write audit log
- [ ] T019 [US3] Implement `OrganisationService.acceptInvite(orgId, userId)` — set accepted_at timestamp
- [ ] T020 [US3] Add `GET`, `POST /api/v1/organizations/{id}/members` and `DELETE /api/v1/organizations/{id}/members/{userId}` to `OrganisationController`

**Checkpoint**: Member management independently testable

---

## Phase 4b: User Story 5 — Team Management (Priority: P2) ⚠️ NON-NEGOTIABLE

**Goal**: Org admins create Teams within Orgs; users belong to Teams; hierarchy: Organisation→Teams→Users→Roles enforced.

**Independent Test**: POST /api/v1/organizations/{id}/teams creates team; POST members/{userId} adds user; GET /me/teams returns user's teams. Removing from team leaves org membership intact.

- [ ] T020b [P] [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/Team.java` JPA entity (id, org_id, name, slug, description, created_by, deleted_at)
- [ ] T020c [P] [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/entity/TeamMembership.java` JPA entity (team_id, user_id, role, joined_at, deleted_at)
- [ ] T020d [P] [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/repository/TeamRepository.java` and `TeamMembershipRepository.java`
- [ ] T020e [US5] Implement `backend/organisation-service/src/main/java/com/saasstarterkit/org/service/TeamService.java` — `createTeam(orgId, request)`, `addMember(teamId, userId, role)`, `removeMember(teamId, userId)` (soft-delete), `listTeamsForUser(userId, tenantId)`
- [ ] T020f [US5] Add `team_slugs` unique-within-org constraint in migration; add `team_memberships` table to `database/init/03-organizational-management-schema.sql`
- [ ] T020g [US5] Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/controller/TeamController.java` with: `POST /api/v1/organizations/{id}/teams`, `GET /api/v1/organizations/{id}/teams`, `POST /api/v1/organizations/{id}/teams/{teamId}/members`, `DELETE /api/v1/organizations/{id}/teams/{teamId}/members/{userId}`, `GET /api/v1/users/me/teams`

**Checkpoint**: Teams CRUD and membership independently testable

---

## Phase 5: User Story 4 — Enterprise Governance (Priority: P3)

**Goal**: Super-admin can enforce ENTERPRISE tier limits (custom max_users, api_rate_limit) and view onboarding status.

**Independent Test**: ENTERPRISE org enforces max_users limit; onboarding_step returned when onboarding_completed=false.

- [ ] T021 [P] [US4] Implement `OrganisationService.enforceUsageLimits(orgId)` — check max_users, storage_limit_gb, api_rate_limit against current usage
- [ ] T022 [US4] Add `onboarding_completed`, `onboarding_step` fields to `Organisation` entity (update migration)
- [ ] T023 [US4] Add `GET /api/v1/admin/organizations` (SUPER_ADMIN only, cross-tenant health view) to a new `AdminOrgController`

**Checkpoint**: Enterprise governance independently testable

---

## Phase 6a: User Data Erasure Consumer (Cross-Cutting)

**Goal**: organisation-service consumes `UserDataErasureEvent` and anonymises member records for the erased user.

**Independent Test**: Publish `UserDataErasureEvent` to Kafka → `OrganisationMember` rows for that userId have pii fields (email, name) overwritten with anonymised values; records retained for audit integrity.

- [ ] TErasure-004a Add `spring-kafka` to `backend/organisation-service/pom.xml`; configure Kafka brokers in `application.yml`
- [ ] TErasure-004b Create `backend/organisation-service/src/main/java/com/saasstarterkit/org/kafka/ErasureEventConsumer.java` — anonymise `OrganisationMember` PII fields (name, email) for the specified userId while preserving audit log entries

**Checkpoint**: Erasure consumer independently testable

---

## Phase 6: Polish & Cross-Cutting

- [ ] T024 [P] Add indexes on `organizations(tenant_id, deleted_at)` WHERE deleted_at IS NULL in migration
- [ ] T025 [P] Add Prometheus counter for org creation, member add/remove events
- [ ] T026 Run `quickstart.md` smoke tests (create, list, add member, remove member, archive)

---

## Dependencies & Execution Order

- **Phase 1** (T001–T003): No dependencies
- **Phase 2** (T004–T009): Depends on Phase 1
- **Phase 3** (T010–T015): Depends on Phase 2 — **MVP scope**
- **Phase 4** (T016–T020): Depends on Phase 2
- **Phase 4b** (T020b–T020g): Depends on Phase 2
- **Phase 5** (T021–T023): Depends on Phase 3
- **Phase 6** (T024–T026): Depends on all phases

### MVP Scope
Complete **Phase 1 + Phase 2 + Phase 3** for working org creation and listing.
