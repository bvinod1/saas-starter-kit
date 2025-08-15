package com.saasstarterkit.user.service.secrets;

import java.util.Map;

/**
 * Simple service interface for retrieving secrets from a secret management system.
 * This interface provides basic functionality for accessing secrets by key or path.
 */
public interface SecretManagerService {
    
    /**
     * Retrieve a single secret by a unique key or path.
     * @param key Unique secret identifier or path.
     * @return The secret value as a string.
     */
    String getSecret(String key);
    
    /**
     * Retrieve multiple secrets residing under a given path or namespace.
     * @param path Namespace or prefix where secrets are stored.
     * @return A map/dictionary of key-value pairs of secrets.
     */
    Map<String, String> getSecretsByPath(String path);
}
