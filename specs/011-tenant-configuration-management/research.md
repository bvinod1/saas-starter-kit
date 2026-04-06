# Research: Tenant Configuration Management

## Decision: Hierarchical Fallback Config Resolution

**Decision**: Three-tier hierarchy: (1) tenant+key+env specific → (2) key+env global → (3) key+default global.
**Rationale**: Allows platform defaults to be overridden per environment and further per tenant. Reduces config duplication for common settings.

## Decision: AES-256 Encryption for Sensitive Config

**Decision**: Config values with is_encrypted=true stored AES-256-CBC encrypted. Decrypted transparently on read.
**Rationale**: API keys, webhook secrets, SMTP passwords must not be stored in plaintext. Encryption key sourced from SecretsProvider, never from application config files.

## Decision: Spring Cache (Redis) for Config Reads

**Decision**: @Cacheable(value="tenantConfigs", key="#tenantId + '_' + #configKey + '_' + #environment") accelerates reads.
**Rationale**: Config is read-heavy (every request may need 1-3 config values). DB round-trip on every request at 1000 req/s = 3000 DB queries/sec just for config. Cache reduces to near zero.

## Decision: Type-Safe Retrieval

**Decision**: Overloaded getConfiguration(tenantId, key, env, Class<T>) returns typed values.
**Rationale**: Prevents type confusion bugs. Integer config used as Integer, not parsed at call site. Simplifies calling code.

## Best Practices Confirmed

- Cache key includes environment to prevent staging config leaking into production reads
- Encryption key stored in Vault/SecretsManager; injected at service startup via SecretsProvider
- AES key ID stored with encrypted value for key rotation support (decrypt old, re-encrypt with new key)
- Config updates must call CacheManager.evictIfPresent(cacheKey) before returning success
- JSONB type for complex objects: stored as JSON string, deserialized via ObjectMapper
