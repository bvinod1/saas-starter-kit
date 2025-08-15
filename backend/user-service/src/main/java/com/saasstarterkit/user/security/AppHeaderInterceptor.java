package com.saasstarterkit.user.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.saasstarterkit.user.dto.Tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AppHeaderInterceptor extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
        FilterChain filterChain)
            throws ServletException, IOException {
        
        String appname = request.getHeader("app-name");
        Tenant.setAppname(appname);
        
        filterChain.doFilter(request, response);
    }

}