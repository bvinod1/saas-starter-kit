# Data Model: Subscription & Billing Management

## Entity: SubscriptionPlan
| Field | Type | Notes |
|-------|------|-------|
| id | VARCHAR(50) (PK) | e.g. `starter`, `pro`, `enterprise` |
| name | VARCHAR(100) | Display name |
| price_monthly_cents | INTEGER | 0 = free |
| price_annual_cents | INTEGER | |
| features | JSONB | Feature entitlement map |
| limits | JSONB | User/API quotas |
| active | BOOLEAN | |

## Entity: Subscription
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID (FK → tenants.id) UNIQUE | |
| plan_id | VARCHAR(50) (FK → plans.id) | |
| status | sub_status ENUM | TRIAL, ACTIVE, PAST_DUE, CANCELLED, EXPIRED |
| billing_cycle | VARCHAR(10) | MONTHLY, ANNUAL |
| current_period_start | TIMESTAMPTZ | |
| current_period_end | TIMESTAMPTZ | |
| trial_ends_at | TIMESTAMPTZ | Nullable |
| external_subscription_id | VARCHAR(200) | Provider reference |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

## Entity: Invoice
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| subscription_id | UUID (FK) | |
| amount_cents | INTEGER | |
| currency | CHAR(3) | ISO 4217 |
| status | invoice_status ENUM | DRAFT, OPEN, PAID, VOID, UNCOLLECTIBLE |
| due_date | DATE | |
| paid_at | TIMESTAMPTZ | |
| external_invoice_id | VARCHAR(200) | |
| created_at | TIMESTAMPTZ | |

## Entity: PaymentMethod
| Field | Type | Notes |
|-------|------|-------|
| id | UUID (PK) | |
| tenant_id | UUID | |
| provider_token | VARCHAR(300) | Tokenized; never raw card |
| type | VARCHAR(30) | CARD, BANK_TRANSFER |
| last4 | CHAR(4) | |
| is_default | BOOLEAN | |
| created_at | TIMESTAMPTZ | |

## Relationships
```
SubscriptionPlan 1 ──< Subscription
Tenant 1 ──1 Subscription
Tenant 1 ──< Invoice
Tenant 1 ──< PaymentMethod
Subscription 1 ──< Invoice
```

## Indexes
- `subscriptions(tenant_id)` UNIQUE
- `subscriptions(status, current_period_end)` – renewal sweep
- `invoices(tenant_id, status)`
- `invoices(external_invoice_id)` UNIQUE

## State Transitions (Subscription)
```
TRIAL → ACTIVE (payment collected)
ACTIVE → PAST_DUE (payment fails)
PAST_DUE → ACTIVE (retry succeeds)
PAST_DUE → CANCELLED (grace period expires)
ACTIVE → CANCELLED (user cancel)
CANCELLED → EXPIRED (period ends)
```

## Migration References
- `database/migrations/04-subscription-management.sql`
