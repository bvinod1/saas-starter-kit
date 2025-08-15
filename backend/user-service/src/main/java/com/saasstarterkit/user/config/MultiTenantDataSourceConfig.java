package com.saasstarterkit.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiTenantDataSourceConfig {

    @Bean
    @Primary
    public DataSource multiTenantDataSource() {
        // Create the default data source
        HikariDataSource defaultDataSource = new HikariDataSource();
        defaultDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/saas_starter_kit");
        defaultDataSource.setUsername("saas_user");
        defaultDataSource.setPassword("saas_password");
        defaultDataSource.setDriverClassName("org.postgresql.Driver");
        
        AbstractRoutingDataSource multiTenantDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                String tenantSchema = TenantContext.getCurrentTenantSchema();
                return tenantSchema != null ? tenantSchema : "public";
            }
        };

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("public", defaultDataSource);
        
        // For now, we'll use the same data source for all tenants
        // In a production environment, you might want to have separate data sources per tenant
        multiTenantDataSource.setTargetDataSources(targetDataSources);
        multiTenantDataSource.setDefaultTargetDataSource(defaultDataSource);
        
        return multiTenantDataSource;
    }
}
