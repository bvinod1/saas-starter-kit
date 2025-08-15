package com.saasstarterkit.user.service.secrets;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Generic interface for secret manager adapters.
 * This interface provides a unified way to interact with different secret management systems
 * such as AWS Secrets Manager, HashiCorp Vault, Azure Key Vault, etc.
 */
public interface SecretManager {
    
    /**
     * Retrieves a secret value by its key.
     * 
     * @param secretKey The unique identifier for the secret
     * @return Optional containing the secret value if found
     */
    Optional<String> getSecret(String secretKey);
    
    /**
     * Retrieves a secret value by its key asynchronously.
     * 
     * @param secretKey The unique identifier for the secret
     * @return CompletableFuture containing the secret value
     */
    CompletableFuture<Optional<String>> getSecretAsync(String secretKey);
    
    /**
     * Retrieves multiple secrets by their keys.
     * 
     * @param secretKeys Array of secret keys to retrieve
     * @return Map of secret keys to their values (only successful retrievals)
     */
    Map<String, String> getSecrets(String... secretKeys);
    
    /**
     * Retrieves multiple secrets by their keys asynchronously.
     * 
     * @param secretKeys Array of secret keys to retrieve
     * @return CompletableFuture containing map of secret keys to their values
     */
    CompletableFuture<Map<String, String>> getSecretsAsync(String... secretKeys);
    
    /**
     * Stores a new secret or updates an existing one.
     * 
     * @param secretKey The unique identifier for the secret
     * @param secretValue The secret value to store
     * @return true if the operation was successful
     */
    boolean putSecret(String secretKey, String secretValue);
    
    /**
     * Stores a new secret or updates an existing one asynchronously.
     * 
     * @param secretKey The unique identifier for the secret
     * @param secretValue The secret value to store
     * @return CompletableFuture containing the operation result
     */
    CompletableFuture<Boolean> putSecretAsync(String secretKey, String secretValue);
    
    /**
     * Stores multiple secrets in a batch operation.
     * 
     * @param secrets Map of secret keys to their values
     * @return true if all operations were successful
     */
    boolean putSecrets(Map<String, String> secrets);
    
    /**
     * Stores multiple secrets in a batch operation asynchronously.
     * 
     * @param secrets Map of secret keys to their values
     * @return CompletableFuture containing the operation result
     */
    CompletableFuture<Boolean> putSecretsAsync(Map<String, String> secrets);
    
    /**
     * Deletes a secret by its key.
     * 
     * @param secretKey The unique identifier for the secret to delete
     * @return true if the deletion was successful
     */
    boolean deleteSecret(String secretKey);
    
    /**
     * Deletes a secret by its key asynchronously.
     * 
     * @param secretKey The unique identifier for the secret to delete
     * @return CompletableFuture containing the operation result
     */
    CompletableFuture<Boolean> deleteSecretAsync(String secretKey);
    
    /**
     * Checks if a secret exists.
     * 
     * @param secretKey The unique identifier for the secret
     * @return true if the secret exists
     */
    boolean secretExists(String secretKey);
    
    /**
     * Lists all available secret keys (if supported by the implementation).
     * 
     * @return Array of available secret keys
     */
    String[] listSecretKeys();
    
    /**
     * Lists all available secret keys asynchronously.
     * 
     * @return CompletableFuture containing array of available secret keys
     */
    CompletableFuture<String[]> listSecretKeysAsync();
    
    /**
     * Gets metadata about a secret (creation date, last modified, etc.).
     * 
     * @param secretKey The unique identifier for the secret
     * @return Optional containing secret metadata
     */
    Optional<SecretMetadata> getSecretMetadata(String secretKey);
    
    /**
     * Gets metadata about a secret asynchronously.
     * 
     * @param secretKey The unique identifier for the secret
     * @return CompletableFuture containing secret metadata
     */
    CompletableFuture<Optional<SecretMetadata>> getSecretMetadataAsync(String secretKey);
    
    /**
     * Rotates a secret value (if supported by the implementation).
     * 
     * @param secretKey The unique identifier for the secret to rotate
     * @return true if the rotation was successful
     */
    boolean rotateSecret(String secretKey);
    
    /**
     * Rotates a secret value asynchronously.
     * 
     * @param secretKey The unique identifier for the secret to rotate
     * @return CompletableFuture containing the operation result
     */
    CompletableFuture<Boolean> rotateSecretAsync(String secretKey);
    
    /**
     * Refreshes the connection or cache (if applicable).
     * 
     * @return true if the refresh was successful
     */
    boolean refresh();
    
    /**
     * Refreshes the connection or cache asynchronously.
     * 
     * @return CompletableFuture containing the operation result
     */
    CompletableFuture<Boolean> refreshAsync();
    
    /**
     * Gets the health status of the secret manager.
     * 
     * @return SecretManagerHealth containing status information
     */
    SecretManagerHealth getHealth();
    
    /**
     * Gets the health status of the secret manager asynchronously.
     * 
     * @return CompletableFuture containing health status information
     */
    CompletableFuture<SecretManagerHealth> getHealthAsync();
    
    /**
     * Closes the connection to the secret manager.
     */
    void close();
    
    /**
     * Checks if the secret manager is connected and operational.
     * 
     * @return true if the secret manager is healthy and operational
     */
    boolean isHealthy();
}
