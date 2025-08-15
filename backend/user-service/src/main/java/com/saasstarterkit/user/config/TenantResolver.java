package com.saasstarterkit.user.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantResolver implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String tenantSlug = extractTenantFromJwt(jwt);
            
            if (tenantSlug != null) {
                TenantContext.setCurrentTenant(tenantSlug);
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private String extractTenantFromJwt(Jwt jwt) {
        // Try to get tenant_slug from JWT claims
        String tenantSlug = jwt.getClaimAsString("tenant_slug");
        
        if (tenantSlug == null) {
            // Fallback to tenant_id if tenant_slug is not available
            String tenantId = jwt.getClaimAsString("tenant_id");
            if (tenantId != null) {
                // You might want to look up the tenant slug from tenant_id
                // For now, we'll use tenant_id as slug
                tenantSlug = tenantId;
            }
        }
        
        return tenantSlug;
    }
}
