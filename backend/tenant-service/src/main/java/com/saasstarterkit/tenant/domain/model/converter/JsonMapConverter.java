package com.saasstarterkit.tenant.domain.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JsonMapConverter - Converts between JSON string and Map for JPA entities
 * 
 * This converter handles the conversion between JSON strings stored in the database
 * and Map objects used in Java entities.
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonMapConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Error converting Map to JSON string", e);
            throw new RuntimeException("Error converting Map to JSON string", e);
        }
    }
    
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            logger.error("Error converting JSON string to Map", e);
            // Return empty map instead of throwing exception to prevent application crashes
            return new HashMap<>();
        }
    }
    
    /**
     * Convert a Map to a JSON string without throwing exceptions
     * 
     * @param map the map to convert
     * @return JSON string or null if conversion fails
     */
    public static String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            logger.error("Error converting Map to JSON string", e);
            return null;
        }
    }
    
    /**
     * Convert a JSON string to a Map without throwing exceptions
     * 
     * @param json the JSON string to convert
     * @return Map or empty HashMap if conversion fails
     */
    public static Map<String, Object> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            logger.error("Error converting JSON string to Map", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Check if a string is valid JSON
     * 
     * @param json the string to validate
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Get the ObjectMapper instance used by this converter
     * 
     * @return the ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
