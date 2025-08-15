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

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

import com.saasstarterkit.user.config.IAMAdminClientConfig;
import com.saasstarterkit.user.config.IAMAdminClientUtils;
import com.saasstarterkit.user.config.IAMPropertyReader;

/**
 * 
 * @author Mohankumar.D
 *
 */
@Configuration
public class CurrentUserProvider {
	
    private final RestTemplate restTemplate = new RestTemplate();
    
	private final IAMPropertyReader keycloakPropertyReader;
	
	public CurrentUserProvider(IAMPropertyReader keycloakPropertyReader) {
		this.keycloakPropertyReader = keycloakPropertyReader;
	}

	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST)
	public CurrentUser getCurrentUser() {

		CurrentUser result = new CurrentUser();

		OAuth2AuthenticatedPrincipal principal = getCurrentUserPrincipal();

		String userId = principal.getName();
		String username = principal.getAttribute("preferred_username");
		Map<String, List<String>> roles = principal.getAttribute("realm_access");

		if (roles != null) {
			List<String> realmRoles = roles.get("roles");
			List<String> transformedRoles = (realmRoles != null)
					? realmRoles.stream().filter(role -> role != null).filter(roleName -> roleName != null)
							.map(roleName -> "ROLE_" + roleName.toUpperCase()).collect(Collectors.toList())
					: List.of();
			result.setRoles(transformedRoles);
		}
		result.setUserId(userId);
		result.setUsername(username);
		result.setEmail(principal.getAttribute("email"));

		return result;
	}

	public RealmResource currentRealmResource(IAMPropertyReader keycloakPropertyReader) {
		IAMAdminClientConfig keycloakAdminClientConfig = IAMAdminClientUtils.loadConfig(keycloakPropertyReader);
		Keycloak keycloak = IAMAdminClientUtils
				.getKeycloakClient(accessToken(), keycloakAdminClientConfig);

		// Get realm
		RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());

		return realmResource;
	}

	public String accessToken() {
		return "Bearer " + ((OAuth2AccessToken) SecurityContextHolder.getContext().getAuthentication().getCredentials())
				.getTokenValue();
	}

	public OAuth2AuthenticatedPrincipal getCurrentUserPrincipal() {
		return (OAuth2AuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/*public void logout() {
		KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal = getCurrentUserPrincipal();
		KeycloakDeployment deployment = principal.getKeycloakSecurityContext().getDeployment();
		principal.getKeycloakSecurityContext().logout(deployment);
	}*/

	public void logout() {
		logoutFromKeycloak(getCurrentUserPrincipal());
	}

	private void logoutFromKeycloak(OAuth2AuthenticatedPrincipal principal) {
		String AUTH_URL = keycloakPropertyReader.getProperty("keycloak.auth-server-url");
		String REALM = keycloakPropertyReader.getProperty("keycloak.realm");

		String endSessionEndpoint = AUTH_URL + "/admin/realms/" + REALM + "/users/{id}/logout";

		Map<String, String> vars = new HashMap<>();
		vars.put("id", principal.getName());

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization",
				"Bearer "
						+ ((OAuth2AccessToken) SecurityContextHolder.getContext().getAuthentication().getCredentials())
								.getTokenValue());

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endSessionEndpoint);
		URI uri = builder.buildAndExpand(vars).encode().toUri();
		HttpEntity<?> request = new HttpEntity<>(null, headers);

		restTemplate.exchange(uri, HttpMethod.POST, request, Object.class);
	}
}
