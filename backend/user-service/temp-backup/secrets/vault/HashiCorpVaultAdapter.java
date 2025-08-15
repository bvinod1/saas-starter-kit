package com.saasstarterkit.user.service.secrets.vault;

import com.saasstarterkit.user.service.secrets.SecretManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HashiCorp Vault adapter implementing SecretManagerService.
 * Provides secure access to HashiCorp Vault for local/development use.
 */
@Service
public class HashiCorpVaultAdapter implements SecretManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(HashiCorpVaultAdapter.class);
    
    @Value("${vault.url:http://localhost:8200}")
    private String vaultUrl;
    
    @Value("${vault.token}")
    private String vaultToken;
    
    @Value("${vault.namespace:}")
    private String vaultNamespace;
    
    @Value("${vault.cache.ttl:300}")
    private long cacheTtlSeconds;
    
    @Value("${vault.cache.max-size:1000}")
    private int maxCacheSize;
    
    @Value("${vault.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${vault.retry.delay-ms:1000}")
    private long retryDelayMs;
    
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private final Map<String, CachedSecret> secretCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    @PostConstruct
    public void initialize() {
        try {
            // Initialize HTTP client and JSON mapper
            restTemplate = new RestTemplate();
            objectMapper = new ObjectMapper();
            
            // Schedule cache cleanup
            cacheCleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredCache,
                cacheTtlSeconds,
                cacheTtlSeconds,
                TimeUnit.SECONDS
            );
            
            // Test connection to Vault
            testVaultConnection();
            
            logger.info("HashiCorp Vault adapter initialized successfully for vault: {}", vaultUrl);
            
        } catch (Exception e) {
            logger.error("Failed to initialize HashiCorp Vault adapter", e);
            throw new RuntimeException("HashiCorp Vault initialization failed", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (cacheCleanupExecutor != null) {
            cacheCleanupExecutor.shutdown();
            try {
                if (!cacheCleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cacheCleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cacheCleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("HashiCorp Vault adapter cleaned up");
    }
    
    @Override
    public String getSecret(String key) {
        try {
            // Check cache first
            CachedSecret cached = secretCache.get(key);
            if (cached != null && !cached.isExpired()) {
                logger.debug("Retrieved secret from cache: {}", key);
                return cached.getValue();
            }
            
            // Fetch from HashiCorp Vault
            String secretValue = fetchSecretFromVault(key);
            
            // Cache the result
            cacheSecret(key, secretValue);
            
            logger.debug("Retrieved secret from Vault: {}", key);
            return secretValue;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve secret: {}", key, e);
            throw new RuntimeException("Secret retrieval failed for key: " + key, e);
        }
    }
    
    @Override
    public Map<String, String> getSecretsByPath(String path) {
        try {
            Map<String, String> secrets = new HashMap<>();
            
            // List secrets at the given path
            String listPath = "/v1/secret/metadata/" + path;
            HttpHeaders headers = createVaultHeaders();
            
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    vaultUrl + listPath,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode responseNode = objectMapper.readTree(response.getBody());
                    JsonNode keysNode = responseNode.get("data").get("keys");
                    
                    if (keysNode != null && keysNode.isArray()) {
                        for (JsonNode keyNode : keysNode) {
                            String secretKey = keyNode.asText();
                            try {
                                String secretValue = getSecret(path + "/" + secretKey);
                                secrets.put(secretKey, secretValue);
                            } catch (Exception e) {
                                logger.warn("Failed to fetch secret: {}/{}", path, secretKey, e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed to list secrets at path: {}, falling back to individual fetch", path, e);
                
                // Fallback: try to fetch common secret names
                String[] commonSecretNames = {
                    path + "/database-password",
                    path + "/api-key",
                    path + "/jwt-secret",
                    path + "/encryption-key"
                };
                
                for (String secretName : commonSecretNames) {
                    try {
                        String secretValue = getSecret(secretName);
                        String key = secretName.substring(path.length() + 1);
                        secrets.put(key, secretValue);
                    } catch (Exception ex) {
                        logger.debug("Secret not found: {}", secretName);
                    }
                }
            }
            
            logger.debug("Retrieved {} secrets from path: {}", secrets.size(), path);
            return secrets;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve secrets by path: {}", path, e);
            throw new RuntimeException("Path-based secret retrieval failed for: " + path, e);
        }
    }
    
    /**
     * Fetches a secret from HashiCorp Vault with retry logic.
     */
    private String fetchSecretFromVault(String secretPath) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                String apiPath = "/v1/secret/data/" + secretPath;
                HttpHeaders headers = createVaultHeaders();
                
                ResponseEntity<String> response = restTemplate.exchange(
                    vaultUrl + apiPath,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode responseNode = objectMapper.readTree(response.getBody());
                    JsonNode dataNode = responseNode.get("data").get("data");
                    
                    if (dataNode != null && dataNode.has("value")) {
                        return dataNode.get("value").asText();
                    } else {
                        throw new RuntimeException("Secret has no value field");
                    }
                } else {
                    throw new RuntimeException("Failed to retrieve secret: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Attempt {} failed to fetch secret: {}", attempt, secretPath, e);
                
                if (attempt < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryDelayMs * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Secret retrieval interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed to fetch secret after " + maxRetryAttempts + " attempts: " + secretPath, lastException);
    }
    
    /**
     * Creates Vault-specific HTTP headers.
     */
    private HttpHeaders createVaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultToken);
        
        if (vaultNamespace != null && !vaultNamespace.trim().isEmpty()) {
            headers.set("X-Vault-Namespace", vaultNamespace);
        }
        
        return headers;
    }
    
    /**
     * Tests the connection to Vault.
     */
    private void testVaultConnection() {
        try {
            HttpHeaders headers = createVaultHeaders();
            ResponseEntity<String> response = restTemplate.exchange(
                vaultUrl + "/v1/sys/health",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Vault connection test successful");
            } else {
                logger.warn("Vault connection test returned status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.warn("Vault connection test failed: {}", e.getMessage());
        }
    }
    
    /**
     * Caches a secret with TTL.
     */
    private void cacheSecret(String key, String value) {
        // Implement cache size limit
        if (secretCache.size() >= maxCacheSize) {
            cleanupExpiredCache();
            if (secretCache.size() >= maxCacheSize) {
                // Remove oldest entry if still at limit
                String oldestKey = secretCache.keySet().iterator().next();
                secretCache.remove(oldestKey);
            }
        }
        
        secretCache.put(key, new CachedSecret(value, System.currentTimeMillis() + (cacheTtlSeconds * 1000)));
    }
    
    /**
     * Cleans up expired cache entries.
     */
    private void cleanupExpiredCache() {
        long now = System.currentTimeMillis();
        secretCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }
    
    /**
     * Cached secret with expiration.
     */
    private static class CachedSecret {
        private final String value;
        private final long expirationTime;
        
        public CachedSecret(String value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        public String getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }
        
        public boolean isExpired(long currentTime) {
            return currentTime > expirationTime;
        }
    }
    
    /**
     * Health check method for monitoring.
     */
    public boolean isHealthy() {
        try {
            HttpHeaders headers = createVaultHeaders();
            ResponseEntity<String> response = restTemplate.exchange(
                vaultUrl + "/v1/sys/health",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("Vault health check failed", e);
            return false;
        }
    }
    
    /**
     * Gets cache statistics for monitoring.
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", secretCache.size());
        stats.put("maxCacheSize", maxCacheSize);
        stats.put("cacheTtlSeconds", cacheTtlSeconds);
        stats.put("vaultUrl", vaultUrl);
        stats.put("vaultNamespace", vaultNamespace);
        stats.put("healthy", isHealthy());
        return stats;
    }
}
