# Data Model: Security & Compliance

## Entity: ConsentRecord (GDPR)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID | |
| tenant_id | UUID | |
| consent_type | VARCHAR(60) | MARKETING, ANALYTICS, COOKIES |
| consented | BOOLEAN | |
| version | VARCHAR(20) | Policy version string |
| consented_at | TIMESTAMPTZ | |
| ip_address | INET | |
| user_agent | TEXT | |

## Entity: DataErasureRequest (GDPR Right-to-Erasure)
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| user_id | UUID | |
| tenant_id | UUID | |
| requested_at | TIMESTAMPTZ | |
| completed_at | TIMESTAMPTZ | Nullable |
| status | erasure_status ENUM | PENDING, IN_PROGRESS, COMPLETED, FAILED |
| services_notified | TEXT[] | Services that received event |

## Entity: SecurityEvent
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| user_id | UUID | Nullable |
| event_type | VARCHAR(80) | FAILED_LOGIN, BRUTE_FORCE, PRIVILEGE_ESCALATION, … |
| severity | VARCHAR(10) | LOW, MEDIUM, HIGH, CRITICAL |
| source_ip | INET | |
| details | JSONB | |
| occurred_at | TIMESTAMPTZ | |

## Entity: EncryptedPHIField (HIPAA)
Marker for fields encrypted with AES-256-GCM before persistence.

| Column pattern | Encryption |
|----------------|------------|
| `*_phi` | AES-256-GCM, key in Vault |
| `ssn`, `dob`, `diagnosis` | AES-256-GCM |

## Relationships
```
User 1 ──< ConsentRecord
User 1 ──< DataErasureRequest
Tenant 1 ──< SecurityEvent
```

## Indexes
- `consent_records(user_id, tenant_id, consent_type)` UNIQUE per version
- `data_erasure_requests(user_id, status)`
- `security_events(tenant_id, severity, occurred_at)`

## State Transitions (DataErasureRequest)
```
PENDING → IN_PROGRESS (saga coordinator picks up)
IN_PROGRESS → COMPLETED (all services ack)
IN_PROGRESS → FAILED (timeout/error)
FAILED → IN_PROGRESS (retry)
```

## Migration References
- `database/migrations/05-gdpr-hipaa-compliance.sql`
