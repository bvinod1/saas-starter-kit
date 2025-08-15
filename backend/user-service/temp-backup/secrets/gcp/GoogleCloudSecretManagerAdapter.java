package com.saasstarterkit.user.service.secrets.gcp;

import com.saasstarterkit.user.service.secrets.SecretManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.cloud.secretmanager.v1.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Google Cloud Secret Manager adapter implementing SecretManagerService.
 * Provides secure access to GCP Secret Manager with caching and error handling.
 */
@Service
public class GoogleCloudSecretManagerAdapter implements SecretManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudSecretManagerAdapter.class);
    
    @Value("${gcp.secrets.manager.project-id}")
    private String projectId;
    
    @Value("${gcp.secrets.manager.cache.ttl:300}")
    private long cacheTtlSeconds;
    
    @Value("${gcp.secrets.manager.cache.max-size:1000}")
    private int maxCacheSize;
    
    @Value("${gcp.secrets.manager.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${gcp.secrets.manager.retry.delay-ms:1000}")
    private long retryDelayMs;
    
    private SecretManagerServiceClient secretManagerClient;
    private final Map<String, CachedSecret> secretCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    @PostConstruct
    public void initialize() {
        try {
            // Initialize Google Cloud Secret Manager client
            secretManagerClient = SecretManagerServiceClient.create();
            
            // Schedule cache cleanup
            cacheCleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredCache,
                cacheTtlSeconds,
                cacheTtlSeconds,
                TimeUnit.SECONDS
            );
            
            logger.info("Google Cloud Secret Manager adapter initialized successfully for project: {}", projectId);
            
        } catch (IOException e) {
            logger.error("Failed to initialize Google Cloud Secret Manager adapter", e);
            throw new RuntimeException("Google Cloud Secret Manager initialization failed", e);
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
        
        if (secretManagerClient != null) {
            secretManagerClient.close();
        }
        
        logger.info("Google Cloud Secret Manager adapter cleaned up");
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
            
            // Fetch from Google Cloud Secret Manager
            String secretValue = fetchSecretFromGcp(key);
            
            // Cache the result
            cacheSecret(key, secretValue);
            
            logger.debug("Retrieved secret from GCP: {}", key);
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
            String parent = ProjectName.of(projectId).toString();
            ListSecretsRequest listRequest = ListSecretsRequest.newBuilder()
                    .setParent(parent)
                    .setFilter("name:" + path + "/")
                    .build();
            
            ListSecretsResponse listResponse = secretManagerClient.listSecrets(listRequest);
            
            // Fetch each secret
            for (Secret secret : listResponse.getSecretsList()) {
                String secretName = secret.getName();
                try {
                    String secretValue = getSecret(secretName);
                    // Extract the key part after the path
                    String key = secretName.substring(secretName.lastIndexOf("/") + 1);
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
     * Fetches a secret from Google Cloud Secret Manager with retry logic.
     */
    private String fetchSecretFromGcp(String secretName) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                // Access the latest version of the secret
                String secretVersionName = secretName + "/versions/latest";
                AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                        .setName(secretVersionName)
                        .build();
                
                AccessSecretVersionResponse response = secretManagerClient.accessSecretVersion(request);
                
                if (response.getPayload().getData() != null) {
                    return response.getPayload().getData().toStringUtf8();
                } else {
                    throw new RuntimeException("Secret has no data");
                }
                
            } catch (NotFoundException e) {
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
            String parent = ProjectName.of(projectId).toString();
            ListSecretsRequest request = ListSecretsRequest.newBuilder()
                    .setParent(parent)
                    .setPageSize(1)
                    .build();
            
            secretManagerClient.listSecrets(request);
            return true;
        } catch (Exception e) {
            logger.warn("Google Cloud Secret Manager health check failed", e);
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
        stats.put("projectId", projectId);
        stats.put("healthy", isHealthy());
        return stats;
    }
}
