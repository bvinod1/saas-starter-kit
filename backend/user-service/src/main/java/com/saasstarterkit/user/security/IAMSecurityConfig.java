/*******************************************************************************
 *
 * Copyright (c) 2020-21 Mindsprint Limited
 *
 * All information contained herein is, and remains the property of Mindsprint
 * Limited. The intellectual and technical concepts contained herein are
 * proprietary to Mindsprint and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from Mindsprint
 * Limited
 *
 *******************************************************************************/
package com.saasstarterkit.user.security;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * 
 * @author Mohankumar.D
 *
 */
@Configuration
@EnableWebSecurity
public class IAMSecurityConfig {

	@Autowired
	Environment env;
	
    @Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.addFilterBefore(new AppHeaderInterceptor(), ChannelProcessingFilter.class)
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated()).oauth2ResourceServer(
						oauth2 -> oauth2.opaqueToken(opaqueToken -> opaqueToken.introspector(introspector())));

		http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();

	}

    @Bean
    public OpaqueTokenIntrospector introspector() {
        return new JwtOpaqueTokenIntrospector();
    }

    @Bean
    public AppHeaderInterceptor interceptor(){
        return new AppHeaderInterceptor();
    }

	@Bean
	@Primary
	public KeycloakConfigResolver keycloakConfigResolver() {
		return new org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver();
	}

	@Bean
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new NullAuthenticatedSessionStrategy();
	}

	@Bean
	@ConditionalOnMissingBean(HttpSessionManager.class)
	protected HttpSessionManager httpSessionManager() {
		return new HttpSessionManager();
	}

	@Bean
	public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
		return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
	}
}