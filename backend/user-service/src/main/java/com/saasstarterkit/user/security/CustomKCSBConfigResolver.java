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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.saasstarterkit.user.config.IAMCredentials;
import com.saasstarterkit.user.config.IAMDeployment;
import com.saasstarterkit.user.exception.IAMConnectorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * @author Mohankumar.D
 *
 */
public class CustomKCSBConfigResolver extends KeycloakSpringBootConfigResolver {
	
	@Autowired
	Environment env;

    @SuppressWarnings("unused")
	private final KeycloakDeployment keycloakDeployment;

    private final ConcurrentHashMap<String, KeycloakDeployment> deploymentMap = new ConcurrentHashMap<>();

    public CustomKCSBConfigResolver(KeycloakSpringBootProperties properties) {
        keycloakDeployment = KeycloakDeploymentBuilder.build(properties);
    }
    
	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request facade) {

		String appName = facade.getHeader("app-name");
		String key = appName == null ? "default" : appName;
		if (!deploymentMap.containsKey(key)) {
			InputStream is;
			try {
				is = new ByteArrayInputStream(getJsonConfig(key).getBytes());
				deploymentMap.put(key, KeycloakDeploymentBuilder.build(is));
			} catch (JsonProcessingException e) {
				throw new IAMConnectorException(e.getMessage());
			}
		}
		return deploymentMap.get(key);
	}
    
	private String getJsonConfig(String tenantName) throws JsonProcessingException {
		
		String client = tenantName.equalsIgnoreCase("default") ? "" : tenantName + ".";

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		
		IAMDeployment deployment = new IAMDeployment();
		deployment.setRealm(env.getProperty(client + "keycloak.realm"));
		deployment.setAuthServerUrl(env.getProperty(client + "keycloak.auth-server-url"));
		deployment.setSslRequired(env.getProperty(client + "keycloak.ssl-required"));
		deployment.setResource(env.getProperty(client + "keycloak.resource"));
		deployment.setConfidentialPort(env.getProperty(client + "keycloak.confidential-port"));
		IAMCredentials cred = new IAMCredentials();
		cred.setSecret(env.getProperty(client + "keycloak.credentials.secret"));
		deployment.setCredentials(cred);
		
		return ow.writeValueAsString(deployment);
	}

}
