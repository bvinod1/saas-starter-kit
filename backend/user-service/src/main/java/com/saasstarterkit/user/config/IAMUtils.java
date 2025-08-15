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
package com.saasstarterkit.user.config;

import org.springframework.stereotype.Service;

import com.saasstarterkit.user.config.IAMPropertyReader;

@Service
public class IAMUtils {

	public String createUserEndpoint(IAMPropertyReader keycloakPropertyReader) {
		return keycloakPropertyReader.getProperty("keycloak.auth-server-url") + "/admin/realms/"
				+ keycloakPropertyReader.getProperty("keycloak.realm") + "/users";
	}
}
