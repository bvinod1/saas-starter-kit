package com.saasstarterkit.user.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean created for cross realm configuration
 * Used internally. Not exposed to client as any response object
 * 
 */
public class IAMDeployment {

	private String realm;

	@JsonProperty("auth-server-url")
	private String authServerUrl;

	@JsonProperty("ssl-required")
	private String sslRequired;

	private String resource;

	@JsonProperty("confidential-port")
	private String confidentialPort;

	private IAMCredentials credentials;

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getAuthServerUrl() {
		return authServerUrl;
	}

	public void setAuthServerUrl(String authServerUrl) {
		this.authServerUrl = authServerUrl;
	}

	public String getSslRequired() {
		return sslRequired;
	}

	public void setSslRequired(String sslRequired) {
		this.sslRequired = sslRequired;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getConfidentialPort() {
		return confidentialPort;
	}

	public void setConfidentialPort(String confidentialPort) {
		this.confidentialPort = confidentialPort;
	}

	public IAMCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(IAMCredentials credentials) {
		this.credentials = credentials;
	}

}
