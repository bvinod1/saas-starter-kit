package com.saasstarterkit.tenant.domain.model.enums;

/**
 * ConfigurationType - Defines the type of configuration value
 * 
 * This enum represents the different types of configuration values that can be stored
 * in the tenant configuration system.
 */
public enum ConfigurationType {
    
    /**
     * String configuration value
     */
    STRING("string"),
    
    /**
     * Integer configuration value
     */
    INTEGER("integer"),
    
    /**
     * Long configuration value
     */
    LONG("long"),
    
    /**
     * Double configuration value
     */
    DOUBLE("double"),
    
    /**
     * Boolean configuration value
     */
    BOOLEAN("boolean"),
    
    /**
     * JSON configuration value
     */
    JSON("json"),
    
    /**
     * Base64 encoded configuration value
     */
    BASE64("base64"),
    
    /**
     * Encrypted configuration value
     */
    ENCRYPTED("encrypted"),
    
    /**
     * URL configuration value
     */
    URL("url"),
    
    /**
     * Email configuration value
     */
    EMAIL("email"),
    
    /**
     * Phone number configuration value
     */
    PHONE("phone"),
    
    /**
     * Date configuration value
     */
    DATE("date"),
    
    /**
     * DateTime configuration value
     */
    DATETIME("datetime"),
    
    /**
     * File path configuration value
     */
    FILE_PATH("file_path"),
    
    /**
     * Database connection string configuration value
     */
    DB_CONNECTION("db_connection"),
    
    /**
     * API key configuration value
     */
    API_KEY("api_key"),
    
    /**
     * Secret configuration value
     */
    SECRET("secret"),
    
    /**
     * Certificate configuration value
     */
    CERTIFICATE("certificate"),
    
    /**
     * Custom configuration value
     */
    CUSTOM("custom");
    
    private final String value;
    
    ConfigurationType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static ConfigurationType fromValue(String value) {
        for (ConfigurationType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ConfigurationType: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
