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

/**
 * 
 * @author Mohankumar.D
 *
 */
public class IAMAdminClientConfig {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    
    @Override
	public String toString() {
		return "IAMAdminClientConfig [serverUrl=" + serverUrl + ", realm=" + realm + ", clientId=" + clientId
				+ ", clientSecret=" + clientSecret + "]";
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public static IAMAdminClientConfigBuilder builder() {
		return new IAMAdminClientConfigBuilder();
	}
}