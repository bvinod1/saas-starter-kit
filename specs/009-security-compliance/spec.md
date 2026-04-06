# Feature Specification: Security & Compliance

**Feature Branch**: `009-security-compliance`
**Created**: 2026-04-04
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — GDPR consent management (Priority: P1)

A user grants or withdraws consent for specific data processing categories.

**Acceptance Scenarios**:
1. **Given** new user, **When** registers, **Then** ESSENTIAL_SERVICES consent created as ACTIVE.
2. **Given** user withdraws MARKETING consent, **When** consent updated, **Then** status = WITHDRAWN, consent_withdrawn_at recorded.
3. **Given** consent expired, **When** checked, **Then** status = EXPIRED and user notified.

---

### User Story 2 — Data subject rights requests (Priority: P1)

A user submits a data access, erasure, or portability request. The system tracks it through completion.

**Acceptance Scenarios**:
1. **Given** user submits ERASURE request, **When** verified, **Then** all PII data deleted/anonymised within legal timeline.
2. **Given** DATA_PORTABILITY request, **When** processed, **Then** user data exported in JSON/CSV format.
3. **Given** ACCESS request, **When** fulfilled, **Then** all data held about user returned.

---

### User Story 3 — HIPAA PHI protection (Priority: P2)

Healthcare-enabled tenants store Protected Health Information with full encryption, access controls, and audit trails.

**Acceptance Scenarios**:
1. **Given** HIPAA-enabled tenant, **When** PHI record created, **Then** is_encrypted=true, data_hash stored.
2. **Given** PHI access, **When** audit log checked, **Then** entry records accessor, purpose, timestamp.
3. **Given** breach detected, **When** breach_notification_required=true, **Then** notification triggered within 72 hours.

---

### User Story 4 — Security event monitoring and alerting (Priority: P1)

The platform detects anomalous activity (brute force, unusual API patterns, unauthorised DB connections)
and raises alerts with severity scoring.

**Acceptance Scenarios**:
1. **Given** 5+ failed logins from same IP, **When** detected, **Then** HIGH-severity alert created, account optionally locked.
2. **Given** DB connection from non-whitelisted IP, **When** detected, **Then** CRITICAL alert with risk_score=95.
3. **Given** API endpoint accessed 50× above baseline, **When** detected, **Then** MEDIUM alert created.

---

### User Story 5 — Compliance score dashboard (Priority: P2)

An admin views compliance scores per framework (GDPR/HIPAA/SOX/PCI-DSS) with last/next scan dates.

**Acceptance Scenarios**:
1. **Given** admin, **When** compliance dashboard opened, **Then** score per framework shown (0–100%).
2. **Given** compliance issue detected, **When** listed, **Then** category, tenants affected, and remediation steps shown.

---

### Edge Cases
- What if a user requests erasure but data is required for legal hold?
- How is PHI encryption key rotation handled?
- What is the fallback when SAST scan fails in CI?

## Requirements

### Functional Requirements
- **FR-001**: System MUST implement GDPR consent management for all consent types.
- **FR-002**: System MUST support all 16 data subject request types (ACCESS, ERASURE, PORTABILITY, etc.).
- **FR-003**: System MUST broadcast `UserDataErasureEvent` via Kafka on ERASURE request; all services MUST handle it.
- **FR-004**: PHI MUST be stored in `protected_health_information` with AES-256 encryption and SHA-256 hash verification.
- **FR-005**: All PHI access MUST be logged in immutable audit trail.
- **FR-006**: Data classification levels MUST be enforced: PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED, SENSITIVE_PERSONAL, HEALTH_DATA.
- **FR-007**: Secrets MUST never be hardcoded; MUST go through SecretsProvider interface.
- **FR-008**: SAST, DAST, and dependency scanning MUST run in CI before every merge.
- **FR-009**: Accounts MUST be lockable after configurable failed login attempts.
- **FR-010**: Compliance frameworks MUST be selectable per tenant: GDPR, HIPAA, SOX, PCI-DSS.
- **FR-011**: Security alerts MUST be scored (0–100) with severity: CRITICAL, HIGH, MEDIUM, LOW.
- **FR-012**: MFA MUST be enforceable per user via `multi_factor_required` flag.

### Key Entities
- **DataConsent**: user_id, tenant_id, consent_type, status, consent_text, version, ip_address
- **DataSubjectRequest**: user_id, request_type, status, verification_completed, data_portability_format
- **ProtectedHealthInformation**: user_id, phi_category, phi_data (encrypted), data_hash, access_purpose
- **SecurityAlert**: title, severity, type, tenant, risk_score, status, assigned_to
- **ComplianceCheck**: framework, score, last_check, next_check, issues

### Non-Functional Requirements
- Erasure requests MUST complete within the legally required window (GDPR: 30 days)
- PHI decryption MUST require explicit access purpose logging
- Audit logs MUST be immutable in production
