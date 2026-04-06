# Research: Organisation Management

## Decision: Split Responsibility — user-service + organisation-service

**Decision**: Basic org CRUD (create/list/get/update/delete) in user-service; enterprise governance (tiers, limits, compliance, onboarding) in organisation-service.
**Rationale**: Keeps user-service focused on user lifecycle. organisation-service handles complex governance without polluting basic CRUD. Clear service boundary.
**Alternatives considered**:
- Single service: Becomes too large; harder to scale governance features independently

## Decision: Soft-Delete for Orgs and Members

**Decision**: deleted_at timestamp; all default queries filter WHERE deleted_at IS NULL. Unique constraint uses partial index (WHERE deleted_at IS NULL).
**Rationale**: Audit trail preserved. Compliance requirement: deleted orgs may need to be referenced in financial records. GDPR erasure is a separate operation.

## Decision: Async Audit Logging

**Decision**: Org and member operations emit audit events asynchronously (Spring @Async or message queue). Audit records written by separate audit writer thread.
**Rationale**: Audit log write failure must NOT fail the business operation. Async ensures < 5 ms added latency per org operation.

## Decision: Risk Level Scoring on Audit Events

**Decision**: Every audit entry includes risk_level (LOW/MEDIUM/HIGH/CRITICAL) and compliance_category.
**Rationale**: Enables security dashboards to aggregate risk; supports compliance reporting per framework.

## Best Practices Confirmed

- Org tiers (STARTER/PROFESSIONAL/ENTERPRISE) stored on org record; limits enforced in service layer
- max_users enforced before member addition (current_users counter)
- Two-phase invitation: invited_at recorded on invite; accepted_at on acceptance
- Onboarding tracked as (onboarding_completed bool + onboarding_step string)
