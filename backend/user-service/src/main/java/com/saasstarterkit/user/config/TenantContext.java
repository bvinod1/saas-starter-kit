package com.saasstarterkit.user.config;

import org.springframework.stereotype.Component;

@Component
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTenantSchema = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantSlug) {
        currentTenant.set(tenantSlug);
        if (tenantSlug != null) {
            currentTenantSchema.set("tenant_" + tenantSlug);
        } else {
            currentTenantSchema.set(null);
        }
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static String getCurrentTenantSchema() {
        return currentTenantSchema.get();
    }

    public static void clear() {
        currentTenant.remove();
        currentTenantSchema.remove();
    }
}
