package com.saasstarterkit.user.service.secrets.aws;

import com.saasstarterkit.user.service.secrets.SecretManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AWS Secrets Manager adapter implementing SecretManagerService.
 * Provides secure access to AWS Secrets Manager with caching and error handling.
 */
@Service
public class AwsSecretsManagerAdapter implements SecretManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(AwsSecretsManagerAdapter.class);
    
    @Value("${aws.secrets.manager.region:us-east-1}")
    private String region;
    
    @Value("${aws.secrets.manager.cache.ttl:300}")
    private long cacheTtlSeconds;
    
    @Value("${aws.secrets.manager.cache.max-size:1000}")
    private int maxCacheSize;
    
    @Value("${aws.secrets.manager.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${aws.secrets.manager.retry.delay-ms:1000}")
    private long retryDelayMs;
    
    private SecretsManagerClient secretsClient;
    private final Map<String, CachedSecret> secretCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    @PostConstruct
    public void initialize() {
        try {
            // Initialize AWS Secrets Manager client
            secretsClient = SecretsManagerClient.builder()
                    .region(software.amazon.awssdk.regions.Region.of(region))
                    .build();
            
            // Schedule cache cleanup
            cacheCleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredCache,
                cacheTtlSeconds,
                cacheTtlSeconds,
                TimeUnit.SECONDS
            );
            
            logger.info("AWS Secrets Manager adapter initialized successfully for region: {}", region);
            
        } catch (Exception e) {
            logger.error("Failed to initialize AWS Secrets Manager adapter", e);
            throw new RuntimeException("AWS Secrets Manager initialization failed", e);
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
        
        if (secretsClient != null) {
            secretsClient.close();
        }
        
        logger.info("AWS Secrets Manager adapter cleaned up");
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
            
            // Fetch from AWS Secrets Manager
            String secretValue = fetchSecretFromAws(key);
            
            // Cache the result
            cacheSecret(key, secretValue);
            
            logger.debug("Retrieved secret from AWS: {}", key);
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
            
            // List secrets with the given path prefix
            ListSecretsRequest listRequest = ListSecretsRequest.builder()
                    .filters(Filter.builder()
                            .key(FilterNameStringType.NAME)
                            .values(path + "/")
                            .build())
                    .build();
            
            ListSecretsResponse listResponse = secretsClient.listSecrets(listRequest);
            
            // Fetch each secret
            for (SecretListEntry secretEntry : listResponse.secretList()) {
                String secretName = secretEntry.name();
                try {
                    String secretValue = getSecret(secretName);
                    // Extract the key part after the path
                    String key = secretName.substring(path.length() + 1);
                    secrets.put(key, secretValue);
                } catch (Exception e) {
                    logger.warn("Failed to fetch secret: {}", secretName, e);
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
     * Fetches a secret from AWS Secrets Manager with retry logic.
     */
    private String fetchSecretFromAws(String secretName) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                GetSecretValueRequest request = GetSecretValueRequest.builder()
                        .secretId(secretName)
                        .build();
                
                GetSecretValueResponse response = secretsClient.getSecretValue(request);
                
                if (response.secretString() != null) {
                    return response.secretString();
                } else if (response.secretBinary() != null) {
                    return new String(response.secretBinary().asByteArray());
                } else {
                    throw new RuntimeException("Secret has no string or binary value");
                }
                
            } catch (ResourceNotFoundException e) {
                logger.error("Secret not found: {}", secretName);
                throw new RuntimeException("Secret not found: " + secretName, e);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Attempt {} failed to fetch secret: {}", attempt, secretName, e);
                
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
        
        throw new RuntimeException("Failed to fetch secret after " + maxRetryAttempts + " attempts: " + secretName, lastException);
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
            // Simple health check by listing secrets (limited to 1)
            ListSecretsRequest request = ListSecretsRequest.builder()
                    .maxResults(1)
                    .build();
            
            secretsClient.listSecrets(request);
            return true;
        } catch (Exception e) {
            logger.warn("AWS Secrets Manager health check failed", e);
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
        stats.put("healthy", isHealthy());
        return stats;
    }
}
