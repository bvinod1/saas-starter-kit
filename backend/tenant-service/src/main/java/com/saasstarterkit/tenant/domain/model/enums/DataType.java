package com.saasstarterkit.tenant.domain.model.enums;

/**
 * DataType - Defines the data type for configuration values
 * 
 * This enum represents the different data types that can be used
 * for configuration values in the tenant configuration system.
 */
public enum DataType {
    
    /**
     * String data type
     */
    STRING("string"),
    
    /**
     * Integer data type
     */
    INTEGER("integer"),
    
    /**
     * Long data type
     */
    LONG("long"),
    
    /**
     * Double data type
     */
    DOUBLE("double"),
    
    /**
     * Float data type
     */
    FLOAT("float"),
    
    /**
     * Boolean data type
     */
    BOOLEAN("boolean"),
    
    /**
     * Date data type
     */
    DATE("date"),
    
    /**
     * DateTime data type
     */
    DATETIME("datetime"),
    
    /**
     * Time data type
     */
    TIME("time"),
    
    /**
     * JSON data type
     */
    JSON("json"),
    
    /**
     * XML data type
     */
    XML("xml"),
    
    /**
     * YAML data type
     */
    YAML("yaml"),
    
    /**
     * Base64 data type
     */
    BASE64("base64"),
    
    /**
     * Binary data type
     */
    BINARY("binary"),
    
    /**
     * URL data type
     */
    URL("url"),
    
    /**
     * Email data type
     */
    EMAIL("email"),
    
    /**
     * Phone data type
     */
    PHONE("phone"),
    
    /**
     * IP Address data type
     */
    IP_ADDRESS("ip_address"),
    
    /**
     * MAC Address data type
     */
    MAC_ADDRESS("mac_address"),
    
    /**
     * UUID data type
     */
    UUID("uuid"),
    
    /**
     * Hash data type
     */
    HASH("hash"),
    
    /**
     * Color data type
     */
    COLOR("color"),
    
    /**
     * File path data type
     */
    FILE_PATH("file_path"),
    
    /**
     * Directory path data type
     */
    DIRECTORY_PATH("directory_path"),
    
    /**
     * Environment variable data type
     */
    ENV_VAR("env_var"),
    
    /**
     * Secret data type
     */
    SECRET("secret"),
    
    /**
     * Certificate data type
     */
    CERTIFICATE("certificate"),
    
    /**
     * Private key data type
     */
    PRIVATE_KEY("private_key"),
    
    /**
     * Public key data type
     */
    PUBLIC_KEY("public_key"),
    
    /**
     * Custom data type
     */
    CUSTOM("custom");
    
    private final String value;
    
    DataType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static DataType fromValue(String value) {
        for (DataType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DataType: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
