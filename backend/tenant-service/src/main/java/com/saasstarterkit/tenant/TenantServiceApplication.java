package com.saasstarterkit.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tenant Service - Centralized configuration management for multi-tenant SaaS platform
 * 
 * This service provides:
 * - Tenant-aware configuration storage and retrieval
 * - Dynamic configuration loading and runtime refresh
 * - Secure, isolated access with RBAC
 * - Configuration versioning and staged rollouts
 * - Self-service tenant customization
 * - Environment and tier support
 * - Compliance and backup capabilities
 * - Monitoring and alerting
 * - API enablement for programmatic access
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@RefreshScope
public class TenantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantServiceApplication.class, args);
    }
}
