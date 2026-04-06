# Research: Security & Compliance

## Decision: GDPR Consent Management in DB

**Decision**: data_consents table with consent_type, status lifecycle (PENDING->ACTIVE->WITHDRAWN/EXPIRED), legal_basis, retention_period.
**Rationale**: GDPR Art. 7 requires demonstrable consent with timestamp, version, and withdrawal support. DB-based consent provides audit trail and supports right-to-withdraw queries.

## Decision: Kafka UserDataErasureEvent for Right-to-Erasure

**Decision**: ERASURE data subject request triggers a Kafka event (UserDataErasureEvent). Every service consuming user data MUST implement a handler that purges PII for that userId.
**Rationale**: Microservices architecture means user data is distributed. Point-to-point erasure calls are brittle. Kafka event with at-least-once delivery + idempotent handlers ensures all services process erasure.

## Decision: AES-256 Encryption for PHI

**Decision**: protected_health_information.phi_data stored AES-256-CBC encrypted. SHA-256 hash stored alongside for integrity verification. Encryption key from SecretsProvider only.
**Rationale**: HIPAA Security Rule requires encryption of ePHI at rest (addressable but standard practice). AES-256 meets NIST requirements. Key separation prevents encrypted PHI exposure if DB is compromised.

## Decision: Risk-Level Security Alert Scoring

**Decision**: Security events scored 0-100 (risk_score). Severity mapped by score range: LOW (0-33), MEDIUM (34-66), HIGH (67-84), CRITICAL (85-100).
**Rationale**: Enables automated alerting thresholds and compliance dashboard aggregation. Risk score combines: sensitivity of data accessed, number of anomalous events, time-of-day, IP reputation.

## Best Practices Confirmed

- GDPR data retention: 7 years default (2555 days); configurable per tenant
- Account data anonymized on deletion (not deleted): pseudonymize first, then delete PII fields
- HIPAA minimum_necessary_standard flag enforced by PHI access audit
- SAST tools: SpotBugs + OWASP dependency-check in CI pipeline
- DAST: OWASP ZAP against staging environment before release
- Secrets rotation schedule: 90 days for API keys, 30 days for DB passwords
