package com.saasstarterkit.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class SchemaInterceptor implements HandlerInterceptor {
    
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantSchema = TenantContext.getCurrentTenantSchema();
        
        if (tenantSchema != null) {
            try {
                // Set the search path to include the tenant schema
                Connection connection = getCurrentConnection();
                if (connection != null) {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("SET search_path TO " + tenantSchema + ", shared, tenant_management, public");
                    }
                }
            } catch (SQLException e) {
                // Log the error but don't fail the request
                System.err.println("Failed to set schema: " + e.getMessage());
            }
        }
        
        return true;
    }

    private Connection getCurrentConnection() {
        // This is a simplified version. In a real implementation,
        // you would get the connection from your data source
        try {
            return javax.sql.DataSource.class.cast(
                applicationContext.getBean("multiTenantDataSource")
            ).getConnection();
        } catch (Exception e) {
            return null;
        }
    }
}
